@echo off

setlocal enabledelayedexpansion
setlocal enableextensions

CALL "%~dp0runClass.bat" "org.apache.uima.tools.cpm.CpmFrame" || exit /b 1
