@echo off
echo ================================
echo Building project...
echo ================================

:: Ir para a pasta deste ficheiro
cd /d %~dp0

:: Caminho completo para o diretório de saída
set OUT_DIR=C:\Users\Utilizador\Desktop\uni2\SDProj\out\production\SD

echo Compilando todos os ficheiros Java...

javac -encoding UTF-8 ^
 -cp ".;C:\Users\Utilizador\Desktop\uni2\SDProj\lib\amqp-client-5.24.0.jar;C:\Users\Utilizador\Desktop\uni2\SDProj\lib\slf4j-api-1.7.30.jar;C:\Users\Utilizador\Desktop\uni2\SDProj\lib\slf4j-simple-1.7.30.jar" ^
 -d %OUT_DIR% ^
 ..\client\*.java ^
 ..\server\*.java ^
 ..\rabbitmq\*.java

if %errorlevel% neq 0 (
    echo Erro ao compilar.
    pause
    exit /b
)

echo ================================
echo Iniciando rmiregistry...
echo ================================

cd /d %OUT_DIR%
start /b "C:\Program Files\Java\jdk-21\bin\rmiregistry.exe"

ping 127.0.0.1 -n 3 > nul

echo ================================
echo Iniciando servidor RMI...
echo ================================

java -cp ".;C:\Users\Utilizador\Desktop\uni2\SDProj\out\production\SD;C:\Users\Utilizador\Desktop\uni2\SDProj\lib\amqp-client-5.24.0.jar;C:\Users\Utilizador\Desktop\uni2\SDProj\lib\slf4j-api-1.7.30.jar;C:\Users\Utilizador\Desktop\uni2\SDProj\lib\slf4j-simple-1.7.30.jar" edu.ufp.inf.sd.rmi.drive.server.MainServer
