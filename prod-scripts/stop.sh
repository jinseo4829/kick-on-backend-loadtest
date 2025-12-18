#!/bin/bash
APP_DIR="/srv/kickon/prod"
JAR_FILE=$(ls -t $APP_DIR/*.jar | head -n 1)

[ -z "$JAR_FILE" ] && exit 0

PID=$(pgrep -f "$JAR_FILE")
[ ! -z "$PID" ] && kill -9 $PID
