#!/bin/bash
export SPRING_PROFILES_ACTIVE=dev
CONTAINER_NAME="kickon-backend-dev"

echo "=== [STOP] Stopping $CONTAINER_NAME ==="

# 실행 중인 컨테이너가 있으면 종료
if [ "$(sudo docker ps -q -f name=$CONTAINER_NAME)" ]; then
  echo "Stopping container: $CONTAINER_NAME"
  sudo docker stop $CONTAINER_NAME
fi

# 종료된 컨테이너까지 모두 삭제
if [ "$(sudo docker ps -aq -f name=$CONTAINER_NAME)" ]; then
  echo "Removing container: $CONTAINER_NAME"
  sudo docker rm -f $CONTAINER_NAME
fi

echo "✅ Container $CONTAINER_NAME stopped and removed."

# 혹시 컨테이너는 없지만 이미지 꼬여있을 수 있으니 dangling 이미지 정리
echo "Cleaning up dangling Docker images..."
sudo docker image prune -f > /dev/null 2>&1

echo "✅ Cleanup done."
