@echo off
title TheKnife - Server
cd /d "%~dp0"
java -jar theknife-server-1.0.jar
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERRORE] Il server si e' fermato con codice %ERRORLEVEL%.
    pause
)