# BLPS Pinterest API

REST API на Spring Boot для моделирования бизнес-процесса публикации пинов и формирования досок в стиле Pinterest.

## Возможности

- создание бизнес-пользователей;
- создание досок;
- изменение приватности доски: `PUBLIC` / `PRIVATE`;
- загрузка изображений в MinIO;
- создание пинов с изображением;
- сохранение существующего пина в доску;
- создание нового пина и одновременное сохранение его в доску;
- получение пользователей, досок, пинов и пинов конкретной доски;
- удаление доски вместе со связями с пинами;
- аутентификация через JAAS + XML;
- авторизация через JWT;
- разграничение доступа по ролям `USER` и `ADMIN`;
- управление транзакциями через Spring JTA + Atomikos.

## Стек

- Java 17
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Spring Security
- JAAS
- JWT
- PostgreSQL
- Flyway
- MinIO
- Atomikos / JTA
- Swagger / OpenAPI
- Gradle Kotlin DSL
- Docker Compose
- Lombok

## Архитектура

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

Файлы хранятся отдельно:

```text
Controller
    ↓
FileStorageService
    ↓
MinIO
```

В PostgreSQL сохраняются только метаданные изображения:

- `imageKey`;
- `imageUrl`.

Сам файл хранится в MinIO.

## Структура проекта

```text
src/main/java/.../controller   REST-контроллеры
src/main/java/.../service      бизнес-логика
src/main/java/.../repository   JPA-репозитории
src/main/java/.../entity       сущности БД
src/main/java/.../dto          DTO для API
src/main/java/.../mapper       мапперы
src/main/java/.../exception    обработка ошибок
src/main/java/.../security     Spring Security, JWT, JAAS
src/main/resources/db/migration Flyway-миграции
src/main/resources/security-users.xml security-пользователи
```

## Основные сущности

### User

Бизнес-пользователь приложения.

Поля:

- `id`
- `username`
- `email`

### Board

Доска пользователя.

Поля:

- `id`
- `name`
- `description`
- `privacy`
- `owner`
- `createdAt`

Значения `privacy`:

```text
PUBLIC
PRIVATE
```

### Pin

Пин с изображением.

Поля:

- `id`
- `title`
- `description`
- `imageKey`
- `imageUrl`
- `author`

### BoardPin

Связь между доской и пином.

```text
Board ↔ Pin
```

При удалении доски удаляются связи `BoardPin`, но сами `Pin` не удаляются.

## API

### Auth

```http
POST /api/auth/login
```

Пример запроса:

```json
{
  "username": "gleb",
  "password": "1234"
}
```

Пример ответа:

```json
{
  "token": "...",
  "username": "gleb",
  "roles": ["USER"]
}
```

Для защищённых запросов:

```http
Authorization: Bearer <token>
```

### Users

```http
POST /api/users
GET /api/users
GET /api/users/{id}
```

`POST /api/users` создаёт бизнес-пользователя в PostgreSQL. Это не security-регистрация.

### Boards

```http
POST /api/boards
GET /api/boards
GET /api/boards/{id}
GET /api/users/{userId}/boards
PATCH /api/boards/{id}/privacy
DELETE /api/boards/{id}
```

Изменение приватности:

```json
{
  "privacy": "PRIVATE"
}
```

### Files

```http
POST /api/files/upload
```

### Pins

```http
POST /api/pins/with-file
GET /api/pins
GET /api/pins/{id}
```

### Board-Pin workflow

```http
POST /api/boards/{boardId}/pins/{pinId}
GET /api/boards/{boardId}/pins
POST /api/boards/{boardId}/pins/with-file
```

## Безопасность

Security-пользователи хранятся в:

```text
src/main/resources/security-users.xml
```

Пример:

```xml
<users>
    <user>
        <username>gleb</username>
        <password>1234</password>
        <roles>
            <role>USER</role>
        </roles>
    </user>

    <user>
        <username>admin</username>
        <password>admin</password>
        <roles>
            <role>ADMIN</role>
        </roles>
    </user>
</users>
```

Роли:

- `ANONYMOUS` — пользователь без JWT;
- `USER` — обычный авторизованный пользователь;
- `ADMIN` — администратор.

Права:

| Действие | ANONYMOUS | USER | ADMIN |
|---|---:|---:|---:|
| Логин | Да | Да | Да |
| Просмотр PUBLIC-досок | Да | Да | Да |
| Просмотр своей PRIVATE-доски | Нет | Да | Да |
| Просмотр чужой PRIVATE-доски | Нет | Нет | Да |
| Создание доски | Нет | Да | Да |
| Создание пина | Нет | Да | Да |
| Изменение приватности доски | Нет | Только своей | Да |
| Удаление доски | Нет | Только своей | Да |
| Добавление пина в доску | Нет | Только в свою | Да |

## JWT и JAAS

Логин выполняется через JAAS:

```text
AuthController
    ↓
JaasAuthService
    ↓
LoginContext
    ↓
XmlLoginModule
    ↓
security-users.xml
```

После успешного логина сервер выдаёт JWT.

Дальше `JwtAuthFilter` проверяет токен в заголовке `Authorization` и кладёт пользователя в `SecurityContext`.

## Транзакции

В сервисном слое используются декларативные транзакции:

```java
@Transactional
```

Для чтения:

```java
@Transactional(readOnly = true)
```

В качестве JTA transaction manager используется Atomikos.

PostgreSQL подключён как XA datasource:

```yaml
spring:
  datasource:
    xa:
      data-source-class-name: org.postgresql.xa.PGXADataSource
```

Flyway использует отдельное обычное JDBC-подключение.

## Локальный запуск

### Требования

- Java 17
- Docker Desktop
- PostgreSQL
- Gradle Wrapper

### PostgreSQL

Создать базу данных, например:

```text
blps1
```

Настройки подключения находятся в:

```text
src/main/resources/application.yml
```

### MinIO

Запуск инфраструктуры:

```bash
docker-compose up -d
```

MinIO Console:

```text
http://localhost:9001
```

По умолчанию:

```text
login: minioadmin
password: minioadmin
```

Bucket:

```text
pins
```

### Backend

Linux/macOS:

```bash
./gradlew bootRun
```

Windows:

```powershell
.\gradlew.bat bootRun
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## Prod-запуск

Сборка jar:

```bash
./gradlew clean bootJar
```

Запуск с prod-профилем:

```bash
java -jar build/libs/blps1-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

Пример prod-порта:

```text
http://localhost:9177/swagger-ui.html
```

## Проверочный сценарий

1. Создать бизнес-пользователей `gleb` и `admin` через `POST /api/users`.
2. Выполнить логин `gleb` через `POST /api/auth/login`.
3. Выполнить логин `admin` через `POST /api/auth/login`.
4. Проверить, что `POST /api/boards` без токена возвращает `401`.
5. Создать доску с токеном `gleb`.
6. Изменить приватность доски на `PRIVATE`.
7. Проверить, что без токена `GET /api/boards/{id}` возвращает `403`.
8. Проверить, что владелец видит свою `PRIVATE`-доску.
9. Проверить, что `ADMIN` видит любую `PRIVATE`-доску.
10. Удалить доску и проверить, что связанные `BoardPin` удалены.