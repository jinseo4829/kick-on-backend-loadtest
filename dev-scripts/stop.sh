#!/bin/bash
export SPRING_PROFILES_ACTIVE=dev
CONTAINER_NAME="kickon-backend-dev"

echo "=== [STOP] Stopping $CONTAINER_NAME ==="

# 실행 중이든 종료 상태든 무조건 삭제
if [ "$(sudo docker ps -a -q -f name=$CONTAINER_NAME)" ]; then
  echo "Stopping and removing container: $CONTAINER_NAME"
  sudo docker stop $CONTAINER_NAME || true
  sudo docker rm -f $CONTAINER_NAME || true
  echo "✅ Container $CONTAINER_NAME stopped and removed."
else
  echo "No container found: $CONTAINER_NAME"
fi

# 꼬여있는 dangling 이미지 정리
echo "Cleaning up dangling Docker images..."
sudo docker image prune -f > /dev/null 2>&1

echo "✅ Cleanup done."
