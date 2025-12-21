#!/bin/bash
export SPRING_PROFILES_ACTIVE=dev
export AWS_REGION=ap-northeast-2

APP_DIR="/srv/kickon/dev"
LOG_DIR="/srv/kickon/logs"
LOG_FILE="$LOG_DIR/dev.log"

# 1. 로그 디렉토리가 없으면 생성 (에러 방지)
if [ ! -d "$LOG_DIR" ]; then
  mkdir -p $LOG_DIR
fi

# 2. 실행할 JAR 파일 찾기
JAR_FILE=$(ls -t $APP_DIR/*.jar | head -n 1)

if [ -z "$JAR_FILE" ]; then
  echo "No JAR file found in $APP_DIR!"
  exit 1
fi

echo "Deploying JAR: $JAR_FILE"

# 3. 애플리케이션 실행
# (기존 프로세스 종료는 CodeDeploy의 ApplicationStop 단계인 stop.sh에서 이미 처리됨)
nohup java \
  -Dspring.profiles.active=dev \
  -Dserver.port=8081 \
  -Daws.paramstore.enabled=true \
  -jar $JAR_FILE > $LOG_FILE 2>&1 &