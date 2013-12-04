@echo off
dir /b /o /s *.java | find /c /v "" >allJavaFilesFileCount.txt
cls
