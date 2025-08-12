@echo off
SETLOCAL ENABLEDELAYEDEXPANSION

:: 설정
set "PROJECT_DIR=C:"
set "TOMCAT_DIR=C:"
set "WAR_NAME=ROOT.war"

echo ==== 백엔드 빌드 시작 ====
cd /d "%PROJECT_DIR%" || (
    echo 폴더 이동 실패
    exit /b 1
)

:: 1. 빌드 (gradlew.bat 호출)
call gradlew.bat clean build -x test
if errorlevel 1 (
    echo ❌ 빌드 실패
    exit /b 1
)

echo ==== WAR 파일 배포 ====
:: 기존 WAR/폴더 삭제
if exist "%TOMCAT_DIR%\webapps\ROOT" (
    rmdir /s /q "%TOMCAT_DIR%\webapps\ROOT"
)
if exist "%TOMCAT_DIR%\webapps\%WAR_NAME%" (
    del /f /q "%TOMCAT_DIR%\webapps\%WAR_NAME%"
)

:: 새 WAR 복사
copy /Y "%PROJECT_DIR%\build\libs\%WAR_NAME%" "%TOMCAT_DIR%\webapps\"

echo ==== 톰캣 재시작 ====
call "%TOMCAT_DIR%\bin\shutdown.bat"
timeout /t 3 /nobreak >nul
call "%TOMCAT_DIR%\bin\startup.bat"

echo ✅ 배포 완료
ENDLOCAL
