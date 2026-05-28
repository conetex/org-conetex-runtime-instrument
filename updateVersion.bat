REM this script is just magic around this simple command: mvn versions:set -DnewVersion=%NEW_VERSION%
@echo off
setlocal ENABLEDELAYEDEXPANSION

REM 1) get version from maven
for /f "delims=" %%v in ('mvn -q help:evaluate "-Dexpression=project.version" -DforceStdout') do (
    set CURRENT_VERSION=%%v
)

if "%CURRENT_VERSION%"=="" (
    echo Konnte aktuelle Version nicht ermitteln.
    exit /b 1
)

echo Current Version: %CURRENT_VERSION%

REM 2) handle -SNAPSHOT
REM lenght of "-SNAPSHOT" is 9
set LAST9=%CURRENT_VERSION:~-9%
if "%LAST9%"=="-SNAPSHOT" (
    set BASE_VERSION=%CURRENT_VERSION:~0,-9%
) else (
    set BASE_VERSION=%CURRENT_VERSION%
)

REM 3) split major.minor.patch
for /f "tokens=1-3 delims=." %%a in ("%BASE_VERSION%") do (
    set MAJOR=%%a
    set MINOR=%%b
    set PATCH=%%c
)

if "%PATCH%"=="" set PATCH=0

REM 4) increase Patch
set /a PATCH=PATCH+1

REM 5) build new version string
set NEW_VERSION=%MAJOR%.%MINOR%.%PATCH%-SNAPSHOT

echo Neue Version: %NEW_VERSION%

REM 6) set new version string
mvn versions:set -DnewVersion=%NEW_VERSION%

endlocal