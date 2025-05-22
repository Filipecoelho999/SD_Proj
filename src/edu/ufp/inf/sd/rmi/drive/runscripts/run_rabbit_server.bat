@echo off
echo ================================
echo [RabbitMQ] Building project...
echo ================================

cd /d %~dp0

set OUT_DIR=C:\Users\Utilizador\Desktop\uni2\SDProj\out\production\SD

javac -encoding UTF-8 ^
 -cp ".;C:\Users\Utilizador\Desktop\uni2\SDProj\lib\amqp-client-5.24.0.jar;C:\Users\Utilizador\Desktop\uni2\SDProj\lib\slf4j-api-1.7.30.jar;C:\Users\Utilizador\Desktop\uni2\SDProj\lib\slf4j-simple-1.7.30.jar" ^
 -d %OUT_DIR% ^
 ..\client\*.java ^
 ..\server\*.java ^
 ..\util\*.java ^
 ..\session\*.java ^
 ..\rabbitmq\*.java

if %errorlevel% neq 0 (
    echo Erro ao compilar.
    pause
    exit /b
)

echo ================================
echo [RabbitMQ] Iniciando servidor...
echo ================================

cd /d %OUT_DIR%

java -Dmodo=rabbitmq -cp ".;%OUT_DIR%;C:\Users\Utilizador\Desktop\uni2\SDProj\lib\amqp-client-5.24.0.jar;C:\Users\Utilizador\Desktop\uni2\SDProj\lib\slf4j-api-1.7.30.jar;C:\Users\Utilizador\Desktop\uni2\SDProj\lib\slf4j-simple-1.7.30.jar" edu.ufp.inf.sd.rmi.drive.server.MainServer
