# Task Manager

REST API для управления задачами: регистрация и вход по JWT, CRUD задач с фильтрацией по статусу, автору и исполнителю.

Требования:

- **Java 21**
- **Docker** и **Docker Compose** — для вариантов с контейнерами
- **PostgreSQL 17** — только если база поднимается не в Docker
- Gradle Wrapper уже в репозитории (`gradlew` / `gradlew.bat`)

Схема БД создаётся при старте приложения через **Liquibase**.

По умолчанию приложение слушает порт **8080**.

---

## Запуск

### 1. Всё в Docker

Поднимает PostgreSQL и само приложение:

```bash
docker compose --profile full up -d --build
```

- Приложение: http://localhost:8080
- База внутри сети Compose, с хоста доступна на `localhost:5432`

### 2. База в Docker, приложение локально (для дебага)

Поднимает только PostgreSQL:

```bash
docker compose --profile db up -d
```

Параметры по умолчанию совпадают с `application.yaml`:

| Параметр | Значение |
|----------|----------|
| Хост / порт | `localhost:5432` |
| База | `task_manager` |
| Пользователь | `task_manager` |
| Пароль | `task_manager` |

Запуск приложения из консоли (из корня репозитория):

```bash
./gradlew bootRun
```

Windows:
```powershell
.\gradlew.bat bootRun
```

Либо запуск класса `com.gimax.taskmanager.TaskManagerApplication` из IDE.

### 3. Локальный PostgreSQL и запуск из консоли

Если Postgres установлен на машине (не в Docker):

1. Создать базу и пользователя:

```sql
CREATE USER task_manager WITH PASSWORD 'task_manager';
CREATE DATABASE task_manager OWNER task_manager;
GRANT ALL PRIVILEGES ON DATABASE task_manager TO task_manager;
```

2. Убедиться, что сервер слушает `localhost:5432` и принимает парольную аутентификацию для этого пользователя.

Linux / macOS:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/task_manager
export SPRING_DATASOURCE_USERNAME=task_manager
export SPRING_DATASOURCE_PASSWORD=task_manager
./gradlew bootRun
```

Windows (PowerShell):

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/task_manager"
$env:SPRING_DATASOURCE_USERNAME="task_manager"
$env:SPRING_DATASOURCE_PASSWORD="task_manager"
.\gradlew.bat bootRun
```

Если параметры совпадают с дефолтами из `application.yaml`, переменные можно не задавать — достаточно `./gradlew bootRun` / `.\gradlew.bat bootRun`.

Таблицы появятся автоматически при первом старте (Liquibase).

---

## Переменные окружения

| Переменная                                            | Назначение                       | По умолчанию |
|-------------------------------------------------------|----------------------------------|--------------|
| `SPRING_DATASOURCE_URL`                               | JDBC URL                         | `jdbc:postgresql://localhost:5432/task_manager` |
| `SPRING_DATASOURCE_USERNAME`                          | Пользователь БД                  | `task_manager` |
| `SPRING_DATASOURCE_PASSWORD`                          | Пароль БД                        | `task_manager` |
| `JWT_SECRET`                                          | Секрет подписи JWT               | значение из `application.yaml` |
| `JWT_EXPIRATION`                                      | Срок жизни токена, мс            | `86400000` (24 часа) |
| `APP_PORT`                                            | Внешний порт приложения в Docker | `8080` |
| `SERVER_PORT`                                         | Локальный порт приложения        | `8080` |
| `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` | Параметры контейнера Postgres    | `task_manager` |

---

## API

Актуальное описание эндпоинтов, схем запросов и ответов доступно в **Swagger UI** после запуска приложения:

- UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

В Swagger можно авторизоваться: кнопка **Authorize**, схема `bearerAuth`, вставить JWT без префикса `Bearer`.

Несколько примеров через `curl`:

### Регистрация

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"ivanov_ivan\",\"email\":\"ivanov@example.com\",\"password\":\"password123\"}"
```

### Вход (JWT)

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"ivanov_ivan\",\"password\":\"password123\"}"
```

В ответе поле `token`. Дальше данное значение нужно использовать вместо `$TOKEN`.

### Создать задачу

```bash
curl -s -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Разработать API\",\"description\":\"REST API для задач\",\"status\":\"TODO\",\"priority\":\"HIGH\"}"
```

### Получить задачу по id

```bash
curl -s http://localhost:8080/api/tasks/<TASK_ID> \
  -H "Authorization: Bearer $TOKEN"
```

### Список задач с фильтром и пагинацией

```bash
curl -s "http://localhost:8080/api/tasks?status=IN_PROGRESS&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

Другие фильтры: `authorId=<UUID>`, `assigneeId=<UUID>`.

### Обновить задачу

Изменять задачу может автор или пользователь с ролью `ADMIN`.

```bash
curl -s -X PUT http://localhost:8080/api/tasks/<TASK_ID> \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Разработать API\",\"description\":\"В работе\",\"status\":\"IN_PROGRESS\",\"priority\":\"MEDIUM\"}"
```

### Удалить задачу

```bash
curl -s -X DELETE http://localhost:8080/api/tasks/<TASK_ID> \
  -H "Authorization: Bearer $TOKEN"
```
