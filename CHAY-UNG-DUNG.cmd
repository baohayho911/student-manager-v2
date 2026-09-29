@echo off
setlocal
cd /d "%~dp0backend"
where java >nul 2>&1
if errorlevel 1 (
 echo Khong tim thay Java. Hay cai JDK 21 va mo lai cua so lenh.
 pause
 exit /b 1
)
if not exist "..\release\student-manager-2.0.0.jar" (
 echo Chua co file JAR. Hay chay BUILD-LAI.cmd truoc.
 pause
 exit /b 1
)
echo UniDesk - Student Manager 2.0
echo Sau khi thay Started Application, mo http://localhost:8081
echo Giu cua so nay mo. Dung ung dung bang Ctrl+C.
echo Du lieu duoc luu tai backend\data.
java -jar "..\release\student-manager-2.0.0.jar" --spring.profiles.active=demo --server.port=8081
pause
