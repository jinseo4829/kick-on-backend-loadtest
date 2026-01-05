#!/bin/bash
export SPRING_PROFILES_ACTIVE=loadtest
export AWS_REGION=ap-northeast-2

APP_DIR="/srv/kickon/loadtest"
LOG_DIR="/srv/kickon/logs"
LOG_FILE="$LOG_DIR/loadtest.log"

# 로그 디렉토리 생성
if [ ! -d "$LOG_DIR" ]; then
  mkdir -p $LOG_DIR
  chown ubuntu:ubuntu $LOG_DIR
fi

# JAR 찾기
JAR_FILE=$(ls -t $APP_DIR/*.jar 2>/dev/null | head -n 1)

if [ -z "$JAR_FILE" ]; then
  echo "> Error: No JAR file found in $APP_DIR" >> $LOG_FILE
  exit 1
fi

echo "> Starting loadtest application: $JAR_FILE" >> $LOG_FILE

nohup java \
  -Dspring.profiles.active=loadtest \
  -Dserver.port=8082 \
  -Daws.paramstore.enabled=true \
  -jar $JAR_FILE >> $LOG_FILE 2>&1 &

echo "> Loadtest application started on port 8082" >> $LOG_FILE
exit 0
