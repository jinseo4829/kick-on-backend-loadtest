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
if [ "$(sudo docker ps -q -f name=$CONTAINER_NAME)" ]; then
  echo "Stopping and removing old container..."
  sudo docker stop $CONTAINER_NAME
  sudo docker rm $CONTAINER_NAME
fi

# 3. 로그 파일 준비
sudo mkdir -p $APP_DIR
sudo touch $APP_DIR/app.log && sudo chmod 777 $APP_DIR/app.log

# 4. 최신 이미지 pull
echo "Pulling image: $IMAGE"
sudo docker pull $IMAGE

# 5. 새 컨테이너 실행 (컨테이너 안도 8081, EC2도 8081)
echo "Running container on port 8081"
sudo docker run -d --name $CONTAINER_NAME -p 8081:8081 \
  --env-file /etc/environment \
  -e SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE \
  $IMAGE >> $APP_DIR/app.log 2>&1 &

echo "✅ $CONTAINER_NAME started (profile=$SPRING_PROFILES_ACTIVE, port=8081)"
