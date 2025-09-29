#!/bin/bash
export SPRING_PROFILES_ACTIVE=dev

APP_DIR="/home/ubuntu/springboot-dev"
CONTAINER_NAME="kickon-backend-dev"
IMAGE="235494776341.dkr.ecr.ap-northeast-2.amazonaws.com/kickon-backend-dev:latest"

echo "=== [START] Starting $CONTAINER_NAME (profile=$SPRING_PROFILES_ACTIVE) ==="

# 1. ECR 로그인
aws ecr get-login-password --region ap-northeast-2 \
  | sudo docker login --username AWS --password-stdin 235494776341.dkr.ecr.ap-northeast-2.amazonaws.com

# 2. 기존 컨테이너 종료/삭제
if [ "$(sudo docker ps -a -q -f name=$CONTAINER_NAME)" ]; then
  echo "Stopping and removing old container..."
  sudo docker stop $CONTAINER_NAME || true
  sudo docker rm -f $CONTAINER_NAME || true
fi

# 3. 기존 이미지 삭제 (꼬임 방지)
if [ "$(sudo docker images -q $IMAGE)" ]; then
  echo "Removing old image: $IMAGE"
  sudo docker rmi -f $IMAGE
fi

# 4. 최신 이미지 pull
echo "Pulling fresh image: $IMAGE"
sudo docker pull $IMAGE

# 5. 로그 파일 준비 (매번 덮어쓰기)
sudo mkdir -p $APP_DIR
: > $APP_DIR/app.log   # 내용 초기화
sudo chmod 777 $APP_DIR/app.log

# 6. 새 컨테이너 실행 (로그 덮어쓰기 모드)
echo "Running container..."
if ! sudo docker run -d --name $CONTAINER_NAME -p 8081:8081 \
  --env-file /etc/environment \
  -e SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE \
  -e JWT_SECRET_KEY=$JWT_SECRET_KEY \
  -e ADMIN_JWT_SECRET_KEY=$ADMIN_JWT_SECRET_KEY \
  $IMAGE > $APP_DIR/app.log 2>&1; then
  echo "❌ Failed to start container $CONTAINER_NAME, check $APP_DIR/app.log"
  exit 1
fi

echo "✅ $CONTAINER_NAME started (profile=$SPRING_PROFILES_ACTIVE, port=8081)"
