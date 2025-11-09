@echo off
setlocal enabledelayedexpansion

:: ============================================================================
:: Script pour compiler le Framework et generer le JAR
:: ============================================================================

:: === Variables à adapter ===
set "FRAMEWORK_DIR=%~dp0"
if "%FRAMEWORK_DIR:~-1%"=="\" set "FRAMEWORK_DIR=%FRAMEWORK_DIR:~0,-1%"
set "TEST_PROJECT_DIR=D:\Framework\test-project"
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "TOMCAT_HOME=D:\Progtool\apache-tomcat-10.1.28"

:: === Variables calculées (ne pas modifier) ===
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "BUILD_DIR=%FRAMEWORK_DIR%\build"
set "FRAMEWORK_CLASSES_DIR=%BUILD_DIR%\framework-classes"
set "WEB_INF_LIB_DIR=%TEST_PROJECT_DIR%\src\main\webapp\WEB-INF\lib"
set "FRAMEWORK_JAR=%WEB_INF_LIB_DIR%\framework.jar"

:: Construction du Classpath pour la compilation du framework
set "FULL_CLASSPATH="
for %%j in ("%TOMCAT_HOME%\lib\*.jar") do (
    set "FULL_CLASSPATH=!FULL_CLASSPATH!;%%j"
)

:: ============================================================================
:: 1. Nettoyage
:: ============================================================================
echo Nettoyage des anciens builds du framework...
if exist "%BUILD_DIR%" (
    rd /s /q "%BUILD_DIR%"
)
if exist "%FRAMEWORK_JAR%" (
    del /f /q "%FRAMEWORK_JAR%"
)

mkdir "%BUILD_DIR%"
mkdir "%FRAMEWORK_CLASSES_DIR%"
if not exist "%WEB_INF_LIB_DIR%" (
    mkdir "%WEB_INF_LIB_DIR%"
)

echo Nettoyage termine.

:: ============================================================================
:: 2. Compilation du Framework
:: ============================================================================
echo Compilation du Framework...
set "FRAMEWORK_SOURCES_FILE=%BUILD_DIR%\framework_sources.txt"
dir /s /b "%FRAMEWORK_DIR%\src\main\java\*.java" > "%FRAMEWORK_SOURCES_FILE%"
javac -d "%FRAMEWORK_CLASSES_DIR%" -cp "!FULL_CLASSPATH!" @"%FRAMEWORK_SOURCES_FILE%"
if %errorlevel% neq 0 (
    echo Erreur lors de la compilation du framework.
    pause
    exit /b 1
)
echo Framework compile.

:: ============================================================================
:: 3. Création du JAR du Framework
:: ============================================================================
echo Creation du fichier framework.jar...
jar -c -f "%FRAMEWORK_JAR%" -C "%FRAMEWORK_CLASSES_DIR%" .
if %errorlevel% neq 0 (
    echo Erreur lors de la creation du JAR.
    pause
    exit /b 1
)
echo framework.jar cree et copie dans %WEB_INF_LIB_DIR%

endlocal
pause
