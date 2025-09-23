#!/bin/bash
export SPRING_PROFILES_ACTIVE=dev
CONTAINER_NAME="kickon-backend-dev"

echo "=== [STOP] Stopping $CONTAINER_NAME ==="

if [ "$(sudo docker ps -q -f name=$CONTAINER_NAME)" ]; then
  echo "Stopping and removing container: $CONTAINER_NAME"
  sudo docker stop $CONTAINER_NAME
  sudo docker rm $CONTAINER_NAME
  echo "✅ Container $CONTAINER_NAME stopped and removed."
else
  echo "No running container found: $CONTAINER_NAME"
fi
