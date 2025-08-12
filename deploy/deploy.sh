#!/bin/bash

# 설정
PROJECT_DIR="/"
TOMCAT_DIR="/"
WAR_NAME="ROOT.war"

echo "==== 백엔드 빌드 시작 ===="
cd "$PROJECT_DIR" || exit 1

# 1. 빌드 (일반 사용자 권한)
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