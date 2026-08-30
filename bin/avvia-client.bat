@echo off
title TheKnife - Client
cd /d "%~dp0"
java -jar theknife-client-1.0.jar
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERRORE] Il client si e' fermato con codice %ERRORLEVEL%.
    pause
)