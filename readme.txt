================================================================================
  FoodNDeliv — CPS2010 Security by Design
  Replication Guide
  Author : Dylan Berezay (2538415)
  Date   : 1 June 2026
================================================================================

PREREQUISITES
-------------
  - minikube >= 1.32  (Docker driver)
  - kubectl  >= 1.29
  - Helm     >= 3.14
  - Docker   >= 24
  - Java 17 + Maven 3.9
  - Postman Desktop (localhost access required)

QUICK OVERVIEW
--------------
  The project deploys the following components on a local minikube cluster:

    [Postman / curl]
          |
    KrakenD Gateway  (port 8000)  ← JWT validated by Keycloak
          |
    +-----------+    +-----------+
    | API-RW    |    | API-RO    |
    | :8080     |    | :8080     |
    | (writes)  |    | (reads)   |
    +-----------+    +-----------+
          |                |
    foodndeliv-db-rw   foodndeliv-db-ro
          \               /
           CNPG Cluster
           (primary + replica)

    Keycloak  (NodePort 30080)  — OAuth2 / JWT issuer


================================================================================
  TASK 1 — CNPG CLUSTER + DUAL API DEPLOYMENT
================================================================================

STEP 1 — Start minikube
-----------------------
  minikube start --driver=docker --memory=4096 --cpus=2

STEP 2 — Install CNPG operator via Helm
----------------------------------------
  helm repo add cnpg https://cloudnative-pg.github.io/charts
  helm repo update
  helm upgrade --install cnpg \
    --namespace cnpg-system --create-namespace \
    cnpg/cloudnative-pg

  # Verify operator is running
  kubectl get pods -n cnpg-system

STEP 3 — Bootstrap the PostgreSQL cluster
------------------------------------------
  kubectl apply -f k8s/postgres-cluster.yaml
  kubectl apply -f k8s/db-secret.yaml

  # Wait for both instances to be Ready
  kubectl get cluster foodndeliv-db
  kubectl get pods -l cnpg.io/cluster=foodndeliv-db

  # Expected output:
  #   foodndeliv-db-1   1/1   Running   (primary / rw)
  #   foodndeliv-db-2   1/1   Running   (replica / ro)

  # Confirm services created by CNPG
  kubectl get svc | grep foodndeliv-db
  #   foodndeliv-db-rw   ClusterIP   ...   5432/TCP
  #   foodndeliv-db-ro   ClusterIP   ...   5432/TCP
  #   foodndeliv-db-r    ClusterIP   ...   5432/TCP

STEP 4 — Build and load Docker images
--------------------------------------
  mvn clean package -DskipTests

  # RW image (default profile)
  docker build -t foodndeliv:latest .
  minikube image load foodndeliv:latest

  # RO image (readonly Spring profile)
  docker build -t foodndeliv-ro:latest \
    --build-arg SPRING_PROFILES_ACTIVE=readonly .
  minikube image load foodndeliv-ro:latest

  Docker Hub paths (public):
    docker.io/denzo92i/foodndeliv:latest
    docker.io/denzo92i/foodndeliv-ro:latest

STEP 5 — Deploy API services
------------------------------
  kubectl apply -f k8s/api-deployment.yaml
  kubectl apply -f k8s/api-readonly-deployment.yaml

  # Verify both pods are Running
  kubectl get pods -l app=foodndeliv-api
  kubectl get pods -l app=foodndeliv-api-ro

STEP 6 — Test with Postman (Task 1)
--------------------------------------
  # Port-forward RW service
  kubectl port-forward svc/foodndeliv-api 8080:8080 &

  # Port-forward RO service
  kubectl port-forward svc/foodndeliv-api-ro 8081:8080 &

  Import postman-task1.json into Postman Desktop.
  Run the collection — expected results:
    GET  http://localhost:8080/api/ctrl/customers  → 200 OK
    POST http://localhost:8080/api/ctrl/customers  → 201 Created
    GET  http://localhost:8081/api/ctrl/customers  → 200 OK
    POST http://localhost:8081/api/ctrl/customers  → 405 Method Not Allowed


================================================================================
  TASK 2 — POD SECURITY STANDARDS (PSS) COMPLIANCE VERIFICATION
================================================================================

The PSS analysis does NOT require any new deployments. It inspects existing
running pods using kubectl dry-run.

STEP 1 — Label namespace in warn-only mode
-------------------------------------------
  kubectl label namespace default \
    pod-security.kubernetes.io/warn=baseline \
    pod-security.kubernetes.io/warn-version=latest --overwrite

  kubectl label namespace default \
    pod-security.kubernetes.io/warn=restricted \
    pod-security.kubernetes.io/warn-version=latest --overwrite

  # No warnings emitted = compliant for that profile.

STEP 2 — Dry-run API deployments
----------------------------------
  kubectl apply -f k8s/api-deployment.yaml          --dry-run=server
  kubectl apply -f k8s/api-readonly-deployment.yaml --dry-run=server

  # Expected: "unchanged (server dry run)" with no PSS warnings.

STEP 3 — Inspect security contexts
-------------------------------------
  # API-RW pod-level context
  kubectl get pod -l app=foodndeliv-api \
    -o jsonpath='{.items[0].spec.securityContext}' | python3 -m json.tool

  # API-RW container-level context
  kubectl get pod -l app=foodndeliv-api \
    -o jsonpath='{.items[0].spec.containers[0].securityContext}' | python3 -m json.tool

  # CNPG operator context (cnpg-system namespace)
  kubectl get pod -n cnpg-system \
    -l app.kubernetes.io/name=cloudnative-pg \
    -o jsonpath='{.items[0].spec.containers[0].securityContext}' | python3 -m json.tool

  See kubectl-dump-task2-final.txt for the full raw output.

STEP 4 — Collect Restricted PSS dry-run for CNPG
---------------------------------------------------
  CNPG_POD=$(kubectl get pods -n cnpg-system \
    -l app.kubernetes.io/name=cloudnative-pg \
    -o jsonpath='{.items[0].metadata.name}')
  kubectl get pod $CNPG_POD -n cnpg-system -o yaml | \
    kubectl apply --dry-run=server -f -


================================================================================
  TASK 3 — KRAKEND GATEWAY + KEYCLOAK + RBAC/ABAC
================================================================================

STEP 1 — Deploy Keycloak
--------------------------
  # Install Keycloak operator (v26.0.5)
  kubectl apply -f https://raw.githubusercontent.com/keycloak/keycloak-k8s-resources/refs/tags/26.0.5/kubernetes/kubernetes.yml

  kubectl apply -f k8s/keycloak.yaml

  # Wait for Keycloak to be ready (may take 2–3 minutes)
  kubectl rollout status deployment/keycloak

  # Verify NodePort is reachable
  minikube service keycloak --url
  # → http://127.0.0.1:30080

STEP 2 — Import realm
-----------------------
  # The foodndeliv realm is auto-imported via ConfigMap mounted at startup.
  # To verify:
  kubectl create configmap foodndeliv-realm \
    --from-file=task3/foodndeliv-realm.json \
    --dry-run=client -o yaml | kubectl apply -f -

  Realm export files:
    task3/foodndeliv-realm.json   (foodndeliv realm)
    task3/master-realm.json       (master realm)

  Roles defined in foodndeliv realm:
    ROLE_ADMIN, ROLE_CUSTOMER, ROLE_RESTAURANT, ROLE_RIDER

  Test users:
    admin-user  / admin123   → ROLE_ADMIN
    customer1   / customer123 → ROLE_CUSTOMER

STEP 3 — Obtain a JWT token
------------------------------
  # Forward Keycloak port if not using NodePort
  kubectl port-forward svc/keycloak 8090:8080 &

  curl -s -X POST \
    "http://localhost:8090/realms/foodndeliv/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "client_id=foodndeliv-client" \
    -d "client_secret=X2VaVjTIhX9mgM41Oc3KcPFlBHNurnzt" \
    -d "username=admin-user" \
    -d "password=admin123" \
    -d "grant_type=password" | jq -r '.access_token'

  # Copy the returned token — it is used as Bearer in all API calls.

STEP 4 — Deploy KrakenD
-------------------------
  kubectl apply -f k8s/krakend-deployment.yaml

  # Verify pod is Running
  kubectl get pods -l app=krakend-gateway

  # Port-forward gateway
  kubectl port-forward svc/krakend-gateway 8000:8000 &

  KrakenD routes (krakend.json):
    GET  /customers   → foodndeliv-api-ro:8080/api/ctrl/customers
    POST /customers   → foodndeliv-api:8080/api/ctrl/customers
    GET  /orders      → foodndeliv-api-ro:8080/api/ctrl/orders
    POST /orders      → foodndeliv-api:8080/api/ctrl/orders
    GET  /restaurants → foodndeliv-api-ro:8080/api/ctrl/restaurants
    POST /restaurants → foodndeliv-api:8080/api/ctrl/restaurants

  JWT validation: KrakenD forwards the Authorization header verbatim to the
  Spring Boot backend. Spring Security (configured with Keycloak's JWKS URI)
  validates the token and enforces RBAC via @PreAuthorize annotations.

STEP 5 — Test with Postman (Task 3)
--------------------------------------
  Import postman-task3.json into Postman Desktop.
  Set collection variable: access_token = <JWT from Step 3>

  Expected results:
    GET  http://localhost:8000/customers          → 200 OK  (any valid token)
    POST http://localhost:8000/customers  (valid body)  → 201 Created
    POST http://localhost:8000/customers  (missing name) → 400 Bad Request
    POST http://localhost:8000/orders     (> 5 pending) → 409 / 400
    GET  http://localhost:8000/restaurants (CUSTOMER)   → 200 OK
    POST http://localhost:8000/restaurants (CUSTOMER)   → 403 Forbidden
    POST http://localhost:8000/restaurants (ADMIN)      → 201 Created

STEP 6 — Rebuild after code changes (if needed)
-------------------------------------------------
  mvn clean package -DskipTests
  docker build -t foodndeliv:latest .
  minikube image load foodndeliv:latest
  kubectl rollout restart deployment/foodndeliv-api
  kubectl rollout restart deployment/foodndeliv-api-ro


================================================================================
  DIGITAL ARTIFACTS REFERENCE
================================================================================

  File                          Description
  ----------------------------  ------------------------------------------------
  k8s/postgres-cluster.yaml     CNPG cluster manifest (2 instances, initdb)
  k8s/db-secret.yaml            PostgreSQL credentials Secret
  k8s/api-deployment.yaml       API-RW Deployment + Service
  k8s/api-readonly-deployment.yaml  API-RO Deployment + Service
  k8s/keycloak.yaml             Keycloak Deployment + NodePort Service
  k8s/krakend-deployment.yaml   KrakenD Deployment + ConfigMap + Service
  krakend.json                  KrakenD endpoint configuration
  task3/foodndeliv-realm.json   Keycloak foodndeliv realm export
  task3/master-realm.json       Keycloak master realm export
  postman-task1.json            Postman collection — Task 1 tests (4 requests)
  postman-task3.json            Postman collection — Task 3 tests (8 requests)
  kubectl-dump-task1-final.txt  kubectl session — Task 1 deployments
  kubectl-dump-task2-final.txt  kubectl session — Task 2 PSS analysis
  kubectl-dump-task3.txt        kubectl session — Task 3 deployments
  kubectl-dump-final.txt        kubectl session — Full cluster state
  pod-logs-dump.txt             Application logs from all pods
  Dockerfile                    Multi-stage Docker build (Java 17 / Alpine)
  pom.xml                       Maven project descriptor
  collect-pod-logs.sh           Helper script to collect all pod logs

  Source changes (Task 3b):
    src/.../dto/CustomerRequestDTO.java       Bean validation annotations
    src/.../dto/OrderRequestDTO.java          Bean validation annotations
    src/.../dto/OrderLineDTO.java             Bean validation annotations
    src/.../service/OrderService.java         Server-side total, invariant, RBAC
    src/.../service/CustomerService.java      RBAC @PreAuthorize
    src/.../controller/OrderController.java   @Valid, getOrderById
    src/.../controller/CustomerController.java  @Valid
    src/.../config/ReadOnlyFilter.java        Blocks non-GET on RO instance


================================================================================
  TROUBLESHOOTING
================================================================================

Pod stuck in Pending:
  kubectl describe pod <pod-name>
  → Check resource limits; increase minikube memory if needed:
    minikube stop && minikube start --memory=6144 --cpus=4 --driver=docker

CNPG cluster not becoming Ready:
  kubectl describe cluster foodndeliv-db
  kubectl logs -n cnpg-system deployment/cnpg-cloudnative-pg | tail -50

Keycloak token request returns 401:
  → Verify client_secret matches the value in keycloak.yaml ConfigMap.
  → Confirm the foodndeliv realm is active (not master).

KrakenD returns 502 Bad Gateway:
  kubectl logs deployment/krakend-gateway
  → Check that foodndeliv-api and foodndeliv-api-ro services are reachable
    within the cluster (ClusterIP DNS resolution).

Spring Boot returns 500 on validation errors:
  → Ensure @ControllerAdvice / ResponseEntityExceptionHandler is registered.
  → Check pod logs: kubectl logs deployment/foodndeliv-api --tail=50

================================================================================
  END OF README
================================================================================