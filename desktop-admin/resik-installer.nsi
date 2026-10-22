; Resik Admin Installer - NSIS Script
; Produces a single EXE that bundles JRE + JAR

!define APPNAME "Resik"
!define APPVERSION "1.0.0"
!define COMPANYNAME "Resik"

Name "${APPNAME}"
OutFile "Resik-Setup.exe"
InstallDir "$PROGRAMFILES64\${APPNAME}"
RequestExecutionLevel admin
ShowInstDetails show

# Include UI
!include "MUI2.nsh"
!define MUI_ICON "src\main\resources\resik-icon.ico"
!define MUI_UNICON "src\main\resources\resik-icon.ico"

# Pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

# Languages
!insertmacro MUI_LANGUAGE "English"

Section "Install"
    SetOutPath "$INSTDIR"
    
    ; Copy JAR
    File "target\resik-desktop-1.0.0.jar"
    
    ; Copy JRE (portable)
    File /r "jre17\*.*"
    
    ; Copy icon
    File "src\main\resources\resik-logo.png"
    File "src\main\resources\resik-logo-large.png"
    
    ; Create launcher batch file
    FileOpen $0 "$INSTDIR\resik.bat" w
    FileWrite $0 '@echo off$\r$\n'
    FileWrite $0 'cd /d "$INSTDIR"$\r$\n'
    FileWrite $0 'start "" "jre\bin\javaw.exe" -jar resik-desktop-1.0.0.jar$\r$\n'
    FileClose $0
    
    ; Create shortcut with icon
    CreateShortCut "$DESKTOP\Resik Admin.lnk" "$INSTDIR\jre\bin\javaw.exe" '-jar "$INSTDIR\resik-desktop-1.0.0.jar"' "$INSTDIR\resik-logo.png"
    
    ; Start menu shortcut
    CreateDirectory "$SMPROGRAMS\Resik"
    CreateShortCut "$SMPROGRAMS\Resik\Resik Admin.lnk" "$INSTDIR\jre\bin\javaw.exe" '-jar "$INSTDIR\resik-desktop-1.0.0.jar"' "$INSTDIR\resik-logo.png"
    CreateShortCut "$SMPROGRAMS\Resik\Uninstall Resik.lnk" "$INSTDIR\uninstall.exe" "" ""
    
    ; Write uninstaller
    WriteUninstaller "$INSTDIR\uninstall.exe"
    
    ; Registry entry for Add/Remove Programs
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayName" "${APPNAME} Admin"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayVersion" "${APPVERSION}"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "Publisher" "${COMPANYNAME}"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "UninstallString" "$INSTDIR\uninstall.exe"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "InstallLocation" "$INSTDIR"
SectionEnd

Section "Uninstall"
    Delete "$INSTDIR\resik-desktop-1.0.0.jar"
    Delete "$INSTDIR\resik.bat"
    Delete "$INSTDIR\resik-logo.png"
    Delete "$INSTDIR\resik-logo-large.png"
    RMDir /r "$INSTDIR\jre"
    Delete "$INSTDIR\uninstall.exe"
    RMDir "$INSTDIR"
    
    Delete "$DESKTOP\Resik Admin.lnk"
    RMDir /r "$SMPROGRAMS\Resik"
    
    DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}"
SectionEnd
