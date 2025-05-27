@echo off
echo [RabbitMQ] A compilar e correr o Consumer...

REM Caminho absoluto da pasta do projeto (ajusta se necessário)
set ROOT=C:\Users\Utilizador\Desktop\uni2\SDProj

REM Caminho para os JARs
set LIB=%ROOT%\lib

REM Caminho para o src
set SRC=%ROOT%\src

REM Compilar
javac -cp "%LIB%\amqp-client-5.24.0.jar;%LIB%\slf4j-api-1.7.30.jar;%LIB%\slf4j-simple-1.7.30.jar" ^
"%SRC%\edu\ufp\inf\sd\rmi\drive\rabbitmq\Consumer.java"

REM Executar
java -cp "%LIB%\amqp-client-5.24.0.jar;%LIB%\slf4j-api-1.7.30.jar;%LIB%\slf4j-simple-1.7.30.jar;%SRC%" ^
edu.ufp.inf.sd.rmi.drive.rabbitmq.Consumer

pause
