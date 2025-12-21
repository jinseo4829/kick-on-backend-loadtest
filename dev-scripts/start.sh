#!/bin/bash
export SPRING_PROFILES_ACTIVE=dev
export AWS_REGION=ap-northeast-2

APP_DIR="/srv/kickon/dev"
LOG_DIR="/srv/kickon/logs"
LOG_FILE="$LOG_DIR/dev.log"

# 1. 로그 디렉토리 생성 및 권한 설정
if [ ! -d "$LOG_DIR" ]; then
  mkdir -p $LOG_DIR
  sudo chown ubuntu:ubuntu $LOG_DIR
fi

# 2. JAR 파일 찾기
JAR_FILE=$(ls -t $APP_DIR/*.jar 2>/dev/null | head -n 1)

if [ -z "$JAR_FILE" ]; then
  echo "> Error: No JAR file found to start in $APP_DIR."
  exit 1
fi

echo "> Starting dev application: $JAR_FILE"

# 3. 애플리케이션 실행 (dev용 8081 포트)
nohup java \
  -Dspring.profiles.active=dev \
  -Dserver.port=8081 \
  -Daws.paramstore.enabled=true \
  -jar $JAR_FILE > $LOG_FILE 2>&1 &

echo "> Dev application started on port 8081."
exit 0