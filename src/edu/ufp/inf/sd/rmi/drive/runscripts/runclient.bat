@echo off
echo ========================================
echo Running Distributed Drive Client...
echo ========================================

cd /d "%~dp0\..\out\production\SD"

java -cp ".;C:\Users\Utilizador\Desktop\uni2\SDProj\out\production\SD;C:\Users\Utilizador\Desktop\uni2\SDProj\lib\amqp-client-5.24.0.jar;C:\Users\Utilizador\Desktop\uni2\SDProj\lib\slf4j-api-1.7.30.jar;C:\Users\Utilizador\Desktop\uni2\SDProj\lib\slf4j-simple-1.7.30.jar" edu.ufp.inf.sd.rmi.drive.client.DriveClient
pause
