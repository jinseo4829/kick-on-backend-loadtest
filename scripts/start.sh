#!/bin/bash
set -e

# ===== 기본 설정 =====
AWS_REGION="ap-northeast-2"
SPRING_PROFILES_ACTIVE="loadtest"

ECR_REGISTRY="591671455490.dkr.ecr.ap-northeast-2.amazonaws.com"
ECR_REPOSITORY="kickon-loadtest"
IMAGE_TAG="latest"

CONTAINER_NAME="kickon-loadtest"
SERVER_PORT=8082

LOG_DIR="/srv/kickon/logs"
LOG_FILE="$LOG_DIR/loadtest.log"

echo "===== [START] Loadtest Docker Application ====="

# 1️⃣ 로그 디렉토리 준비
mkdir -p "$LOG_DIR"

# 2️⃣ ECR 로그인 (EC2 IAM Role 사용)
echo "[INFO] Logging in to ECR..." | tee -a "$LOG_FILE"
aws ecr get-login-password --region $AWS_REGION \
  | docker login --username AWS --password-stdin $ECR_REGISTRY

# 3️⃣ 최신 이미지 pull
echo "[INFO] Pulling Docker image..." | tee -a "$LOG_FILE"
docker pull $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG

# 4️⃣ 기존 컨테이너 종료 및 제거
if docker ps -a --format '{{.Names}}' | grep -w "$CONTAINER_NAME" > /dev/null; then
  echo "[INFO] Existing container found. Stopping..." | tee -a "$LOG_FILE"
  docker stop $CONTAINER_NAME
  docker rm $CONTAINER_NAME
fi

# 5️⃣ 컨테이너 실행
echo "[INFO] Starting new container on port $SERVER_PORT" | tee -a "$LOG_FILE"

docker run -d \
  --name $CONTAINER_NAME \
  -p $SERVER_PORT:8082 \
  -e SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE \
  -e AWS_REGION=$AWS_REGION \
  --restart unless-stopped \
  $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG \
  >> "$LOG_FILE" 2>&1

echo "[SUCCESS] Loadtest Docker application started (port=$SERVER_PORT)" | tee -a "$LOG_FILE"
echo "============================================="
