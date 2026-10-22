# RESIK Desktop Admin — Build EXE Installer (Windows)

## Prerequisites
1. Install **JDK 17+** (includes jpackage)
2. Install **WiX Toolset v3** (for EXE installer): https://wixtoolset.org/
3. Set `WIX` environment variable: `set WIX=C:\Program Files (x86)\WiX Toolset v3.14\bin`

## Build Steps

### Option A: Build EXE directly
```cmd
cd desktop-admin
mvn clean package -DskipTests -Pexe
```
Output: `target/RESIK-Admin-1.0.0.exe`

### Option B: Build JAR first, then package
```cmd
cd desktop-admin
mvn clean package -DskipTests
jpackage --type EXE --name RESIK-Admin --input target --main-jar resik-desktop-1.0.0.jar --main-class com.resik.admin.ResikDesktopApp --app-version 1.0.0 --vendor RESIK --win-menu --win-menu-group RESIK --win-shortcut --win-dir-chooser
```

### Option C: Run JAR directly (cross-platform)
```cmd
java -jar desktop-admin/target/resik-desktop-1.0.0.jar
```

## Notes
- The EXE installer creates a standalone Windows application with embedded JRE
- No need to install Java separately on target machine
- Default API URL: http://43.157.243.87:8081/api
