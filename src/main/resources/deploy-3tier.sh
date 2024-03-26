#!/bin/bash

# Tier 3
kubectl apply -f tier3-deployment.yaml
#sleep 2
#kubectl apply -f tier3-service.yaml

sleep 3

# Tier 2
kubectl apply -f tier2-deployment.yaml
#sleep 2
#kubectl apply -f tier2-service.yaml

sleep 3

# Tier 1
kubectl apply -f tier1-deployment.yaml
#sleep 2
#kubectl apply -f tier1-service.yaml