@echo off
REM === RESIK Desktop Admin — Windows EXE Builder ===
REM Requires: JDK 17+, WiX Toolset v3, Maven

echo Building RESIK Desktop Admin EXE Installer...

cd /d "%~dp0"

echo [1/3] Building fat JAR...
call mvn clean package -DskipTests -q
if %ERRORLEVEL% neq 0 (
    echo BUILD FAILED: Maven package error
    exit /b 1
)

echo [2/3] Building EXE installer with jpackage...
jpackage --type EXE --name RESIK-Admin --input target --main-jar resik-desktop-1.0.0.jar --main-class com.resik.admin.ResikDesktopApp --app-version 1.0.0 --vendor RESIK --description "RESIK Bank Sampah Digital - Admin Desktop" --win-menu --win-menu-group RESIK --win-shortcut --win-dir-chooser
if %ERRORLEVEL% neq 0 (
    echo BUILD FAILED: jpackage error
    echo Make sure WiX Toolset is installed and WIX env var is set
    exit /b 1
)

echo [3/3] Done!
echo Output: RESIK-Admin-1.0.0.exe
pause
