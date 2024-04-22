#!/bin/bash

IMAGE_NAME="rpizziol/spring-test-app-tier2"
TAG="0.18"

docker build --no-cache -t $IMAGE_NAME:$TAG . && docker push $IMAGE_NAME:$TAG
