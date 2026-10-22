; Resik Admin Installer - NSIS Script
; Bundles JRE + JAR so users need NO Java installed

!define APPNAME "Resik"
!define APPVERSION "1.0.0"
!define COMPANYNAME "Resik"

Name "${APPNAME} Admin"
OutFile "Resik-Setup.exe"
InstallDir "$PROGRAMFILES64\${APPNAME}"
RequestExecutionLevel admin
ShowInstDetails show
SetCompressor zlib

!include "MUI2.nsh"
!define MUI_ICON "src\main\resources\resik-icon.ico"
!define MUI_UNICON "src\main\resources\resik-icon.ico"

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_LANGUAGE "English"

Section "Install"
    ; JRE
    SetOutPath "$INSTDIR\jre"
    File /r "jre-win\jre\*.*"

    ; JavaFX Windows SDK
    SetOutPath "$INSTDIR\lib"
    File /r "javafx-win\lib\*.*"
    SetOutPath "$INSTDIR\bin"
    File /r "javafx-win\bin\*.*"

    ; App files
    SetOutPath "$INSTDIR"
    File "target\resik-desktop-1.0.0.jar"
    File "src\main\resources\resik-icon.ico"

    ; Launcher VBS — bundled file (no CMD window), NOT generated (avoids escaping bugs)
    File "resik-launcher.vbs"
    Rename "$INSTDIR\resik-launcher.vbs" "$INSTDIR\resik.vbs"

    ; Debug batch — shows console with errors
    FileOpen $1 "$INSTDIR\resik-debug.bat" w
    FileWrite $1 '@echo off$\r$\n'
    FileWrite $1 'set APPDIR=%~dp0$\r$\n'
    FileWrite $1 'cd /d "%APPDIR%"$\r$\n'
    FileWrite $1 '"%APPDIR%jre\bin\java.exe" --module-path "%APPDIR%lib" --add-modules javafx.controls,javafx.fxml -cp "%APPDIR%resik-desktop-1.0.0.jar" com.resik.admin.ResikDesktopApp$\r$\n'
    FileWrite $1 'pause$\r$\n'
    FileClose $1

    ; Shortcuts — VBS launcher (no CMD window)
    CreateShortCut "$DESKTOP\Resik Admin.lnk" "wscript.exe" '"$INSTDIR\resik.vbs"' "$INSTDIR\resik-icon.ico"
    CreateDirectory "$SMPROGRAMS\Resik"
    CreateShortCut "$SMPROGRAMS\Resik\Resik Admin.lnk" "wscript.exe" '"$INSTDIR\resik.vbs"' "$INSTDIR\resik-icon.ico"
    CreateShortCut "$SMPROGRAMS\Resik\Resik Admin (Debug).lnk" "$INSTDIR\resik-debug.bat" "" "$INSTDIR\resik-icon.ico"
    CreateShortCut "$SMPROGRAMS\Resik\Uninstall Resik.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\resik-icon.ico"

    WriteUninstaller "$INSTDIR\uninstall.exe"

    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayName" "${APPNAME} Admin"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayVersion" "${APPVERSION}"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "Publisher" "${COMPANYNAME}"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "UninstallString" "$INSTDIR\uninstall.exe"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "InstallLocation" "$INSTDIR"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayIcon" "$INSTDIR\resik-icon.ico"
SectionEnd

Section "Uninstall"
    Delete "$INSTDIR\resik-desktop-1.0.0.jar"
    Delete "$INSTDIR\resik.vbs"
    Delete "$INSTDIR\resik-debug.bat"
    Delete "$INSTDIR\resik-icon.ico"
    RMDir /r "$INSTDIR\jre"
    RMDir /r "$INSTDIR\lib"
    RMDir /r "$INSTDIR\bin"
    Delete "$INSTDIR\uninstall.exe"
    RMDir "$INSTDIR"

    Delete "$DESKTOP\Resik Admin.lnk"
    RMDir /r "$SMPROGRAMS\Resik"

    DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}"
SectionEnd
