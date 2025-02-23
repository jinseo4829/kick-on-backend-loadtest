#!/bin/bash
export SPRING_PROFILES_ACTIVE=dev
export AWS_REGION=ap-northeast-2

APP_DIR="/home/ubuntu/springboot-dev"
JAR_FILE=$(ls -t $APP_DIR/*.jar | head -n 1)

if [ -z "$JAR_FILE" ]; then
  echo "No JAR file found in $APP_DIR!"
  exit 0
fi

# 현재 실행 중인 프로세스 종료 (dev 환경만)
PID=$(pgrep -f "$JAR_FILE")
if [ ! -z "$PID" ]; then
  echo "Stopping process $PID running $JAR_FILE"
  kill -9 $PID
  echo "Process $PID stopped."
else
  echo "No running process found for $JAR_FILE"
fi
