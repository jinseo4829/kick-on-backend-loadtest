#!/bin/bash

set -e

export SPRING_PROFILES_ACTIVE=loadtest
export AWS_REGION=ap-northeast-2

APP_DIR="/srv/kickon/loadtest"
LOG_DIR="/srv/kickon/logs"
LOG_FILE="$LOG_DIR/loadtest.log"
SERVER_PORT=8082

echo "===== [START] Loadtest Application ====="

# 1. 로그 디렉토리 준비
mkdir -p "$LOG_DIR"

# 2. 최신 JAR 찾기
JAR_FILE=$(ls -t "$APP_DIR"/*.jar 2>/dev/null | head -n 1)

if [ -z "$JAR_FILE" ]; then
  echo "[ERROR] No JAR file found in $APP_DIR" | tee -a "$LOG_FILE"
  exit 1
fi

echo "[INFO] Found JAR: $JAR_FILE" | tee -a "$LOG_FILE"

# 3. 기존 프로세스 종료 (중복 실행 방지)
PID=$(pgrep -f "$JAR_FILE" || true)
if [ -n "$PID" ]; then
  echo "[INFO] Existing process found (PID=$PID). Stopping..." | tee -a "$LOG_FILE"
  kill -15 "$PID"
  sleep 5
fi

# 4. 애플리케이션 실행
echo "[INFO] Starting application on port $SERVER_PORT" | tee -a "$LOG_FILE"

nohup java \
  -Dspring.profiles.active=loadtest \
  -Dserver.port=$SERVER_PORT \
  -Daws.paramstore.enabled=true \
  -jar "$JAR_FILE" \
  >> "$LOG_FILE" 2>&1 &

echo "[SUCCESS] Loadtest application started (port=$SERVER_PORT)" | tee -a "$LOG_FILE"
echo "========================================"

exit 0
