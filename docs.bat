@echo off
echo Generating documentation...
cmd /c "lein codox"

set upl=y
set /p upl="Upload? (Y/n) "

set uplb=1
if %upl:~0,1%==n ( set uplb=0 )
if %upl:~0,1%==N ( set uplb=0 )

if %uplb%==1 ( ftp -s:docs.ftp -i -v )
