# Модель данных и ER-диаграмма базы данных Klawa (с Keycloak IDP)

Данный документ содержит полную спецификацию схемы базы данных, ER-диаграмму (Entity-Relationship) и аудит соответствия между JPA Entity (Java) и миграцией Flyway (`V0__init_tables.sql`) с учётом интеграции **Keycloak** в качестве внешнего Identity & Access Management (IAM / OIDC).

---

## 1. Архитектурные изменения после перехода на Keycloak

1. **Сервер авторизации Keycloak**:
   - Хранит учетные данные пользователей, хэши паролей (Argon2 / PBKDF2), управляет MFA/2FA.
   - Выпускает и ротирует JWT Access и Refresh токены.
   - Управляет ролями (`ROLE_USER`, `ROLE_ADMIN`), которые передаются внутри JWT токена в claim `realm_access.roles`.
2. **Таблица `refresh_token`**: **ПОЛНОСТЬЮ УДАЛЕНА** из БД Klawa. Управление сессиями и токенами теперь на стороне Keycloak.
3. **Таблица `user_profile` (ранее `_user`)**:
   - Первичный ключ `id UUID PRIMARY KEY` — совпадает с `sub` (UUID пользователя) из JWT токена Keycloak.
   - Поля `password` и `roles` удалены из базы Klawa.
   - Создаётся и наполняется локальный профиль (Just-In-Time provisioning) при первой авторизации пользователя.
4. **Внешние ключи**:
   - Во всех связанных таблицах (`project`, `task`, `reminder`, `notification`, `conversation`, `memory_entry`, `telegram_account`) колонка `user_id` переведена с `BIGINT` на `UUID REFERENCES user_profile(id) ON DELETE CASCADE`.

---

## 2. ER-диаграмма базы данных (Mermaid)

```mermaid
erDiagram
    KEYCLOAK_IAM ||--|| USER_PROFILE : "sub (UUID)"

    USER_PROFILE ||--o{ PROJECT : "owns"
    USER_PROFILE ||--o{ TASK : "owns"
    USER_PROFILE ||--o{ REMINDER : "receives"
    USER_PROFILE ||--o{ NOTIFICATION : "receives"
    USER_PROFILE ||--o{ CONVERSATION : "starts"
    USER_PROFILE ||--o{ MEMORY_ENTRY : "has"
    USER_PROFILE ||--o| TELEGRAM_ACCOUNT : "links (1-to-1)"

    PROJECT ||--o{ TASK : "contains"
    TASK ||--o{ REMINDER : "triggers"
    CONVERSATION ||--o{ MESSAGE : "contains"

    USER_PROFILE {
        uuid id PK "sub из JWT Keycloak"
        varchar_255 email UK
        varchar_100 first_name
        varchar_100 last_name
        boolean is_active
        timestamptz created_at
        timestamptz updated_at
        timestamptz deleted_at "Soft Delete"
    }

    PROJECT {
        bigserial id PK
        varchar_255 name
        text description
        uuid user_id FK
        timestamptz created_at
        timestamptz updated_at
        timestamptz deleted_at "Soft Delete"
    }

    TASK {
        bigserial id PK
        bigint project_id FK
        uuid user_id FK
        varchar_255 title
        text description
        task_status status "OPEN, IN_PROGRESS, COMPLETED, ARCHIVED"
        task_priority priority "LOW, MEDIUM, HIGH, URGENT"
        timestamptz due_date
        timestamptz completed_at
        timestamptz created_at
        timestamptz updated_at
        timestamptz deleted_at "Soft Delete"
    }

    REMINDER {
        bigserial id PK
        uuid user_id FK
        bigint task_id FK "nullable"
        timestamptz trigger_time
        text message
        varchar_20 status "PENDING, SENT, FAILED"
        timestamptz last_notified_at
        timestamptz created_at
        timestamptz updated_at
    }

    NOTIFICATION {
        bigserial id PK
        uuid user_id FK
        notification_type type "TASK_REMINDER, AI_MESSAGE, SYSTEM_ALERT"
        text message
        platform_type platform "APP, TELEGRAM, EMAIL"
        boolean is_read
        timestamptz read_at
        timestamptz created_at
        timestamptz updated_at
        timestamptz deleted_at
    }

    CONVERSATION {
        uuid id PK "gen_random_uuid()"
        uuid user_id FK
        varchar_255 title
        timestamptz created_at
        timestamptz updated_at
        timestamptz deleted_at
    }

    MESSAGE {
        uuid id PK "gen_random_uuid()"
        uuid conversation_id FK
        varchar_20 role "USER, ASSISTANT, SYSTEM"
        text content
        integer token_count
        timestamptz created_at
        timestamptz updated_at
        timestamptz deleted_at
    }

    MEMORY_ENTRY {
        bigserial id PK
        uuid user_id FK
        varchar_100 key "UK(user_id, key)"
        text value
        varchar_20 source "MANUAL, AI_EXTRACTED"
        timestamptz created_at
        timestamptz updated_at
        timestamptz deleted_at
    }

    TELEGRAM_ACCOUNT {
        bigserial id PK
        uuid user_id FK "UK"
        bigint telegram_chat_id
        varchar_64 telegram_username
        timestamptz linked_at
    }

    EVENT_PUBLICATION {
        uuid id PK "Spring Modulith Outbox"
        timestamp completion_date
        varchar_255 event_type
        varchar_255 listener_id
        timestamp publication_date
        text serialized_event
        integer completion_attempts
        varchar_255 completion_status
        timestamp last_resubmission_date
    }
```

---

## 3. Перечисления (Enums)

| Enum | Значения | Где используется |
|---|---|---|
| `task_status` | `OPEN`, `IN_PROGRESS`, `COMPLETED`, `ARCHIVED` | `task.status` |
| `task_priority` | `LOW`, `MEDIUM`, `HIGH`, `URGENT` | `task.priority` |
| `notification_type`| `TASK_REMINDER`, `AI_MESSAGE`, `SYSTEM_ALERT` | `notification.type` |
| `platform_type` | `APP`, `TELEGRAM`, `EMAIL` | `notification.platform` |
| `reminder_status` | `PENDING`, `SENT`, `FAILED` | `reminder.status` |
| `message_role` | `USER`, `ASSISTANT`, `SYSTEM` | `message.role` |
| `memory_source` | `MANUAL`, `AI_EXTRACTED` | `memory_entry.source` |

*(Примечание: `user_role` удалён из Postgres, так как роли выдаются и валидируются через Keycloak)*

---

## 4. Памятка для обновления JPA Entity под новую схему

Когда будете писать код сущностей:

1. **`UserProfile.java`**:
   - `@Id private UUID id;` (без `@GeneratedValue`, так как ID присваивается Keycloak).
   - Убрать поля `password` и `roles`.
   - Имя таблицы: `@Table(name = "user_profile")`.
2. **Все внешние ключи `userId`**:
   - Во всех Entity (`Task`, `Project`, `Reminder`, `Notification`, `Conversation`, `MemoryEntry`, `TelegramAccount`):
     - `private UUID userId;` (вместо `Long userId`).
3. **Удалить Entity `RefreshToken.java`**:
   - Сущность больше не нужна.
4. **`Task.java`**:
   - Исправить имя таблицы: `@Table(name = "task")` (в единственном числе).
5. **`Project.java`**:
   - Убрать ошибочный `unique = true` с полей `userId` и `description`.
6. **`Message.java`**:
   - Исправить имя колонки: `@Column(name = "conversation_id")` (убрать опечатку `convesation_id`).
   - Тип `conversationId`: `UUID` (вместо `Long`).
   - Роль `role`: создать отдельный enum `MessageRole { USER, ASSISTANT, SYSTEM }`.
