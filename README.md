# Git Sync Tools - Совместное кодирование

Инструменты для автоматической синхронизации кода через GitHub.

## ДЛЯ ТЕБЯ (Создатель проекта)

### Первая настройка:

1. Скопируй папку `git-sync-tools` в корень твоего проекта
2. Зайди в папку проекта через командную строку
3. Запусти `setup_user.bat` - введи свое имя и email
4. Запусти `1_install.bat` - установка зависимостей Python
5. Создай репозиторий на GitHub: https://github.com/forestikss/vibecoding
6. Запусти `FIRST_UPLOAD.bat` - загрузит ВСЕ файлы на GitHub
7. Добавь друзей как коллабораторов:
   - Зайди на https://github.com/forestikss/vibecoding/settings/access
   - Нажми "Add people"
   - Введи их GitHub username
   - Они должны принять приглашение

### Каждый раз когда кодишь:

1. Запусти `2_start_sync.bat` (не закрывай окно!)
2. Введи `.` когда спросит путь
3. Открой проект в IntelliJ IDEA
4. Коди! Изменения синхронизируются каждые 3 секунды

---

## ДЛЯ ДРУЗЕЙ (Подключаются к проекту)

### Первая настройка:

1. Установи Git: https://git-scm.com/download/win
2. Установи Python: https://www.python.org/downloads/
3. Прими приглашение на GitHub (проверь email или уведомления)
4. Открой командную строку и введи:

```bash
cd D:\
git clone https://github.com/forestikss/vibecoding.git
cd vibecoding
```

5. Скопируй папку `git-sync-tools` в склонированный проект (попроси у создателя)
6. Запусти `setup_user.bat` - введи свое имя и email
7. Запусти `1_install.bat` - установка зависимостей
8. Запусти команду для настройки ветки:

```bash
git branch --set-upstream-to=origin/main main
```

### Каждый раз когда кодишь:

1. Запусти `2_start_sync.bat` (не закрывай окно!)
2. Введи `.` когда спросит путь
3. Открой папку `D:\vibecoding` в IntelliJ IDEA
4. Коди! Изменения синхронизируются каждые 3 секунды

---

## Важные правила

- Все должны держать `2_start_sync.bat` запущенным во время работы
- Не редактируйте один файл одновременно (будут конфликты)
- Синхронизация происходит каждые 3 секунды автоматически
- Если возникли проблемы - смотри логи в окне `2_start_sync.bat`

---

## Файлы в папке

- `FIRST_UPLOAD.bat` - первая загрузка проекта (только для создателя)
- `setup_user.bat` - настройка имени пользователя Git (один раз)
- `1_install.bat` - установка зависимостей Python (один раз)
- `2_start_sync.bat` - запуск синхронизации (каждый раз!)
- `auto_sync.py` - скрипт автосинхронизации
- `requirements.txt` - список зависимостей Python

---

## Решение проблем

### Ошибка "Permission denied"
- Создатель не добавил тебя как коллаборатора
- Или ты не принял приглашение на GitHub

### Ошибка "There is no tracking information"
Выполни в командной строке:
```bash
git branch --set-upstream-to=origin/main main
```

### Ошибка "You are not currently on a branch"
Выполни в командной строке:
```bash
git checkout main
git pull
```

### Файлы не синхронизируются
- Проверь что `2_start_sync.bat` запущен у всех
- Проверь логи в окне батника
- Перезапусти батник

---

## Ссылки

- Репозиторий: https://github.com/forestikss/vibecoding
- Добавить коллабораторов: https://github.com/forestikss/vibecoding/settings/access
