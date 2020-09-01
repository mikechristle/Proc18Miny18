echo off
cls
java -cp %CPATH% Miny18 -a %1 %2 %3 %4 %5
IF %ERRORLEVEL% NEQ 0  goto exit
java -cp %CPATH% Miny18Asm %1

:exit
