@echo off
chcp 65001 >nul
setlocal EnableExtensions

if /I "%~1"=="brain" goto brain
if /I "%~1"=="server" goto server

call :java25 || goto javaMissing
start "EmergentCraft Brain" "%ComSpec%" /k call "%~f0" brain
start "EmergentCraft Fabric Server" "%ComSpec%" /k call "%~f0" server
echo EmergentCraft is starting in two windows: Brain and Fabric Server.
echo Close either window to stop that component.
exit /b 0

:brain
cd /d "%~dp0brain"
if not exist node_modules (
  echo Installing Brain dependencies...
  call npm install || exit /b 1
)
call npm run dev
exit /b %errorlevel%

:server
call :java25 || goto javaMissing
cd /d "%~dp0mod"
call gradlew.bat runServer
exit /b %errorlevel%

:java25
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" goto javaReady
for /f "tokens=2,*" %%A in ('reg query "HKLM\SOFTWARE\JavaSoft\JDK" /v JavaHome 2^>nul ^| findstr /I "JavaHome"') do set "JAVA_HOME=%%B"
if not defined JAVA_HOME set "JAVA_HOME=C:\Program Files\Java\latest\jdk-25"
if not exist "%JAVA_HOME%\bin\java.exe" exit /b 1
:javaReady
set "PATH=%JAVA_HOME%\bin;%PATH%"
exit /b 0

:javaMissing
echo Java 25 was not found. Set JAVA_HOME to your JDK 25 folder and try again.
exit /b 1
