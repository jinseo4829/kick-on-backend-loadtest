#!/bin/bash
export SPRING_PROFILES_ACTIVE=dev

CONTAINER_NAME="kickon-backend-dev"

# 현재 실행 중인 Docker 컨테이너 종료
if [ "$(sudo docker ps -q -f name=$CONTAINER_NAME)" ]; then
  echo "Stopping container: $CONTAINER_NAME"
  sudo docker stop $CONTAINER_NAME
  sudo docker rm $CONTAINER_NAME
  echo "Container $CONTAINER_NAME stopped and removed."
else
  echo "No running container found: $CONTAINER_NAME"
fi