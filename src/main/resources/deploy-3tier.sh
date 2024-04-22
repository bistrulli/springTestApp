#!/bin/bash

# Tier 3
kubectl apply -f tier3-deployment.yaml
sleep 2
kubectl apply -f tier3-service.yaml

sleep 3

# Tier 2
kubectl apply -f tier2-deployment.yaml
sleep 2
kubectl apply -f tier2-service.yaml

sleep 3

# Tier 1
kubectl apply -f tier1-deployment.yaml
sleep 2
kubectl apply -f tier1-service.yaml

# Deploy HPA on each tier
# kubectl autoscale deployment spring-test-app-tier1 --cpu-percent=50 --min=1 --max=100
for i in {1..3}; do kubectl autoscale deployment "spring-test-app-tier$i" --cpu-percent=50 --min=1 --max=100; done

# Check if HPA was correctly deployed
kubectl get hpa