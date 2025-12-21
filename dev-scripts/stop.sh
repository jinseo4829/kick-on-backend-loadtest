#!/bin/bash
APP_DIR="/srv/kickon/dev"

# 1. 디렉토리 존재 확인
if [ ! -d "$APP_DIR" ]; then
  echo "> Directory $APP_DIR does not exist. Skipping stop."
  exit 0
fi

# 2. 실행 중인 JAR 찾기
JAR_FILE=$(ls -t $APP_DIR/*.jar 2>/dev/null | head -n 1)

if [ -z "$JAR_FILE" ]; then
  echo "> No JAR file found in $APP_DIR. Skipping stop."
  exit 0
fi

# 3. 프로세스 종료 (pgrep 실패 시 에러 방지를 위해 || true 추가)
PID=$(pgrep -f "$(basename "$JAR_FILE")") || true

if [ ! -z "$PID" ]; then
  echo "> Found process $PID. Stopping..."
  kill -15 $PID
  sleep 5
  kill -9 $PID 2>/dev/null || true
else
  echo "> No running process found for $JAR_FILE."
fi

# ⭐ 명시적으로 성공을 반환
exit 0