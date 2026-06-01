# FoodNDeliv - How to replicate

## Prerequisites
- minikube, kubectl, helm, java 21, maven, docker

## Steps
1. minikube start --driver=docker --memory=4096 --cpus=2
2. helm repo add cnpg https://cloudnative-pg.github.io/charts && helm repo update
3. helm upgrade --install cnpg --namespace cnpg-system --create-namespace cnpg/cloudnative-pg
4. kubectl apply -f k8s/postgres-cluster.yaml
5. kubectl apply -f k8s/db-secret.yaml
6. mvn clean package -DskipTests
7. minikube image build -t foodndeliv:latest .
8. kubectl apply -f k8s/api-deployment.yaml
9. kubectl apply -f k8s/api-readonly-deployment.yaml
