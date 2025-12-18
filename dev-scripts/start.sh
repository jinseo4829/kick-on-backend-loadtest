#!/bin/bash
export SPRING_PROFILES_ACTIVE=dev
export AWS_REGION=ap-northeast-2

APP_DIR="/srv/kickon/dev"
LOG_FILE="/srv/kickon/logs/dev.log"

JAR_FILE=$(ls -t $APP_DIR/*.jar | head -n 1)

if [ -z "$JAR_FILE" ]; then
  echo "No JAR file found in $APP_DIR!"
  exit 1
fi

PID=$(pgrep -f "$JAR_FILE")
if [ ! -z "$PID" ]; then
  kill -9 $PID
fi

nohup java \
  -Dspring.profiles.active=dev \
  -Dserver.port=8081 \
  -Daws.paramstore.enabled=true \
  -jar $JAR_FILE > $LOG_FILE 2>&1 &
