@echo off
cmd /c "lein codox"
ftp -s:docs.ftp -i -v
