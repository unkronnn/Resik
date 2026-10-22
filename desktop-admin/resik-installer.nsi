; Resik Admin Installer
!define APPNAME "Resik"
!define APPVERSION "1.0.0"

Name "${APPNAME} Admin"
OutFile "Resik-Setup.exe"
InstallDir "$PROGRAMFILES64\${APPNAME}"
RequestExecutionLevel admin
ShowInstDetails show
SetCompressor zlib

!include "MUI2.nsh"
!include "LogicLib.nsh"
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

Function .onInit
    nsExec::Exec 'taskkill /F /IM ResikAdmin.exe /T'
    nsExec::Exec 'taskkill /F /IM javaw.exe /T'
    Sleep 500
    ReadRegStr $R0 HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "UninstallString"
    ${If} $R0 != ""
        ExecWait '"$R0" /S _?=$INSTDIR'
    ${EndIf}
FunctionEnd

Section "Install"
    ; JRE
    SetOutPath "$INSTDIR\jre"
    File /r "jre-win\jre\*.*"

    ; JavaFX
    SetOutPath "$INSTDIR\lib"
    File /r "javafx-win\lib\*.*"
    SetOutPath "$INSTDIR\bin"
    File /r "javafx-win\bin\*.*"

    ; App
    SetOutPath "$INSTDIR"
    File "target\resik-desktop-1.0.0.jar"
    File "src\main\resources\resik-icon.ico"
    File "resik-launcher.vbs"

    ; Debug
    FileOpen $1 "$INSTDIR\resik-debug.bat" w
    FileWrite $1 '@echo off$\r$\n'
    FileWrite $1 'cd /d "%~dp0"$\r$\n'
    FileWrite $1 '"%~dp0jre\bin\java.exe" --module-path "%~dp0lib" --add-modules javafx.controls,javafx.fxml -cp "%~dp0resik-desktop-1.0.0.jar" com.resik.admin.ResikDesktopApp$\r$\n'
    FileWrite $1 'pause$\r$\n'
    FileClose $1

    ; Shortcut: wscript.exe runs VBS silently, WorkingDir set to INSTDIR
    CreateShortCut "$DESKTOP\Resik Admin.lnk" "wscript.exe" '"$INSTDIR\resik-launcher.vbs"' "$INSTDIR\resik-icon.ico"
    CreateDirectory "$SMPROGRAMS\Resik"
    CreateShortCut "$SMPROGRAMS\Resik\Resik Admin.lnk" "wscript.exe" '"$INSTDIR\resik-launcher.vbs"' "$INSTDIR\resik-icon.ico"
    CreateShortCut "$SMPROGRAMS\Resik\Resik Admin (Debug).lnk" "$INSTDIR\resik-debug.bat" "" "$INSTDIR\resik-icon.ico"
    CreateShortCut "$SMPROGRAMS\Resik\Uninstall Resik.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\resik-icon.ico"

    WriteUninstaller "$INSTDIR\uninstall.exe"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayName" "${APPNAME} Admin"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayVersion" "${APPVERSION}"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "Publisher" "Resik"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "UninstallString" "$INSTDIR\uninstall.exe"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayIcon" "$INSTDIR\resik-icon.ico"
SectionEnd

Section "Uninstall"
    Delete "$INSTDIR\resik-launcher.vbs"
    Delete "$INSTDIR\resik-desktop-1.0.0.jar"
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
