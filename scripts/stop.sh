#!/bin/bash
export SPRING_PROFILES_ACTIVE=loadtest
export AWS_REGION=ap-northeast-2

APP_DIR="/srv/kickon/loadtest"
LOG_DIR="/srv/kickon/logs"
LOG_FILE="$LOG_DIR/loadtest.log"

echo "> Starting loadtest application..."

# 1. 디렉토리 준비
mkdir -p $APP_DIR
mkdir -p $LOG_DIR
sudo chown -R ubuntu:ubuntu /srv/kickon

# 2. 최신 JAR 찾기
JAR_FILE=$(ls -t $APP_DIR/*.jar 2>/dev/null | head -n 1)

if [ -z "$JAR_FILE" ]; then
  echo "> Error: No JAR file found in $APP_DIR"
  exit 1
fi

echo "> Found JAR: $JAR_FILE"

# 3. 기존 프로세스 중복 방지
PID=$(pgrep -f "$JAR_FILE") || true
if [ ! -z "$PID" ]; then
  echo "> Existing process found ($PID). Killing..."
  kill -15 $PID
  sleep 5
fi

# 4. 애플리케이션 실행
nohup java \
  -Dspring.profiles.active=loadtest \
  -Dserver.port=8080 \
  -jar $JAR_FILE > $LOG_FILE 2>&1 &

echo "> Loadtest application started on port 8080"
exit 0
