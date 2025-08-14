#!/bin/bash

# OS별 홈 디렉터리 기반 경로 설정
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    BASE_DIR="$HOME/light_house"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux
    BASE_DIR="$HOME/light_house"
else
    echo "지원되지 않는 OS: $OSTYPE"
    exit 1
fi

PROJECT_DIR="$BASE_DIR/FinalProjectBack"
TOMCAT_DIR="$BASE_DIR/apache-tomcat-9.0.105"
WAR_NAME="ROOT.war"

echo "==== 백엔드 빌드 시작 ===="
cd "$PROJECT_DIR" || { echo "❌ 프로젝트 디렉터리 이동 실패"; exit 1; }

# 빌드
./gradlew clean build -x test || { echo "❌ 빌드 실패"; exit 1; }

echo "==== WAR 파일 배포 ===="
# 기존 WAR/폴더 삭제
sudo rm -rf "$TOMCAT_DIR/webapps/ROOT" "$TOMCAT_DIR/webapps/$WAR_NAME"

# 새 WAR 복사
sudo cp "$PROJECT_DIR/build/libs/$WAR_NAME" "$TOMCAT_DIR/webapps/"

echo "==== 톰캣 재시작 ===="
sudo "$TOMCAT_DIR/bin/shutdown.sh"
sleep 3
sudo "$TOMCAT_DIR/bin/startup.sh"

echo "✅ 배포 완료"
