#!/bin/bash
export SPRING_PROFILES_ACTIVE=dev
CONTAINER_NAME="kickon-backend-dev"

echo "=== [STOP] Stopping $CONTAINER_NAME ==="

# 컨테이너가 실행 중이면 종료 후 삭제
if [ "$(sudo docker ps -q -f name=$CONTAINER_NAME)" ]; then
  echo "Stopping and removing container: $CONTAINER_NAME"
  sudo docker stop $CONTAINER_NAME
  sudo docker rm -f $CONTAINER_NAME
  echo "✅ Container $CONTAINER_NAME stopped and removed."
else
  echo "No running container found: $CONTAINER_NAME"
fi

# 혹시 컨테이너는 없지만 이미지 꼬여있을 수 있으니 dangling 이미지 정리
echo "Cleaning up dangling Docker images..."
sudo docker image prune -f > /dev/null 2>&1

# 네트워크에 혹시 남은 것도 정리 (옵션)
# echo "Cleaning up dangling Docker networks..."
# sudo docker network prune -f > /dev/null 2>&1

echo "✅ Cleanup done."
