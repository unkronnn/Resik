' Resik Admin - silent launcher (no CMD window)
Option Explicit

Dim objShell, strDir, fso
Set objShell = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")

' Resolve install dir from this script's location
strDir = fso.GetParentFolderName(WScript.ScriptFullName)
If Right(strDir, 1) <> "\" Then strDir = strDir & "\"

objShell.CurrentDirectory = strDir

objShell.Run """" & strDir & "jre\bin\javaw.exe""" & _
    " --module-path """ & strDir & "lib""" & _
    " --add-modules javafx.controls,javafx.fxml" & _
    " -cp """ & strDir & "resik-desktop-1.0.0.jar""" & _
    " com.resik.admin.ResikDesktopApp", 0, False
