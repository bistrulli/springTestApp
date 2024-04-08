#!/bin/bash

IMAGE_NAME="rpizziol/spring-test-app-tier3"
TAG="0.07"

docker build --no-cache -t $IMAGE_NAME:$TAG . && docker push $IMAGE_NAME:$TAG
