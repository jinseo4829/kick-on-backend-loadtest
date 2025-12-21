#!/bin/bash
APP_DIR="/srv/kickon/prod"

if [ ! -d "$APP_DIR" ]; then
  echo "> Directory $APP_DIR does not exist."
  exit 0
fi

# JAR 파일 찾기
JAR_FILE=$(ls -t $APP_DIR/*.jar 2>/dev/null | head -n 1)

if [ -z "$JAR_FILE" ]; then
  echo "> No JAR file found."
  exit 0
fi

# 프로세스 종료
PID=$(pgrep -f "$(basename "$JAR_FILE")") || true

if [ ! -z "$PID" ]; then
  echo "> Killing process $PID"
  kill -15 $PID
  sleep 5
  kill -9 $PID 2>/dev/null || true
fi

# 무조건 성공 보고
exit 0