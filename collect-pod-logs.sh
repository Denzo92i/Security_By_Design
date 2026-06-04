#!/usr/bin/env bash
# collect-pod-logs.sh
# Collecte les logs de tous les pods applicatifs pour les livrables.
# Usage: bash collect-pod-logs.sh > pod-logs-dump.txt

TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')
echo "========================================================"
echo "  POD LOGS DUMP — FoodNDeliv"
echo "  Date: $TIMESTAMP"
echo "========================================================"

echo ""
echo "=== PODS EN COURS ==="
kubectl get pods -A

# ── API RW ──
echo ""
echo "========================================================"
echo "  LOG: foodndeliv-api (API RW)"
echo "========================================================"
kubectl logs deployment/foodndeliv-api --tail=100 2>&1

# ── API RO ──
echo ""
echo "========================================================"
echo "  LOG: foodndeliv-api-ro (API RO)"
echo "========================================================"
kubectl logs deployment/foodndeliv-api-ro --tail=100 2>&1

# ── Keycloak ──
echo ""
echo "========================================================"
echo "  LOG: keycloak"
echo "========================================================"
kubectl logs deployment/keycloak --tail=100 2>&1

# ── KrakenD ──
echo ""
echo "========================================================"
echo "  LOG: krakend-gateway"
echo "========================================================"
kubectl logs deployment/krakend-gateway --tail=100 2>&1

# ── CNPG operator ──
echo ""
echo "========================================================"
echo "  LOG: cnpg operator (cnpg-system)"
echo "========================================================"
kubectl logs deployment/cnpg-cloudnative-pg -n cnpg-system --tail=50 2>&1

# ── DB pods ──
echo ""
echo "========================================================"
echo "  LOG: foodndeliv-db-1 (primary)"
echo "========================================================"
kubectl logs foodndeliv-db-1 --tail=50 2>&1

echo ""
echo "========================================================"
echo "  LOG: foodndeliv-db-2 (replica)"
echo "========================================================"
kubectl logs foodndeliv-db-2 --tail=50 2>&1

# ── PSS: vérification finale sur tous les pods ──
echo ""
echo "========================================================"
echo "  PSS: SECURITY CONTEXT — ALL PODS"
echo "========================================================"

for pod in $(kubectl get pods -o name); do
    echo ""
    echo "--- $pod ---"
    kubectl get $pod -o jsonpath='{.spec.securityContext}' | python3 -m json.tool 2>/dev/null || echo "(no pod securityContext)"
    echo "  containers:"
    kubectl get $pod -o jsonpath='{range .spec.containers[*]}  [{.name}]: {.securityContext}{"\n"}{end}' 2>/dev/null
done

# ── PSS dry-run CNPG ──
echo ""
echo "========================================================"
echo "  PSS DRY-RUN: CNPG operator pod (cnpg-system)"
echo "========================================================"
CNPG_POD=$(kubectl get pods -n cnpg-system -l app.kubernetes.io/name=cloudnative-pg -o jsonpath='{.items[0].metadata.name}')
echo "Pod: $CNPG_POD"
kubectl get pod $CNPG_POD -n cnpg-system -o yaml | kubectl apply --dry-run=server -f - 2>&1

echo ""
echo "========================================================"
echo "  PSS DRY-RUN: API deployments (namespace: default)"
echo "========================================================"
kubectl apply -f k8s/api-deployment.yaml --dry-run=server 2>&1
kubectl apply -f k8s/api-readonly-deployment.yaml --dry-run=server 2>&1

echo ""
echo "=== DONE ==="
