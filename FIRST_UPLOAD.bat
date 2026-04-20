@echo off
chcp 65001 >nul
echo ====================================
echo Первая загрузка проекта на GitHub
echo ====================================
echo.

echo Шаг 1: Инициализация Git...
git init

echo.
echo Шаг 2: Добавление ВСЕХ файлов...
git add -A

echo.
echo Шаг 3: Создание коммита...
git commit -m "Initial project upload"

echo.
echo Шаг 4: Подключение к GitHub...
git remote add origin https://github.com/forestikss/vibecoding.git

echo.
echo Шаг 5: Отправка на GitHub...
git branch -M main
git push -u origin main --force

echo.
echo ====================================
echo Готово! Все файлы загружены!
echo Теперь запустите 2_start_sync.bat
echo ====================================
echo.
echo Друзья должны:
echo 1. git clone https://github.com/forestikss/vibecoding.git
echo 2. Скопировать папку git-sync-tools в проект
echo 3. Запустить setup_user.bat
echo 4. Запустить 1_install.bat
echo 5. Запустить 2_start_sync.bat
echo.
pause
