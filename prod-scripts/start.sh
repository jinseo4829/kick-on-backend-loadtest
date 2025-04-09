#!/bin/bash
export SPRING_PROFILES_ACTIVE=prod
export AWS_REGION=ap-northeast-2

APP_DIR="/home/ubuntu/springboot-prod"
JAR_FILE=$(ls -t $APP_DIR/*.jar | head -n 1)

if [ -z "$JAR_FILE" ]; then
  echo "No JAR file found in $APP_DIR!"
  exit 1
fi

# 현재 실행 중인 프로세스 종료 (dev 환경만)
PID=$(pgrep -f "$JAR_FILE")
if [ ! -z "$PID" ]; then
  echo "Stopping process $PID running $JAR_FILE"
  kill -9 $PID
else
  echo "No running process found for $JAR_FILE"
fi

touch $APP_DIR/app.log && chmod 777 $APP_DIR/app.log
# 새 애플리케이션 실행
nohup java -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Daws.paramstore.enabled=true -jar $JAR_FILE > $APP_DIR/app.log 2>&1 &

echo "Application started: $JAR_FILE with profile: $SPRING_PROFILES_ACTIVE"
