@echo off
:: Maven Wrapper script for Windows
:: Downloads and runs Maven automatically

setlocal

set WRAPPER_JAR=.mvn\wrapper\maven-wrapper.jar
set WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar
set DIST_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip
set DIST_NAME=apache-maven-3.9.9
set MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\%DIST_NAME%

if not exist "%MAVEN_HOME%\%DIST_NAME%\bin\mvn.cmd" (
    echo Downloading Maven distribution...
    mkdir "%MAVEN_HOME%" 2>nul
    powershell -Command "Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%TEMP%\maven-dist.zip'"
    powershell -Command "Expand-Archive -Path '%TEMP%\maven-dist.zip' -DestinationPath '%MAVEN_HOME%' -Force"
    del "%TEMP%\maven-dist.zip" 2>nul
)

set JAVA_HOME_CHECK=%JAVA_HOME%
if "%JAVA_HOME_CHECK%"=="" (
    for /f "tokens=*" %%i in ('where java 2^>nul') do set JAVA_CMD=%%i
) else (
    set JAVA_CMD=%JAVA_HOME%\bin\java.exe
)

"%MAVEN_HOME%\%DIST_NAME%\bin\mvn.cmd" %*

endlocal
