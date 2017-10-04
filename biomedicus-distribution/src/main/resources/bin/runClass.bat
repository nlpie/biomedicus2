@echo off

set LOCATION=%0

for %%I in (%LOCATION%) do set BIOMEDICUS_HOME=%%~dpI

:biomedicus_home_loop
for %%I in ("%BIOMEDICUS_HOME:~1,-1%") do set DIRNAME=%%~nxI
if not "%DIRNAME%" == "bin" (
  for %%I in ("%BIOMEDICUS_HOME%..") do set BIOMEDICUS_HOME=%%~dpfI
  goto biomedicus_home_loop
)
for %%I in ("%BIOMEDICUS_HOME%..") do set BIOMEDICUS_HOME=%%~dpfI

set BIOMEDICUS_CLASSPATH=!BIOMEDICUS_HOME!\lib\*

set BIOMEDICUS_LOG4J_CONF=!BIOMEDICUS_HOME!\logs\logging.xml

if defined JAVA_HOME (
  set JAVA="%JAVA_HOME%\bin\java.exe"
) else (
  for %%I in (java.exe) do set JAVA="%%~$PATH:I"
)

if not exist %JAVA% (
  echo could not find java; set JAVA_HOME or ensure java is in PATH 1>&2
  exit /b 1
)

%JAVA% "-Dbiomedicus.paths.home=%BIOMEDICUS_HOME%" -Xmx12g "-Dlog4j.configurationFile=%BIOMEDICUS_LOG4J_CONF%" -cp "%BIOMEDICUS_CLASSPATH%" %*
