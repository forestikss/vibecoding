@echo off
chcp 65001 >nul
echo ====================================
echo Первая загрузка проекта на GitHub
echo ====================================
echo.

echo Шаг 1: Инициализация Git...
git init

echo.
echo Шаг 2: Проверка файлов...
git status

echo.
echo Шаг 3: Добавление ВСЕХ файлов...
git add -A -v

echo.
echo Шаг 4: Создание коммита...
git commit -m "Initial project upload" -v

echo.
echo Шаг 5: Подключение к GitHub...
git remote add origin https://github.com/forestikss/vibecoding.git

echo.
echo Шаг 6: Отправка на GitHub...
git branch -M main
git push -u origin main --force --verbose

echo.
echo ====================================
echo Готово!
echo ====================================
pause
