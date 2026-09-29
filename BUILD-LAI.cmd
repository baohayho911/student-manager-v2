@echo off
setlocal
cd /d "%~dp0frontend"
call npm.cmd ci
if errorlevel 1 goto fail
call npm.cmd run build
if errorlevel 1 goto fail
if exist "..\backend\src\main\resources\static" rmdir /s /q "..\backend\src\main\resources\static"
xcopy "dist\*" "..\backend\src\main\resources\static\" /e /i /y >nul
cd /d "%~dp0backend"
call mvnw.cmd clean verify
if errorlevel 1 goto fail
if not exist "..\release" mkdir "..\release"
copy /y "target\student-manager-2.0.0.jar" "..\release\student-manager-2.0.0.jar" >nul
echo Build thanh cong. Chay CHAY-UNG-DUNG.cmd de mo ban moi.
pause
exit /b 0
:fail
echo Build that bai. Giu lai thong bao loi de kiem tra, khong bo qua test.
pause
exit /b 1
