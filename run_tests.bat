@echo off
REM Compila ed esegue i test automatici di ArrowClock in una cartella temporanea.
REM Uso:  run_tests.bat      (richiede solo un JDK 17+ nel PATH)
setlocal
set DIR=%~dp0
set OUT=%TEMP%\arrowclock_test
if exist "%OUT%" rmdir /s /q "%OUT%"
mkdir "%OUT%"

javac -d "%OUT%" "%DIR%Codes\*.java" "%DIR%Tests\*.java"
if errorlevel 1 goto fine

pushd "%OUT%"
java EsecutoreTest
set CODE=%ERRORLEVEL%
popd

:fine
rmdir /s /q "%OUT%" 2>nul
exit /b %CODE%
