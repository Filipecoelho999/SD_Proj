@echo off
echo ================================
echo Building project...
echo ================================

:: Ir para a pasta deste ficheiro
cd /d %~dp0

:: Caminho completo para o diretório de saída
set OUT_DIR=C:\SD_proj\out\production\SD

echo Compilando ficheiros Java...

javac -d %OUT_DIR% ^
    ..\client\MainClient.java ^
    ..\server\AuthImpl.java ^
    ..\server\AuthRI.java ^
    ..\server\MainServer.java ^
    ..\model\UserStore.java

if %errorlevel% neq 0 (
    echo Erro ao compilar.
    pause
    exit /b
)

echo ================================
echo Iniciando rmiregistry...
echo ================================

:: Lançar rmiregistry no background do terminal (sem abrir nova janela)
cd /d %OUT_DIR%
start /b "C:\Program Files\Java\jdk-21\bin\rmiregistry.exe"


:: Espera 2 segundos (sem usar timeout)
ping 127.0.0.1 -n 3 > nul

echo ================================
echo Iniciando servidor RMI...
echo ================================

java edu.ufp.inf.sd.rmi.drive.server.MainServer
