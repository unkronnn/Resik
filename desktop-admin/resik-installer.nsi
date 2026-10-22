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
    ; JRE goes into $INSTDIR\jre (contents land directly there)
    SetOutPath "$INSTDIR\jre"
    File /r "jre-win\jre\*.*"

    ; App files go into $INSTDIR
    SetOutPath "$INSTDIR"
    File "target\resik-desktop-1.0.0.jar"
    File "src\main\resources\resik-icon.ico"

    ; Launcher batch file
    FileOpen $0 "$INSTDIR\resik.bat" w
    FileWrite $0 '@echo off$\r$\n'
    FileWrite $0 'cd /d "$INSTDIR"$\r$\n'
    FileWrite $0 'start "" "$INSTDIR\jre\bin\javaw.exe" -jar "$INSTDIR\resik-desktop-1.0.0.jar"$\r$\n'
    FileClose $0

    ; Shortcuts - icon MUST be .ico (PNG is not valid for shortcuts)
    CreateShortCut "$DESKTOP\Resik Admin.lnk" "$INSTDIR\jre\bin\javaw.exe" '-jar "$INSTDIR\resik-desktop-1.0.0.jar"' "$INSTDIR\resik-icon.ico"
    CreateDirectory "$SMPROGRAMS\Resik"
    CreateShortCut "$SMPROGRAMS\Resik\Resik Admin.lnk" "$INSTDIR\jre\bin\javaw.exe" '-jar "$INSTDIR\resik-desktop-1.0.0.jar"' "$INSTDIR\resik-icon.ico"
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
    Delete "$INSTDIR\resik.bat"
    Delete "$INSTDIR\resik-icon.ico"
    RMDir /r "$INSTDIR\jre"
    Delete "$INSTDIR\uninstall.exe"
    RMDir "$INSTDIR"

    Delete "$DESKTOP\Resik Admin.lnk"
    RMDir /r "$SMPROGRAMS\Resik"

    DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}"
SectionEnd
