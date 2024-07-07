@echo off
mkdir js
cd "../../js"
call build.bat
xcopy /s /i /y "out" "../resources/res/js"
cd "../resources/res"