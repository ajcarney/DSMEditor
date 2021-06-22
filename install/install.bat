@ECHO off
:: get admin access
if not "%1"=="am_admin" (powershell start -verb runas '%0' am_admin & exit /b)

echo Starting Install
:: download javafx library
echo Downloading JavaFx Library...
curl http://download2.gluonhq.com/openjfx/11.0.1/openjfx-11.0.1_windows-x64_bin-sdk.zip -o %~dp0\openjfx-11.0.1_windows-x64_bin-sdk.zip

:: create .\lib folder
if not exist ".\lib" mkdir ".\lib"

:: unzip javafx library to .\lib folder
echo Extracting...
powershell.exe -nologo -noprofile -command "& { Add-Type -A 'System.IO.Compression.FileSystem'; [IO.Compression.ZipFile]::ExtractToDirectory('%~dp0\openjfx-11.0.1_windows-x64_bin-sdk.zip', '%~dp0\lib'); }"

if exist %~dp0\lib\javafx-sdk-11.0.1\ (  :: check to ensure library was downloaded correctly
    echo Successfully downloaded javafx library
) else (
    echo Failed to install javafx library
    echo Exiting
    Pause
    exit /b
)

:: find the version of java currently installed on the path
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVAVER=%%g
)

(echo %JAVAVER% | findstr /i /c:"16.0.1" > nul) && (set CORRECT_JAVA_VER=1)
if defined CORRECT_JAVA_VER (
    echo Correct Java version was found
) else (
    echo Correct Java version not found
    echo Downloading Java 16.0.1
    curl https://download.java.net/java/GA/jdk16.0.1/7147401fd7354114ac51ef3e1328291f/9/GPL/openjdk-16.0.1_windows-x64_bin.zip -o %~dp0\openjdk-16.0.1.zip

    if exist "C:\Program Files\Java\" (  :: check to see if a Java folder exists
        echo Creating directory \C:\Program Files\Java\
        mkdir "C:\Program Files\Java\"
    )

    echo Extracting...
    powershell.exe -nologo -noprofile -command "& { Add-Type -A 'System.IO.Compression.FileSystem'; [IO.Compression.ZipFile]::ExtractToDirectory('%~dp0\openjdk-16.0.1.zip', 'C:\Program Files\Java'); }"

    echo Setting JAVA_HOME to Java 16.0.1
    setx JAVA_HOME "C:\Program Files\Java\jdk-16.0.1\bin" /M

    echo Adding Java 16.0.1 to PATH
    setx PATH "C:\Program Files\Java\jdk-16.0.1\bin;%PATH%" /M

)

echo Creating batch file to run program
echo java -jrc --module-path ".\lib\javafx-sdk-11.0.1\lib" --add-modules=javafx.controls .\DSMEditor.jar> %~dp0\DSMEditor.bat

echo Cleaning up
if not defined CORRECT_JAVA_VER (
    del %~dp0\openjdk-16.0.1.zip
)
del %~dp0\openjfx-11.0.1_windows-x64_bin-sdk.zip

echo Install finished

Pause
