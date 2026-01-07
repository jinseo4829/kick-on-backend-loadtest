#!/bin/bash

set -e

CONTAINER_NAME="kickon-loadtest"
LOG_DIR="/srv/kickon/logs"
LOG_FILE="$LOG_DIR/loadtest.log"

echo "===== [STOP] Loadtest Docker Application =====" | tee -a "$LOG_FILE"

# 1️⃣ Docker 컨테이너 존재 여부 확인
if docker ps -a --format '{{.Names}}' | grep -w "$CONTAINER_NAME" > /dev/null; then
  echo "[INFO] Container '$CONTAINER_NAME' found." | tee -a "$LOG_FILE"

  # 실행 중이면 중지
  if docker ps --format '{{.Names}}' | grep -w "$CONTAINER_NAME" > /dev/null; then
    echo "[INFO] Stopping container..." | tee -a "$LOG_FILE"
    docker stop "$CONTAINER_NAME"
  fi

  # 컨테이너 제거
  echo "[INFO] Removing container..." | tee -a "$LOG_FILE"
  docker rm "$CONTAINER_NAME"
else
  echo "[INFO] No container named '$CONTAINER_NAME' found. Skipping stop." | tee -a "$LOG_FILE"
fi

echo "===== [STOP COMPLETE] =====" | tee -a "$LOG_FILE"
exit 0
