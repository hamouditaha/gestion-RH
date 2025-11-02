@echo off
echo ====================================
echo  Démarrage Gestion Presence
echo ====================================

echo Vérification de Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo  Java n'est pas installé ou non configuré
    pause
    exit /b 1
)

echo Vérification de Maven...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo  Maven n'est pas installé ou non configuré
    pause
    exit /b 1
)

echo  Environnement vérifié
echo  Démarrage de l'application...

cd /d "C:\Users\lenovo\Desktop\gestion-presence\backend"
call mvn clean spring-boot:run

pause