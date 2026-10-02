# 🚀 Руководство разработчика: Архитектура и реализация Klawa V2

Добро пожаловать в проект **Klawa V2**! Этот документ — твой пошаговый инженерный роадмап и архитектурный компас.

Архитектура проекта — **прагматичный гибридный микросервис на базе Maven Multi-Module Monorepo**:
- **`klawa-contracts`**: чистые Java 21 контракты (DTO, Enums, RabbitMQ Events). Не зависит от Spring и баз данных.
- **`klawa-core`** (порт `8080`): бизнес-ядро (пользователи, задачи, проекты, напоминания, OAuth2 Resource Server, PostgreSQL, Redis, Flyway).
- **`klawa-ai-agent`** (порт `8082`): интеллектуальный микросервис (Spring AI, RAG на `pgvector`, Function Calling, OpenFeign, RabbitMQ).
- **Внешняя инфраструктура** (Docker Compose): PostgreSQL 17 + pgvector, Keycloak 26, RabbitMQ 3, Redis 7.

```mermaid
graph TD
    Client["Frontend (React / Vite) :5173"]
    Keycloak["Keycloak IAM :8085\n(OAuth2 / OIDC Realm: klawa)"]

    subgraph "Backend Services (Maven Monorepo)"
        Core["klawa-core :8080\n(Spring Boot 3.4.3 / Web / JPA)"]
        Contracts["klawa-contracts\n(Shared DTOs & Events JAR)"]
        AI["klawa-ai-agent :8082\n(Spring AI / pgvector / Ollama/OpenAI)"]
    end

    subgraph "Infrastructure (Docker Compose)"
        PostgresCore[("PostgreSQL 17\nklawa_core_db")]
        PostgresAI[("PostgreSQL 17 + pgvector\nklawa_ai_db")]
        Broker["RabbitMQ 3 :5672\n(AMQP + Management :15672)"]
        Cache[("Redis 7 :6379\nCache & Sessions")]
    end

    Client -->|1. Login & Get JWT| Keycloak
    Client -->|2. REST + Bearer JWT| Core
    Client -->|3. Chat + Bearer JWT| AI

    Core -.->|implements| Contracts
    AI -.->|implements| Contracts

    Core -->|Read / Write| PostgresCore
    Core -->|Cache| Cache
    Core -->|Publish Events| Broker

    AI -->|Read / Write Vectors| PostgresAI
    Broker -->|Consume Events| AI
    AI -->|OpenFeign REST + Token Relay| Core
```

---

## 🧭 Содержание роадмапа

1. [Этап 0: Запуск и валидация инфраструктуры](#этап-0-запуск-и-валидация-инфраструктуры)
2. [Этап 1: Модуль `klawa-contracts` (Контракты и события)](#этап-1-модуль-klawa-contracts-контракты-и-события)
3. [Этап 2: Модуль `klawa-core` — Слой данных JPA и аудит](#этап-2-модуль-klawa-core--слой-данных-jpa-и-аудит)
4. [Этап 3: Модуль `klawa-core` — Безопасность и Keycloak JWT](#этап-3-модуль-klawa-core--безопасность-и-keycloak-jwt)
5. [Этап 4: Модуль `klawa-core` — Бизнес-логика и REST API](#этап-4-модуль-klawa-core--бизнес-логика-и-rest-api)
6. [Этап 5: Асинхронное взаимодействие через RabbitMQ](#этап-5-асинхронное-взаимодействие-через-rabbitmq)
7. [Этап 6: Модуль `klawa-ai-agent` — Spring AI и pgvector](#этап-6-модуль-klawa-ai-agent--spring-ai-и-pgvector)
8. [Этап 7: Кэширование (Redis) и Observability](#этап-7-кэширование-redis-и-observability)

---

## Этап 0: Запуск и валидация инфраструктуры

Перед написанием Java-кода необходимо убедиться, что вся среда запущена и здорова.

### 1. Запуск контейнеров
В корне проекта выполни:
```powershell
docker compose up -d
```

### 2. Чек-лист проверки здоровья
Проверь, что все 4 контейнера в статусе `Up` (healthy):
```powershell
docker compose ps
```

| Сервис | Порт | Как проверить вручную | Ожидаемый результат |
| :--- | :--- | :--- | :--- |
| **PostgreSQL** | `5432` | Любой GUI-клиент (DBeaver, DataGrip) к `localhost:5432`, user: `postgres`, pass: `postgres` | Должны существовать две базы: `klawa_core_db` и `klawa_ai_db` (созданы скриптом `01-init-databases.sql`) |
| **Keycloak** | `8085` | Открой в браузере `http://localhost:8085` | Доступна админка (`admin` / `admin`). В выпадающем списке реалмов должен быть импортирован реалм `klawa`. |
| **RabbitMQ** | `15672` | Открой `http://localhost:15672` | Логин `guest` / `guest`. Доступен Management Dashboard. |
| **Redis** | `6379` | `docker exec -it klawa-redis redis-cli ping` | Ответ `PONG`. |

### 3. Тест аутентификации Keycloak
Проверь выдачу JWT-токена через curl:
```powershell
curl -X POST "http://localhost:8085/realms/klawa/protocol/openid-connect/token" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "grant_type=password" `
  -d "client_id=klawa-frontend" `
  -d "username=testuser" `
  -d "password=password123"
```
> [!TIP]
> Скопируй полученный `access_token` и вставь его на сайте [jwt.io](https://jwt.io). Обрати внимание на поля `sub` (UUID пользователя), `preferred_username`, `realm_access.roles` (роли `ROLE_USER`). С этим токеном ты будешь тестировать защищенные эндпоинты!

---

## Этап 1: Модуль `klawa-contracts` (Контракты и события)

> **Цель этапа:** создать изолированный Java-пакет без тяжелых зависимостей от Spring, который будут использовать оба микросервиса.

Каталог: `klawa-contracts/src/main/java/org/example/klawa/contracts/`

### 1.1 Перечисления (Enums)
Создай пакет `enums`:
- `TaskStatus`: `BACKLOG`, `TODO`, `IN_PROGRESS`, `DONE`, `CANCELLED`
- `TaskPriority`: `LOW`, `MEDIUM`, `HIGH`, `URGENT`
- `NotificationType`: `REMINDER`, `AI_INSIGHT`, `SYSTEM`
- `PlatformType`: `TELEGRAM`, `EMAIL`, `WEB_PUSH`

### 1.2 Межсервисные события (Events)
Создай пакет `event`. В Java 21 события брокера идеально объявлять как `record`:
- `TaskCreatedEvent`:
  ```java
  public record TaskCreatedEvent(
      UUID eventId,
      UUID taskId,
      UUID userId,
      String title,
      String description,
      TaskPriority priority,
      Instant occurredAt
  ) implements Serializable {}
  ```
- `TaskStatusUpdatedEvent`:
  ```java
  public record TaskStatusUpdatedEvent(
      UUID eventId,
      UUID taskId,
      UUID userId,
      TaskStatus oldStatus,
      TaskStatus newStatus,
      Instant occurredAt
  ) implements Serializable {}
  ```
- `AiTaskAnalysisRequestedEvent`:
  ```java
  public record AiTaskAnalysisRequestedEvent(
      UUID requestId,
      UUID taskId,
      UUID userId,
      String prompt,
      Instant requestedAt
  ) implements Serializable {}
  ```

### Критерий готовности (Definition of Done)
Выполни сборку и установку модуля в локальный Maven-репозиторий:
```powershell
mvn clean install -pl klawa-contracts
```
Сборка должна завершиться со статусом **`BUILD SUCCESS`**.

---

## Этап 2: Модуль `klawa-core` — Слой данных JPA и аудит

> **Цель этапа:** написать JPA-сущности в точном соответствии со схемой Flyway [V0__init_tables.sql](file:///c:/Users/Honor/Desktop/ai-assistant-klawa/klawa-core/src/main/resources/db/migration/V0__init_tables.sql) и проверить их валидность Hibernate.

Каталог: `klawa-core/src/main/java/org/example/klawa/core/`

### 2.1 Изучи схему БД
Открой [V0__init_tables.sql](file:///c:/Users/Honor/Desktop/ai-assistant-klawa/klawa-core/src/main/resources/db/migration/V0__init_tables.sql) и обрати внимание на связи:
- `user_profiles` (PK: `id` UUID, `keycloak_id` UUID UNIQUE) — хранит профиль пользователя, синхронизированный с Keycloak.
- `projects` (PK: `id` UUID, FK: `user_id` $\to$ `user_profiles.id`)
- `tasks` (PK: `id` UUID, FK: `user_id`, FK: `project_id`)
- `reminders` (PK: `id` UUID, FK: `task_id`)
- `notifications` (PK: `id` UUID, FK: `user_id`)

### 2.2 Базовая сущность и Spring Data Auditing
Создай пакет `domain` (или `entity`):
1. Создай класс `BaseEntity`:
   - `@MappedSuperclass`
   - `@EntityListeners(AuditingEntityListener.class)`
   - Поля: `createdAt` (`Instant`), `updatedAt` (`Instant`).
2. Включи аудит в конфигурационном классе или на главном классе приложения:
   - `@EnableJpaAuditing`

### 2.3 Сущности JPA
Создай сущности:
1. `UserProfile` (`@Table(name = "user_profiles")`)
   - `id` (`UUID`, `@GeneratedValue(strategy = GenerationType.UUID)`)
   - `keycloakId` (`UUID`, unique, nullable = false)
   - `email`, `firstName`, `lastName`, `telegramChatId`
   - `@OneToMany` связи к `projects` и `tasks` (опционально, но лучше контролировать через репозиторий).
2. `Project` (`@Table(name = "projects")`)
   - `id`, `name`, `description`, `colorHex`
   - `@ManyToOne(fetch = FetchType.LAZY)` к `UserProfile`
3. `Task` (`@Table(name = "tasks")`)
   - `id`, `title`, `description`, `dueDate`, `estimatedHours`
   - `@Enumerated(EnumType.STRING)` для `TaskStatus` и `TaskPriority`
   - `@ManyToOne(fetch = FetchType.LAZY)` к `UserProfile`
   - `@ManyToOne(fetch = FetchType.LAZY)` к `Project` (nullable = true)
4. `Reminder` и `Notification` аналогично.

> [!CAUTION]
> ### ⚠️ Три золотых правила JPA для предотвращения критических багов:
> 1. **Никогда не используй `@Data` на JPA Entities!**
>    Используй `@Getter`, `@Setter`. `@Data` генерирует `equals`, `hashCode` и `toString`, которые читают ленивые коллекции (`LAZY`). Это приводит к `LazyInitializationException` или бесконечной взаимной рекурсии и `StackOverflowError`.
> 2. **Правильный `equals` и `hashCode`:**
>    ```java
>    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
>    public class Task extends BaseEntity {
>        @Id
>        @EqualsAndHashCode.Include
>        private UUID id;
>        ...
>    }
>    ```
> 3. **Все ассоциации только `FetchType.LAZY`:**
>    По умолчанию `@ManyToOne` и `@OneToOne` имеют `FetchType.EAGER`. Всегда явно пиши:
>    `@ManyToOne(fetch = FetchType.LAZY, optional = false)`.

### 2.4 Проверка валидности схемы
В [application.yaml](file:///c:/Users/Honor/Desktop/ai-assistant-klawa/klawa-core/src/main/resources/application.yaml) включены:
```yaml
spring.flyway.enabled: true
spring.jpa.hibernate.ddl-auto: validate
```
Запусти `KlawaCoreApplication`. Если имена таблиц, колонок, типы и `nullability` в твоих Java-классах совпадают со скриптом Flyway — приложение стартует чисто. Если где-то опечатка — Hibernate выдаст `SchemaManagementException` с указанием конкретной колонки.

---

## Этап 3: Модуль `klawa-core` — Безопасность и Keycloak JWT

> **Цель этапа:** настроить `SecurityFilterChain` так, чтобы `klawa-core` выступал в роли OAuth2 Resource Server, валидировал JWT токены от Keycloak и извлекал роли пользователя.

Каталог: `klawa-core/src/main/java/org/example/klawa/core/security/`

### 3.1 Архитектура проверки токена
Когда клиент делает запрос:
```http
GET /api/v1/tasks
Authorization: Bearer eyJhbGciOiJSUzI1Ni...
```
Spring Security по `spring.security.oauth2.resourceserver.jwt.issuer-uri`:
1. Скачивает публичный ключ Keycloak (`http://localhost:8085/realms/klawa/protocol/openid-connect/certs`).
2. Проверяет подпись токена и срок его действия (`exp`).
3. Преобразует токен в `Authentication` объект.

### 3.2 Твоя задача: Настроить `SecurityConfig`
Создай класс `@Configuration` и `@EnableWebSecurity`:

1. **Конвертер ролей Keycloak (`JwtAuthenticationConverter`):**
   Keycloak помещает роли в структуру:
   ```json
   "realm_access": {
     "roles": ["ROLE_USER", "default-roles-klawa"]
   }
   ```
   Напиши `Converter<Jwt, AbstractAuthenticationToken>`, который:
   - Достает список строк из `jwt.getClaimAsMap("realm_access").get("roles")`.
   - Оборачивает каждую роль в `new SimpleGrantedAuthority(roleName)`.
   - Передает authorities в `JwtAuthenticationToken`.

2. **Бин `SecurityFilterChain`:**
   - Настрой CORS: разреши источник `http://localhost:5173` с методами `GET, POST, PUT, DELETE, PATCH, OPTIONS`.
   - Отключи CSRF (для stateless REST API с токенами он не нужен): `.csrf(AbstractHttpConfigurer::disable)`.
   - Сессионный режим: `SessionCreationPolicy.STATELESS`.
   - Авторизация запросов:
     - `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**` $\to$ `permitAll()`
     - `/api/**` $\to$ `authenticated()`
   - Подключи Resource Server:
     ```java
     .oauth2ResourceServer(oauth2 -> oauth2
         .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
     )
     ```

3. **Утилитный компонент для получения текущего пользователя:**
   Создай класс или метод, достающий `UUID keycloakId` из `SecurityContextHolder`:
   ```java
   public UUID getCurrentUserKeycloakId() {
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
       Jwt jwt = (Jwt) authentication.getPrincipal();
       return UUID.fromString(jwt.getSubject()); // jwt.getSubject() это 'sub' claim из Keycloak
   }
   ```

### Критерий проверки
Сделай тестовый контроллер с эндпоинтом `GET /api/v1/test/me`.
1. Запрос без токена: ответ `401 Unauthorized`.
2. Запрос с токеном из Этапа 0: ответ `200 OK` с твоим Keycloak ID и списком ролей.

---

## Этап 4: Модуль `klawa-core` — Бизнес-логика и REST API

> **Цель этапа:** реализовать чистую архитектуру: Репозитории $\to$ Сервисы $\to$ DTO/Мапперы $\to$ Контроллеры.

### 4.1 Репозитории (`Spring Data JPA`)
Создай репозитории в пакете `repository`:
- `UserProfileRepository`:
  - `Optional<UserProfile> findByKeycloakId(UUID keycloakId);`
- `ProjectRepository`:
  - `List<Project> findAllByUserId(UUID userId);`
  - `Optional<Project> findByIdAndUserId(UUID id, UUID userId);`
- `TaskRepository`:
  - `Page<Task> findAllByUserId(UUID userId, Pageable pageable);`
  - `Page<Task> findAllByUserIdAndStatus(UUID userId, TaskStatus status, Pageable pageable);`
  - `Optional<Task> findByIdAndUserId(UUID id, UUID userId);`

> [!IMPORTANT]
> **Принцип изоляции данных (Security & Multi-Tenancy):**
> Пользователь **ни при каких обстоятельствах** не должен иметь доступ к задачам чужих пользователей. Всегда делай поиск по составному условию `findByIdAndUserId(taskId, currentUserId)`.

### 4.2 Слой DTO и MapStruct
Создай пакет `dto` и мапперы `mapper`:
- Records для запросов/ответов:
  - `CreateTaskRequest(@NotBlank String title, String description, TaskPriority priority, UUID projectId, Instant dueDate)`
  - `UpdateTaskStatusRequest(@NotNull TaskStatus status)`
  - `TaskResponse(UUID id, String title, String description, TaskStatus status, TaskPriority priority, Instant dueDate, UUID projectId, Instant createdAt)`
- Интерфейс `TaskMapper`:
  ```java
  @Mapper(componentModel = "spring")
  public interface TaskMapper {
      TaskResponse toResponse(Task task);
      Task toEntity(CreateTaskRequest request);
  }
  ```

### 4.3 Сервисный слой (`@Service` и `@Transactional`)
Реализуй `TaskService`:
- `createTask(CreateTaskRequest request, UUID userKeycloakId)`:
  - Находит `UserProfile` по `keycloakId` (если еще нет — создает запись на лету: JIT User Provisioning!).
  - Создает и сохраняет `Task`.
  - Публикует событие `TaskCreatedEvent` в RabbitMQ (см. Этап 5).
- `getTasks(...)` с пагинацией (`Pageable`).
- `updateStatus(UUID taskId, TaskStatus newStatus, UUID userKeycloakId)`:
  - Меняет статус и сохраняет.

### 4.4 Глобальный обработчик ошибок (`@RestControllerAdvice`)
Используй современный стандарт Spring 6 **RFC 7807 `ProblemDetail`**:
- Обработка `EntityNotFoundException` $\to$ HTTP `404 Not Found`.
- Обработка `AccessDeniedException` $\to$ HTTP `403 Forbidden`.
- Обработка `MethodArgumentNotValidException` $\to$ HTTP `400 Bad Request` с перечислением невалидных полей.

---

## Этап 5: Асинхронное взаимодействие через RabbitMQ

> **Цель этапа:** настроить надежную отправку событий из `klawa-core` и их обработку в `klawa-ai-agent`.

### 5.1 Топология RabbitMQ
В `klawa-core` создай конфигурацию `RabbitMqConfig`:
- **Exchange:** `klawa.events` (тип `topic`).
- **Routing Keys:**
  - `task.created` $\to$ при создании задачи
  - `task.status-changed` $\to$ при смене статуса
  - `ai.analysis.requested` $\to$ при запросе AI-аналитики
- **MessageConverter:**
  ```java
  @Bean
  public MessageConverter jacksonMessageConverter() {
      return new Jackson2JsonMessageConverter();
  }
  ```
  *(Это гарантирует отправку сообщений в формате чистого JSON, а не бинарной Java-сериализации).*

### 5.2 Продюсер событий в `klawa-core`
Создай компонент `EventPublisher`:
```java
@Service
@RequiredArgsConstructor
public class EventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publishTaskCreated(TaskCreatedEvent event) {
        rabbitTemplate.convertAndSend("klawa.events", "task.created", event);
    }
}
```

### 5.3 Консьюмер в `klawa-ai-agent`
В модуле `klawa-ai-agent` создай очередь и слушатель:
```java
@RabbitListener(queues = "klawa.ai.task-analysis.queue")
public void handleTaskCreated(TaskCreatedEvent event) {
    log.info("Получено событие создания задачи для AI анализа: {}", event.taskId());
    // Здесь мы передаем задачу в векторную БД или запускаем генерацию тегов
}
```

---

## Этап 6: Модуль `klawa-ai-agent` — Spring AI и pgvector

> **Цель этапа:** научить AI-агента индексировать задачи в векторное хранилище и отвечать на запросы пользователя с учетом контекста (RAG).

Каталог: `klawa-ai-agent/src/main/java/org/example/klawa/agent/`

### 6.1 Векторное хранилище `VectorStore` (pgvector)
В [application.yaml](file:///c:/Users/Honor/Desktop/ai-assistant-klawa/klawa-ai-agent/src/main/resources/application.yaml) уже прописан `spring-ai-pgvector-store`.
При старте он использует базу данных `klawa_ai_db` и создает таблицу для эмбеддингов:
- Преобразование текста задачи в векторное представление через `EmbeddingModel`.
- Поиск семантически похожих задач:
  ```java
  List<Document> similarDocs = vectorStore.similaritySearch(
      SearchRequest.query("Что мне нужно сделать по бэкенду?").withTopK(3)
  );
  ```

### 6.2 Интеграция с LLM (Spring AI `ChatClient`)
Создай `AiChatService`:
```java
@Service
public class AiChatService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public AiChatService(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
    }

    public String askAssistant(String userPrompt, UUID userId) {
        // 1. Ищем релевантный контекст в pgvector
        List<Document> context = vectorStore.similaritySearch(
            SearchRequest.query(userPrompt).withTopK(4)
        );

        // 2. Формируем RAG-промпт и отправляем в модель
        return chatClient.prompt()
            .system("Ты персональный ассистент Klawa. Отвечай кратко на основе контекста задач: " + context)
            .user(userPrompt)
            .call()
            .content();
    }
}
```

### 6.3 Межсервисный клиент к `klawa-core` (OpenFeign)
Если AI-агенту нужно запросить список задач напрямую через REST:
```java
@FeignClient(name = "klawa-core", url = "${klawa.core.url}")
public interface KlawaCoreClient {
    @GetMapping("/api/v1/tasks/{id}")
    TaskResponse getTaskById(@PathVariable("id") UUID id, @RequestHeader("Authorization") String bearerToken);
}
```

---

## Этап 7: Кэширование (Redis) и Observability

### 7.1 Redis Cache в `klawa-core`
1. Включи `@EnableCaching`.
2. Повесь аннотацию на часто запрашиваемый метод:
   ```java
   @Cacheable(value = "projects", key = "#userId")
   public List<ProjectResponse> getProjects(UUID userId) { ... }
   ```
3. Инвалидируй кэш при изменении:
   ```java
   @CacheEvict(value = "projects", key = "#userId")
   public ProjectResponse createProject(CreateProjectRequest req, UUID userId) { ... }
   ```

### 7.2 Actuator & Healthcheck
Проверь работу метрик и состояния компонентов:
- `http://localhost:8080/actuator/health` — статус соединений с PostgreSQL, RabbitMQ, Redis, DiskSpace.
- `http://localhost:8080/actuator/metrics` — детальные метрики JVM, HTTP латентности, пула соединений HikariCP.

---

## 🛠 Полезные команды для разработки

| Команда | Описание |
| :--- | :--- |
| `mvn clean compile` | Компиляция всех модулей проекта |
| `mvn clean install -pl klawa-contracts` | Быстрая пересборка и установка только модуля контрактов |
| `mvn spring-boot:run -pl klawa-core` | Запуск сервиса ядра |
| `mvn spring-boot:run -pl klawa-ai-agent` | Запуск сервиса AI-агента |
| `docker compose logs -f keycloak` | Просмотр логов Keycloak в реальном времени |
| `docker compose restart postgres` | Перезапуск базы данных |

---

## 💡 Как мы работаем дальше:
1. Открывай IDE в папке проекта.
2. Бери **Этап 1** (`klawa-contracts`).
3. Пиши код классов и енамов сам.
4. Если сталкиваешься с ошибкой компиляции, сомневаешься в выборе аннотации или архитектурного решения — задавай вопрос сюда. Я проанализирую твой код, объясню причину и подскажу лучшее решение!
