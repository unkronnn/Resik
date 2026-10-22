Set WshShell = CreateObject("WScript.Shell")
WshShell.CurrentDirectory = CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName)
WshShell.Run "jre\bin\javaw.exe --module-path lib --add-modules javafx.controls,javafx.fxml -cp resik-desktop-1.0.0.jar com.resik.admin.ResikDesktopApp", 1, False
