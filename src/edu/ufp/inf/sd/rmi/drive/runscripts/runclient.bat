@echo off
echo ========================================
echo Running Distributed Drive Client...
echo ========================================

cd /d "%~dp0\..\out\production\SD"
java edu.ufp.inf.sd.rmi.drive.client.DriveClient
pause
