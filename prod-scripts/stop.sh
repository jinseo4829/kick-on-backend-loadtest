#!/bin/bash
APP_DIR="/srv/kickon/prod"

if [ ! -d "$APP_DIR" ]; then
  echo "Directory $APP_DIR does not exist. Skipping..."
  exit 0
fi

JAR_FILE=$(ls -t $APP_DIR/*.jar 2>/dev/null | head -n 1)

if [ -z "$JAR_FILE" ]; then
  echo "No JAR file to stop. Skipping..."
  exit 0
fi

PID=$(pgrep -f "$(basename "$JAR_FILE")")
if [ ! -z "$PID" ]; then
  echo "Stopping process $PID..."
  kill -15 $PID
  sleep 5
  kill -9 $PID 2>/dev/null
fi

exit 0