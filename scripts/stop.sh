#!/bin/bash

APP_DIR="/srv/kickon/loadtest"
LOG_DIR="/srv/kickon/logs"
LOG_FILE="$LOG_DIR/loadtest.log"

echo "===== [STOP] Loadtest Application =====" >> "$LOG_FILE"

# 1. 디렉토리 존재 확인
if [ ! -d "$APP_DIR" ]; then
  echo "[INFO] Directory $APP_DIR does not exist. Skipping stop." >> "$LOG_FILE"
  exit 0
fi

# 2. JAR 파일 찾기
JAR_FILE=$(ls -t "$APP_DIR"/*.jar 2>/dev/null | head -n 1)

if [ -z "$JAR_FILE" ]; then
  echo "[INFO] No JAR file found in $APP_DIR. Skipping stop." >> "$LOG_FILE"
  exit 0
fi

# 3. 실행 중인 프로세스 종료
PID=$(pgrep -f "$(basename "$JAR_FILE")" || true)

if [ -n "$PID" ]; then
  echo "[INFO] Found process PID=$PID. Stopping..." >> "$LOG_FILE"
  kill -15 "$PID"
  sleep 5
  kill -9 "$PID" 2>/dev/null || true
else
  echo "[INFO] No running process found for $JAR_FILE." >> "$LOG_FILE"
fi

echo "===== [STOP COMPLETE] =====" >> "$LOG_FILE"
exit 0
