#!/bin/bash
APP_DIR="/srv/kickon/dev"

# 1. 디렉토리가 없으면 에러 없이 종료 (정상 처리)
if [ ! -d "$APP_DIR" ]; then
  echo "Directory $APP_DIR does not exist. Skipping..."
  exit 0
fi

# 2. JAR 파일 찾기 (에러 메시지 숨김 처리)
JAR_FILE=$(ls -t $APP_DIR/*.jar 2>/dev/null | head -n 1)

# 3. JAR 파일이 없으면 에러 없이 종료
if [ -z "$JAR_FILE" ]; then
  echo "No JAR file to stop. Skipping..."
  exit 0
fi

# 4. 프로세스 종료
PID=$(pgrep -f "$(basename "$JAR_FILE")")
if [ ! -z "$PID" ]; then
  echo "Stopping process $PID..."
  kill -15 $PID
  sleep 5
  # 여전히 살아있으면 강제 종료
  kill -9 $PID 2>/dev/null
fi

exit 0