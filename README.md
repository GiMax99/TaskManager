## Вариант 1. Всё приложение в Docker

Поднимает PostgreSQL и приложение:

```powershell
docker compose --profile full up -d --build
```

Приложение: http://localhost:8080

## Вариант 2. Только PostgreSQL, приложение локально

Поднимает только БД:

```powershell
docker compose --profile db up -d
```

Запуск приложения из консоли:

```powershell
.\gradlew.bat bootRun
```

Или запустите `TaskManagerApplication` в IDEA. Приложение по умолчанию подключается к `localhost:5432/task_manager` (пользователь и пароль — `task_manager`).
