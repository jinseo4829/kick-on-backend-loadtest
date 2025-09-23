==#!/bin/bash
  export SPRING_PROFILES_ACTIVE=dev
  export AWS_REGION=ap-northeast-2

  APP_DIR="/home/ubuntu/springboot-dev"
  CONTAINER_NAME="kickon-backend-dev"
  IMAGE="235494776341.dkr.ecr.ap-northeast-2.amazonaws.com/kickon-backend-dev:latest"

  # ECR 로그인
  aws ecr get-login-password --region $AWS_REGION \
    | sudo docker login --username AWS --password-stdin 235494776341.dkr.ecr.ap-northeast-2.amazonaws.com

  # 기존 컨테이너 종료
  if [ "$(sudo docker ps -q -f name=$CONTAINER_NAME)" ]; then
    echo "Stopping existing container: $CONTAINER_NAME"
    sudo docker stop $CONTAINER_NAME
    sudo docker rm $CONTAINER_NAME
  else
    echo "No running container found: $CONTAINER_NAME"
  fi

  # 로그 파일 준비
  sudo touch $APP_DIR/app.log && sudo chmod 777 $APP_DIR/app.log

  # 최신 Docker 이미지 pull
  echo "Pulling latest Docker image: $IMAGE"
  sudo docker pull $IMAGE

  # 새 Docker 컨테이너 실행 (⭐ env-file 추가)
  echo "Starting new container: $CONTAINER_NAME"
  sudo docker run -d --name $CONTAINER_NAME -p 8081:8080 \
    --env-file /etc/environment \
    -e SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE \
    $IMAGE > $APP_DIR/app.log 2>&1 &

  echo "✅ Application started with Docker: $CONTAINER_NAME (profile: $SPRING_PROFILES_ACTIVE)"
