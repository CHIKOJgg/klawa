# 📋 MASTER PLAN: Персональный AI-ассистент Klawa V2

> **Проект:** AI Assistant Klawa (V2 Architecture)  
> **Ветка:** `feat/V2-monorepo-microservices-migration`  
> **Цель:** Создать современный, надежный микросервисный сервис персонального ассистента с AI, Keycloak, Telegram, RabbitMQ и PostgreSQL (pgvector) в сжатые и предсказуемые сроки (**3–4 недели**).

---

## 1. Текущее состояние проекта (Статус на 17.10.2026)

### 1.1 Что уже сделано и работает:
1. **Репозиторий переведён на Maven Multi-Module Monorepo:**
   - Корневой модуль: `ai-assistant-klawa-parent`
   - Дочерние модули: `klawa-contracts`, `klawa-core`, `klawa-ai-agent`.
2. **Устранена несовместимость компилятора с Java 25:**
   - Исправлена ошибка `ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN` путём обновления Lombok до версии `1.18.38`.
   - Настроен строгий флаг сборки `<release>${java.version}</release>` в `maven-compiler-plugin`.
   - Вся сборка `./mvnw clean compile` успешно проходит со статусом **`BUILD SUCCESS`** на рантайме JDK 25.
3. **Реализован модуль `klawa-contracts` (Этап 1):**
   - Пакет `org.example.klawa.contracts.enums`:
     - `TaskStatus` (`OPEN`, `IN_PROGRESS`, `COMPLETED`, `ARCHIVED`) — строго по схеме `V0__init_tables.sql`.
     - `TaskPriority` (`LOW`, `MEDIUM`, `HIGH`, `URGENT`).
     - `NotificationType` (`TASK_REMINDER`, `AI_MESSAGE`, `SYSTEM_ALERT`).
     - `PlatformType` (`APP`, `TELEGRAM`, `EMAIL`).
     - `UserRole` (`ROLE_USER`, `ROLE_ADMIN`).
   - Пакет `org.example.klawa.contracts.event` (Java Records для RabbitMQ):
     - `TaskCreatedEvent`
     - `TaskStatusUpdatedEvent`
     - `AiTaskAnalysisRequestedEvent`
   - Модуль успешно скомпилирован и установлен в локальный Maven-репозиторий (`.m2`).
4. **Готова схема базы данных и инфраструктура:**
   - Миграция `V0__init_tables.sql` в `klawa-core` описывает полную схему: `user_profile`, `project`, `task`, `reminder`, `notification`, `conversation`, `message`, `memory_entry`, `telegram_account`.
   - В `docker-compose.yml` сконфигурированы: PostgreSQL 17 (с `pgvector`), Keycloak 26 (с готовым файлом импорта `klawa-realm.json`), RabbitMQ 3, Redis 7.

### 1.2 Что находится в статусе TODO (требует реализации):
- [ ] **Docker Desktop:** в данный момент сервис Docker на машине приостановлен (требуется разморозка).
- [ ] **`klawa-core`:** отсутствуют JPA-сущности, репозитории, сервисы, мапперы и REST-контроллеры.
- [ ] **`klawa-core` Security:** не настроен `SecurityFilterChain` для валидации токенов Keycloak (OAuth2 Resource Server).
- [ ] **`klawa-ai-agent`:** отсутствуют сервисы Spring AI, векторный стор `pgvector` и слушатели событий RabbitMQ.

---

## 2. Технологический стек: «Что НАДО» vs «Что НЕ НАДО»

Главный принцип архитектуры — **прагматизм и максимизация ценности (ROI)**. Каждый инструмент должен решать конкретную прикладную задачу, а не добавляться «ради галочки».

```
┌────────────────────────────────────────────────────────────────────────┐
│                              КЛИЕНТЫ                                   │
│            React 19 SPA (Vite)          Telegram Messenger             │
└───────────────────┬───────────────────────────────┬────────────────────┘
                    │                               │ Webhook
                    ▼                               ▼
       ┌────────────────────────┐      ┌─────────────────────────┐
       │      Keycloak 26       │      │   klawa-core (:8080)    │
       │    Identity Provider   │      │   - OAuth2 Resource Srv │
       │ (OIDC / OAuth2 / PKCE) │      │   - Task & Project CRUD │
       └────────────────────────┘      │   - Reminders Scheduler │
                    │                  │   - TG Adapter / Auth   │
                    │ Bearer JWT       │   - Outbox Event Pub    │
                    ▼                  └──────┬────────────┬─────┘
       ┌────────────────────────┐             │            │
       │  klawa-ai-agent (:8082)│◄────────────┘            │
       │  - Spring AI           │   RabbitMQ Events        │
       │  - pgvector RAG        │   (task.created, etc)    │
       │  - Function Calling    │                          │
       │  - OpenFeign Client    │                          │
       └───────────┬────────────┘                          │
                   │                                       │
┌──────────────────▼───────────────────────────────────────▼─────────────┐
│                            ХРАНИЛИЩА                                   │
│      PostgreSQL 17 (klawa_core_db)      PostgreSQL + pgvector (ai_db)  │
│      RabbitMQ 3 (Broker)                Redis 7 (Кэш & Rate Limit)     │
└────────────────────────────────────────────────────────────────────────┘
```

### 2.1 Подробная матрица решений

| Технология / Слой | Что НАДО использовать | Что НЕ НАДО (Оверинжиниринг) | Почему именно так |
| :--- | :--- | :--- | :--- |
| **Количество сервисов** | **2 сервиса + библиотека контрактов:**<br>1. `klawa-contracts`<br>2. `klawa-core`<br>3. `klawa-ai-agent` | Дробить на 6–10 микросервисов (`auth`, `user`, `task`, `reminder`, `notification`, `tg`). | 1 разработчик не сможет в одиночку поддерживать 7–10 сервисов из-за распределенных транзакций (Saga), сетевой синхронизации и оверхеда по RAM (нужно 8+ ГБ). Разделение на **Core** и **AI Agent** идеально изолирует тяжелую AI-нагрузку. |
| **Язык и платформа** | **Java 25 (LTS)** + **Virtual Threads** (`spring.threads.virtual.enabled: true`) | Реактивный стек **Spring WebFlux** (`Mono`/`Flux`). | Virtual Threads дают масштабируемость уровня WebFlux, сохраняя простой, понятный императивный код JPA и Spring MVC. |
| **Фреймворк** | **Spring Boot 3.4.3** (базовая компиляция под Java 25 bytecode) | Форсировать сырой **Spring Boot 4.0 Milestone** прямо на старте. | Spring AI 1.0.0-M5, Spring Cloud OpenFeign и Lombok 1.18.38 гарантированно стабильны на Boot 3.4.х. Переход на 4.x осуществляется сменой версии в POM после стабилизации логики. |
| **Аутентификация** | **Keycloak 26** + `spring-boot-starter-oauth2-resource-server` | Писать самодельную аутентификацию (таблицы паролей, BCrypt, генерация и ротация refresh-токенов). | Keycloak — промышленный стандарт IAM. Оба сервиса просто проверяют цифровую подпись JWT по открытым ключам Keycloak. Экономия 1500+ строк уязвимого кода. |
| **База данных** | **PostgreSQL 17** + расширение **`pgvector`** | Подключать отдельные векторные базы данных (Pinecone, Qdrant, Milvus). | `pgvector` работает прямо внутри PostgreSQL. Мы храним реляционные данные и векторные эмбеддинги в одной СУБД без усложнения инфраструктуры. |
| **Асинхронность** | **RabbitMQ 3** (Topic Exchange `klawa.events`) | Apache Kafka. | Kafka избыточна по потреблению ресурсов и сложности настройки топиков/партиций. RabbitMQ легковесен, надежен и идеально подходит для AMQP-событий в монорепозитории. |
| **Синхронный REST** | **Spring Cloud OpenFeign** с пробросом JWT (`TokenRelayInterceptor`) | gRPC / RSocket с генерацией protobuf. | Protobuf создает лишний барьер компиляции и отладки. Feign-клиенты используют готовые Java-рекорды из `klawa-contracts` без оверхеда. |
| **AI Движок** | **Spring AI** (`ChatClient`, `VectorStore`, `@Tool` Function Calling) | Отдельный сервис на Python (FastAPI + LangChain). | Полноценный Spring AI на чистой Java позволяет делать RAG и Function Calling (вызов CRUD методов задач самой языковой моделью) без зоопарка языков. |
| **Кэш & Rate Limit** | **Redis 7** (`@Cacheable`, Bucket4j) | Сложные распределенные кластеры (Hazelcast/Infinispan). | Redis покрывает кэширование проектов/задач и защиту от спама в API. |
| **Telegram** | Встроенный модуль-адаптер в `klawa-core` (Long-Polling в dev, Webhook в prod) | Отдельный микросервис Telegram со своей базой. | Telegram — это лишь транспортный канал ввода/вывода. Создание под него отдельной БД создаст проблему дублирования профилей пользователей. |
| **Оркестрация** | **Docker Compose** | Kubernetes (k8s), Helm, Istio. | `docker compose up -d` разворачивает всю систему локально за 30 секунд. Идеально для быстрой разработки и демонстрации. |

---

## 3. Полный пошаговый план разработки (4 Спринта по 1 неделе)

### 🏁 СПРИНТ 1: Фундамент, JPA и Keycloak Security (Дни 1–7)

#### Задача 1.1: Контракты межсервисного взаимодействия (`klawa-contracts`)
* **Статус:** ✅ ВЫПОЛНЕНО.
* **Результат:** 
  - Енамы: `TaskStatus`, `TaskPriority`, `NotificationType`, `PlatformType`, `UserRole`.
  - Доменные события: `TaskCreatedEvent`, `TaskStatusUpdatedEvent`, `AiTaskAnalysisRequestedEvent`.
  - Модуль протестирован и установлен в `.m2`.

#### Задача 1.2: Модуль данных и JPA-сущности (`klawa-core`)
* **Срок:** 2-3 дня.
* **Что делать:**
  1. Разморозить Docker Desktop и поднять PostgreSQL (`docker compose up -d postgres`).
  2. Создать класс `BaseEntity` с полями `createdAt`, `updatedAt` и `@EntityListeners(AuditingEntityListener.class)`.
  3. Написать JPA-сущности в пакете `org.example.klawa.core.domain`:
     - `UserProfile`: `id` (UUID sub из Keycloak), `email`, `firstName`, `lastName`, `isActive`.
     - `Project`: `id` (Long), `name`, `description`, связь `@ManyToOne(fetch = FetchType.LAZY)` к `UserProfile`.
     - `Task`: `id` (Long), `title`, `description`, `status` (`TaskStatus`), `priority` (`TaskPriority`), `dueDate`, связи к `UserProfile` и `Project`.
     - `Reminder`: `id` (Long), `triggerTime`, `message`, `status`, связь к `Task`.
     - `Notification`: `id` (Long), `type`, `platform`, `message`, `isRead`.
  4. Запустить `KlawaCoreApplication` с настройкой `hibernate.ddl-auto: validate`.
* **Definition of Done (DoD):** Приложение стартует без `SchemaManagementException`. Схема сущностей на 100% соответствует миграции `V0__init_tables.sql`.

#### Задача 1.3: Настройка Keycloak OAuth2 Resource Server (`klawa-core`)
* **Срок:** 2 дня.
* **Что делать:**
  1. Создать класс `SecurityConfig` (`@EnableWebSecurity`, `@EnableMethodSecurity`).
  2. Написать `KeycloakJwtAuthenticationConverter`, извлекающий роли из клейма `realm_access.roles` с префиксом `ROLE_`.
  3. Настроить `SecurityFilterChain`:
     - Публичные: `/actuator/health`, `/swagger-ui/**`, `/v3/api-docs/**`.
     - Защищенные: `/api/**` (требуют валидный Bearer JWT).
  4. Реализовать JIT (Just-In-Time) User Provisioning сервис: при первом запросе от пользователя из токена считываются `sub`, `email`, `preferred_username` и автоматически создается запись в `user_profile`.
* **Definition of Done (DoD):** 
  - Запрос `GET /api/v1/users/me` без токена возвращает `401 Unauthorized`.
  - Запрос с валидным токеном Keycloak возвращает данные пользователя и роли со статусом `200 OK`.

---

### ⚙️ СПРИНТ 2: Бизнес-логика Core, Репозитории и REST API (Дни 8–14)

#### Задача 2.1: Репозитории Spring Data JPA с изоляцией пользователей
* **Срок:** 2 дня.
* **Что делать:**
  1. `UserProfileRepository`: поиск по `id` (Keycloak UUID).
  2. `ProjectRepository`: методы `findAllByUserId(UUID userId)`, `findByIdAndUserId(Long id, UUID userId)`.
  3. `TaskRepository`: `findAllByUserId(UUID userId, Pageable pageable)`, фильтрация по статусу, пагинация.
  4. Реализовать `TaskSpecification` для гибкого поиска (по названию, приоритету, датам).
* **DoD:** Пользователь А ни при каких обстоятельствах не может получить или изменить задачи пользователя Б.

#### Задача 2.2: Слой DTO, MapStruct и Сервисы
* **Срок:** 3 дня.
* **Что делать:**
  1. Создать рекорды DTO:
     - `CreateTaskRequest`, `UpdateTaskRequest`, `UpdateTaskStatusRequest`, `TaskResponse`.
     - `CreateProjectRequest`, `ProjectResponse`.
  2. Создать интерфейсы мапперов `TaskMapper`, `ProjectMapper` на MapStruct.
  3. Реализовать `TaskService` и `ProjectService` с аннотацией `@Transactional`.
  4. Реализовать конечный автомат переходов статусов (State Machine):
     - `OPEN` $\to$ `IN_PROGRESS` $\to$ `COMPLETED` / `ARCHIVED`.
  5. Реализовать глобальный обработчик ошибок `@RestControllerAdvice` по стандарту RFC 7807 (`ProblemDetail`).
* **DoD:** Валидация входных данных аннотациями `@Valid`, корректные HTTP-статусы ошибок (400, 403, 404, 409).

#### Задача 2.3: REST Контроллеры и OpenAPI Swagger
* **Срок:** 2 дня.
* **Что делать:**
  1. Реализовать `TaskController` (`/api/v1/tasks`) и `ProjectController` (`/api/v1/projects`).
  2. Подключить `springdoc-openapi-starter-webmvc-ui`.
  3. Проверить интерактивную документацию Swagger UI по адресу `http://localhost:8080/swagger-ui.html`.
* **DoD:** Полный CRUD задач и проектов доступен в Swagger, запросы успешно выполняются с авторизацией через Bearer JWT.

---

### 🧠 СПРИНТ 3: Асинхронность (RabbitMQ) и AI-Агент (Spring AI + pgvector) (Дни 15–21)

#### Задача 3.1: Топология RabbitMQ и отправка событий
* **Срок:** 2 дня.
* **Что делать:**
  1. В `klawa-core` настроить `RabbitMqConfig`:
     - Topic Exchange: `klawa.events`.
     - Routing Key: `task.created`, `task.status-changed`.
     - Настроить `Jackson2JsonMessageConverter` для передачи событий в формате JSON.
  2. В `TaskService` при успешном создании задачи публиковать событие `TaskCreatedEvent`.
* **DoD:** При создании задачи в очереди RabbitMQ появляется JSON-сообщение события.

#### Задача 3.2: AI Агент — Векторное хранилище и слушатель событий (`klawa-ai-agent`)
* **Срок:** 2-3 дня.
* **Что делать:**
  1. Подключить `spring-ai-pgvector-store` к базе данных `klawa_ai_db`.
  2. Настроить очередь `klawa.ai.task-indexing` и биндинг к `klawa.events` по ключу `task.created`.
  3. Создать `@RabbitListener`, который принимает `TaskCreatedEvent`:
     - Формирует текст: `Название: {title}. Описание: {description}. Приоритет: {priority}`.
     - Генерирует векторный эмбеддинг.
     - Сохраняет документ в `VectorStore` с метаданными (`taskId`, `userId`).
* **DoD:** Созданная в Core задача автоматически индексируется в таблице векторных эмбеддингов `vector_store` в `klawa_ai_db`.

#### Задача 3.3: AI Чат, RAG и Function Calling (Tool Calling)
* **Срок:** 3 дня.
* **Что делать:**
  1. Реализовать `AiChatService` на базе Spring AI `ChatClient`.
  2. Реализовать **RAG (Retrieval-Augmented Generation)**:
     - При запросе пользователя ищется топ-4 похожих задачи через `vectorStore.similaritySearch()`.
     - Найденные задачи передаются в контекст системного промпта.
  3. Реализовать **Function Calling (Tools)**:
     - Метод `@Tool` `createTaskViaAi(String title, TaskPriority priority, Instant dueDate)`: модель сама вызывает создание задачи, если пользователь просит об этом в чате.
     - Вызов транслируется в `klawa-core` через OpenFeign клиент с пробросом токена пользователя.
* **DoD:** Пользователь в чате пишет: *"Создай задачу купить продукты завтра вечером"* $\to$ AI понимает намерение, вызывает метод `createTaskViaAi` $\to$ задача появляется в PostgreSQL Core.

---

### 🚀 СПРИНТ 4: Telegram Bot, Напоминания, Кэширование и Завершение (Дни 22–28)

#### Задача 4.1: Планировщик напоминаний (Scheduler)
* **Срок:** 2 дня.
* **Что делать:**
  1. Создать сервис планировщика `@Scheduled(fixedRate = 30000)` в `klawa-core`.
  2. Выборка активных записей из таблицы `reminder` по условию `WHERE trigger_time <= NOW() AND status = 'PENDING'`.
  3. Обновление статуса на `SENT` и публикация уведомления.
* **DoD:** Напоминание срабатывает ровно в указанное время.

#### Задача 4.2: Telegram Bot интеграция
* **Срок:** 3 дня.
* **Что делать:**
  1. Подключить `telegram-spring-boot-starter` или Telegram Long-Polling/Webhook клиент.
  2. Реализовать привязку аккаунта по Deep Link:
     - В веб-интерфейсе генерируется одноразовый OTP-код.
     - Пользователь переходит по ссылке `t.me/klawa_assistant_bot?start=CODE`.
     - Бот сохраняет `telegram_chat_id` в таблице `telegram_account`.
  3. При срабатывании напоминания отправлять сообщение в Telegram пользователю.
* **DoD:** Уведомления о дедлайнах и напоминаниях приходят пользователю в личный диалог с Telegram-ботом.

#### Задача 4.3: Redis Кэширование и Rate Limiting
* **Срок:** 2 дня.
* **Что делать:**
  1. Включить `@EnableCaching` в `klawa-core`.
  2. Покрыть кэшем `@Cacheable(value = "projects", key = "#userId")` список проектов.
  3. Настроить `@CacheEvict` при создании или обновлении проекта.
* **DoD:** Повторные запросы списка проектов выполняются за < 5 мс без обращения к PostgreSQL.

---

## 4. Сводный чек-лист готовности к релизу

- [x] Мультимодульный Maven-проект с Java 25.
- [x] Рабочая компиляция и настройка Lombok 1.18.38 (`BUILD SUCCESS`).
- [x] Общие контракты DTO, Enums и событий в `klawa-contracts`.
- [ ] Запущенный Docker Compose (Postgres + pgvector, Keycloak, RabbitMQ, Redis).
- [ ] JPA-сущности и валидация схемы Flyway `V0__init_tables.sql`.
- [ ] OAuth2 Resource Server с Keycloak JWT авторизацией.
- [ ] REST API задач и проектов с изоляцией по пользователям.
- [ ] Асинхронная отправка событий в RabbitMQ.
- [ ] RAG и Function Calling в Spring AI микросервисе.
- [ ] Telegram-бот с отправкой напоминаний.
- [ ] Кэширование Redis и метрики Actuator.

---

## 5. Ближайший шаг к выполнению

Прямо сейчас проект полностью готов к переходу ко **Второму шагу Спринта 1**:
1. Разморозить Docker Desktop на рабочей машине и запустить контейнеры:
   ```powershell
   docker compose up -d
   ```
2. Создать класс `BaseEntity` и JPA-сущности (`UserProfile`, `Project`, `Task`, `Reminder`, `Notification`) в `klawa-core`.
3. Запустить сервис и убедиться, что Hibernate валидирует схему `V0` без единой ошибки.
