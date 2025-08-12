@echo off
SETLOCAL ENABLEDELAYEDEXPANSION

:: 설정
set "PROJECT_DIR=C:\your\path"
set "TOMCAT_DIR=C:\your\path"
set "WAR_NAME=ROOT.war"
set "CATALINA_HOME=%TOMCAT_DIR%"

chcp 65001 >nul

echo 톰캣 경로: "%TOMCAT_DIR%"
echo 프로젝트 경로: "%PROJECT_DIR%"

echo ==== 백엔드 빌드 시작 ====
cd /d "%PROJECT_DIR%" || (
    echo [ERROR] 프로젝트 폴더로 이동 실패: %PROJECT_DIR%
    exit /b 1
)
call gradlew.bat clean build -x test
if %errorlevel% neq 0 (
    echo [ERROR] 빌드 실패
    exit /b 1
)

echo ==== WAR 파일 배포 ====
if exist "%TOMCAT_DIR%\webapps\ROOT" (
    rmdir /s /q "%TOMCAT_DIR%\webapps\ROOT"
)
if exist "%TOMCAT_DIR%\webapps\%WAR_NAME%" (
    del /f /q "%TOMCAT_DIR%\webapps\%WAR_NAME%"
)

copy /Y "%PROJECT_DIR%\build\libs\%WAR_NAME%" "%TOMCAT_DIR%\webapps\" || (
    echo [ERROR] WAR 파일 복사 실패
    exit /b 1
)

echo ==== 톰캣 재시작 ====
call "%TOMCAT_DIR%\bin\shutdown.bat"
if %errorlevel% neq 0 echo [WARN] 톰캣 종료 명령 실패
timeout /t 3 /nobreak >nul
call "%TOMCAT_DIR%\bin\startup.bat"
if %errorlevel% neq 0 (
    echo [ERROR] 톰캣 시작 실패
    exit /b 1
)

echo 배포 완료 [OK]

ENDLOCAL
