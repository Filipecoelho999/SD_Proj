@echo off
echo ================================
echo Building project...
echo ================================

:: Ir para o diretório onde o script está localizado
cd /d "%~dp0"

:: Caminho completo para o diretório de saída
set "OUT_DIR=%~dp0..\out\production\SD"

echo Compilando todos os ficheiros Java...

javac -encoding UTF-8 ^
 -cp ".;..\lib\amqp-client-5.24.0.jar;..\lib\slf4j-api-1.7.30.jar;..\lib\slf4j-simple-1.7.30.jar" ^
 -d "%OUT_DIR%" ^
 ..\client\*.java ^
 ..\server\*.java

if %errorlevel% neq 0 (
    echo Erro ao compilar.
    pause
    exit /b
)

echo ================================
echo Iniciando rmiregistry...
echo ================================

cd /d "%OUT_DIR%"
start /b "C:\Program Files\Java\jdk-21\bin\rmiregistry.exe"

ping 127.0.0.1 -n 3 > nul

echo ================================
echo Iniciando servidor RMI...
echo ================================

java edu.ufp.inf.sd.rmi.drive.server.MainServer
