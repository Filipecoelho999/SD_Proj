@echo off
echo Iniciando o cliente...

REM Define o caminho para os ficheiros .class compilados
set CLASSPATH=C:\SD_proj\out\production\SD

REM Executa a classe principal do cliente
java -cp "%CLASSPATH%" edu.ufp.inf.sd.rmi.drive.client.MainClient

pause

