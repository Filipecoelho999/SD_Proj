@echo off
echo ================================
echo Compilacao RabbitMQ
echo ================================

:: Caminho de saída
set OUT_DIR=C:\SD_proj\out\production\SD

:: Compilar as classes do RabbitMQ (Publisher e Subscriber)
javac -encoding UTF-8 ^
 -cp ".;C:\Users\Utilizador\Desktop\uni2\SDProj\lib\amqp-client-5.24.0.jar;C:\Users\Utilizador\Desktop\uni2\SDProj\lib\slf4j-api-1.7.30.jar;C:\Users\Utilizador\Desktop\uni2\SDProj\lib\slf4j-simple-1.7.30.jar" ^
 -d %OUT_DIR% ^
    ..\drive\SubscribeClient.java ^
    ..\drive\PublishClient.java ^
    ..\util\RabbitUtils.java
    ..\test\MainClientRabbitOnly.java

if %errorlevel% neq 0 (
    echo Erro ao compilar RabbitMQ.
    pause
    exit /b
)

echo RabbitMQ compilado com sucesso!
pause
