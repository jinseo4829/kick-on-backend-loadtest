#!/bin/bash
export SPRING_PROFILES_ACTIVE=dev
CONTAINER_NAME="kickon-backend-dev"

echo "=== [STOP] Cleaning up $CONTAINER_NAME ==="

# 실행 중이든 Exited 상태든 무조건 삭제
if [ "$(sudo docker ps -a -q -f name=$CONTAINER_NAME)" ]; then
  echo "Removing container: $CONTAINER_NAME"
  sudo docker rm -f $CONTAINER_NAME || true
  echo "✅ Container $CONTAINER_NAME removed."
else
  echo "No container found: $CONTAINER_NAME"
fi

# 혹시 꼬여있는 dangling 이미지 정리
echo "Cleaning up dangling Docker images..."
sudo docker image prune -f > /dev/null 2>&1
echo "✅ Cleanup done."
