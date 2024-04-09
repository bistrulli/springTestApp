#!/bin/bash

IMAGE_NAME="rpizziol/spring-test-app-tier1"
TAG="0.19"

docker build --no-cache -t $IMAGE_NAME:$TAG . && docker push $IMAGE_NAME:$TAG
