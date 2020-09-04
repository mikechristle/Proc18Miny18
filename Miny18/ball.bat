cls
del ..\class\*.class
javac -d %CPATH% -cp %CPATH% ..\src\Src.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\MError.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\Token.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\FileRef.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\ICode.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\Scanner.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\Node.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\BlockNode.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\StmtNode.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\Symbol.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\Func.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\Module.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\Util.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\Parser.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\Checker.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\FoldConst.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\ICodeGen.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\ICodeOpt.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\CallTree.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\AsmGen18Offsets.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\AsmGen18.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\Miny18.java
IF %ERRORLEVEL% NEQ 0  goto exit
javac -d %CPATH% -cp %CPATH% ..\src\Miny18Asm.java

:exit