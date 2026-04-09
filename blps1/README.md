# BLPS Pinterest API

REST API-приложение на Spring Boot для моделирования бизнес-процесса публикации новых записей и формирования досок в стиле Pinterest.

## Что реализовано

Приложение позволяет:

- создавать пользователей
- создавать доски
- загружать изображения в MinIO
- создавать пины только с изображением
- сохранять существующий пин в доску
- создавать новый пин с файлом и сразу сохранять его в доску
- получать списки пользователей, досок и пинов
- получать пины конкретной доски

## Технологии

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- PostgreSQL
- Flyway
- MinIO
- Gradle
- Swagger / OpenAPI
- Insomnia

---

# Структура проекта

- `src/main/java/.../entity` — сущности БД
- `src/main/java/.../repository` — JPA-репозитории
- `src/main/java/.../dto` — DTO для API
- `src/main/java/.../mapper` — мапперы
- `src/main/java/.../service` — бизнес-логика
- `src/main/java/.../controller` — REST-контроллеры
- `src/main/java/.../exception` — обработка ошибок
- `src/main/resources/db/migration` — миграции Flyway
- `src/main/resources/application.yml` — конфигурация приложения

---

# Функциональность API

## Users
- `POST /api/users` — создать пользователя
- `GET /api/users` — получить всех пользователей
- `GET /api/users/{id}` — получить пользователя по id

## Boards
- `POST /api/boards` — создать доску
- `GET /api/boards` — получить все доски
- `GET /api/boards/{id}` — получить доску по id
- `GET /api/users/{userId}/boards` — получить доски пользователя

## Files
- `POST /api/files/upload` — загрузить изображение в MinIO

## Pins
- `POST /api/pins/with-file` — создать пин с изображением
- `GET /api/pins` — получить все пины
- `GET /api/pins/{id}` — получить пин по id

## Board-Pin workflow
- `POST /api/boards/{boardId}/pins/{pinId}` — сохранить существующий пин в доску
- `GET /api/boards/{boardId}/pins` — получить все пины доски
- `POST /api/boards/{boardId}/pins/with-file` — создать новый пин с файлом и сразу сохранить в доску

---

# Локальный запуск

## 1. Требования

Перед запуском должны быть установлены:

- Java 17
- Gradle или Gradle Wrapper
- PostgreSQL
- Docker Desktop
- Insomnia (для тестирования API)

---

## 2. Настройка PostgreSQL

PostgreSQL используется локально.

Нужно создать базу данных, например:

- `blps1`

Также нужно знать:
- имя пользователя PostgreSQL
- пароль PostgreSQL
- порт PostgreSQL

### Где указывать данные PostgreSQL

Файл:

- `src/main/resources/application.yml`

---

## 3. Minio запускается локально через Docker

В корне проекта
```bash
docker-compose up -d
```

---

## 4. Запуск серверной части

В корне проекта
```bash
./gradlew bootRun
```

---

## 5. URL

- `http://localhost:9001` - MinIO
- `http://localhost:8080/swagger-ui/index.html` - Swagger
