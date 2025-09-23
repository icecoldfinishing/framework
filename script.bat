@echo off
setlocal

:: ============================================================================
:: Script pour compiler et déployer le projet Framework et Test-Project
:: SANS MAVEN.
:: ============================================================================

:: === Variables à adapter ===
set "FRAMEWORK_DIR=%~dp0"
set "TEST_PROJECT_DIR=%FRAMEWORK_DIR%test-project"
set "PROJECT_NAME=test-project"
set "TOMCAT_HOME=D:\WEB-TOOLS\apache-tomcat-10.1.28"
set "JAVA_HOME=C:\Program Files\Java\jdk-24"

:: === Variables calculées (ne pas modifier) ===
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "SERVLET_API_JAR=%TOMCAT_HOME%\lib\jakarta.servlet-api.jar"
set "BUILD_DIR=%FRAMEWORK_DIR%build"
set "FRAMEWORK_CLASSES_DIR=%BUILD_DIR%\framework-classes"
set "WEB_INF_LIB_DIR=%TEST_PROJECT_DIR%\src\main\webapp\WEB-INF\lib"
set "FRAMEWORK_JAR=%WEB_INF_LIB_DIR%\framework.jar"
set "TEST_CLASSES_DIR=%TEST_PROJECT_DIR%\src\main\webapp\WEB-INF\classes"
set "WAR_FILE_NAME=%PROJECT_NAME%.war"
set "DEPLOY_DIR=%TOMCAT_HOME%\webapps"

:: ============================================================================
:: 1. Nettoyage
:: ============================================================================
echo 🧹 Nettoyage des anciens builds...
if exist "%BUILD_DIR%" ( rd /s /q "%BUILD_DIR%" )
if exist "%WEB_INF_LIB_DIR%" ( rd /s /q "%WEB_INF_LIB_DIR%" )
if exist "%TEST_CLASSES_DIR%" ( rd /s /q "%TEST_CLASSES_DIR%" )
if exist "%DEPLOY_DIR%\%WAR_FILE_NAME%" ( del "%DEPLOY_DIR%\%WAR_FILE_NAME%" )
if exist "%DEPLOY_DIR%\%PROJECT_NAME%" ( rd /s /q "%DEPLOY_DIR%\%PROJECT_NAME%" )

mkdir "%BUILD_DIR%"
mkdir "%FRAMEWORK_CLASSES_DIR%"
mkdir "%TEST_CLASSES_DIR%"
mkdir "%WEB_INF_LIB_DIR%"

echo ✅ Nettoyage terminé.

:: ============================================================================
:: 2. Compilation du Framework
:: ============================================================================
echo ⚙️ Compilation du Framework...
javac -d "%FRAMEWORK_CLASSES_DIR%" -cp "%SERVLET_API_JAR%" "%FRAMEWORK_DIR%src\main\java\etu\sprint\framework\*.java" "%FRAMEWORK_DIR%src\main\java\etu\sprint\framework\annotation\*.java" "%FRAMEWORK_DIR%src\main\java\etu\sprint\framework\utility\*.java"
if %errorlevel% neq 0 (
    echo ❌ Erreur lors de la compilation du framework.
    exit /b 1
)
echo ✅ Framework compilé.

:: ============================================================================
:: 3. Création du JAR du Framework
:: ============================================================================
echo 📦 Création du fichier framework.jar...
jar -cvf "%FRAMEWORK_JAR%" -C "%FRAMEWORK_CLASSES_DIR%" .
if %errorlevel% neq 0 (
    echo ❌ Erreur lors de la création du JAR.
    exit /b 1
)
echo ✅ framework.jar créé dans WEB-INF/lib.

:: ============================================================================
:: 4. Compilation du Projet de Test
:: ============================================================================
echo ⚙️ Compilation du projet de test...
javac -d "%TEST_CLASSES_DIR%" -cp "%FRAMEWORK_JAR%;%SERVLET_API_JAR%" "%TEST_PROJECT_DIR%\src\main\java\com\example\controllers\*.java"
if %errorlevel% neq 0 (
    echo ❌ Erreur lors de la compilation du projet de test.
    exit /b 1
)
echo ✅ Projet de test compilé.

:: ============================================================================
:: 5. Création du WAR
:: ============================================================================
echo 📦 Création du fichier %WAR_FILE_NAME%...
cd "%TEST_PROJECT_DIR%\src\main\webapp"
jar -cvf "%DEPLOY_DIR%\%WAR_FILE_NAME%" .
cd "%FRAMEWORK_DIR%"
if %errorlevel% neq 0 (
    echo ❌ Erreur lors de la création du WAR.
    exit /b 1
)
echo ✅ Fichier WAR créé et copié dans Tomcat/webapps.

:: ============================================================================
:: 6. Redémarrage de Tomcat
:: ============================================================================
echo 🔄 Redémarrage de Tomcat...
call "%TOMCAT_HOME%\bin\shutdown.bat"
echo Attente de l'arrêt de Tomcat...
timeout /t 5 >nul
call "%TOMCAT_HOME%\bin\startup.bat"

echo ✅ Tomcat redémarré.
echo 🌐 Déploiement terminé. Accédez à : http://localhost:8080/%PROJECT_NAME%/hello

endlocal
pause