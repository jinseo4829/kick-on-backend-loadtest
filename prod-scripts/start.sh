#!/bin/bash
export SPRING_PROFILES_ACTIVE=prod
export AWS_REGION=ap-northeast-2

APP_DIR="/srv/kickon/prod"
LOG_DIR="/srv/kickon/logs"
LOG_FILE="$LOG_DIR/prod.log"

# 로그 디렉토리 생성
if [ ! -d "$LOG_DIR" ]; then
  mkdir -p $LOG_DIR
  sudo chown ubuntu:ubuntu $LOG_DIR
fi

JAR_FILE=$(ls -t $APP_DIR/*.jar 2>/dev/null | head -n 1)

if [ -z "$JAR_FILE" ]; then
  echo "> No JAR file to start."
  exit 1
fi

nohup java \
  -Dspring.profiles.active=prod \
  -Dserver.port=8080 \
  -Daws.paramstore.enabled=true \
  -jar $JAR_FILE > $LOG_FILE 2>&1 &

exit 0