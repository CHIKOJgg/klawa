# ai-assistant-klawa — Полный roadmap до трудоустройства
### 30 дней · Middle-level production-ready система · Spring Boot разработчик

> **6 часов в день · 30 дней · Java 21 · Spring Boot 3 · PostgreSQL · Docker · Anthropic API · Telegram**

---

## Содержание

| Неделя | Дни | Фокус | Результат |
|---|---|---|---|
| **1 — Фундамент** | 1–5 | ENV, Flyway, Errors, JWT, Testcontainers | Приложение стартует, авторизация работает |
| **2 — Task модуль** | 6–10 | Soft Delete, Specifications, Cache, Security | CRUD с кэшем, State Machine, Method Security |
| **3 — Reminder + Resilience** | 11–13 | Events, Scheduler, Circuit Breaker | Event-driven уведомления, защита от сбоев |
| **4 — Agent + Memory** | 14–16 | AI чат, PromptBuilder, Memory | Персонализированный AI с историей |
| **5 — Telegram + Analytics** | 17–18 | Webhook, Deep Linking, JPQL | Telegram бот, метрики продуктивности |
| **6 — Production-Ready** | 19–24 | Docker, OpenAPI, Rate Limiting, HikariCP | Система готова к деплою |
| **7 — Трудоустройство** | 25–30 | Git, CI, N+1, @Transactional, Интервью | Первые отклики отправлены |

**Итог:** рабочая система + зелёный CI + готовность к техническому интервью.

---

# ai-assistant-klawa — 30-дневный roadmap
### Middle-level production-ready система

> **6 часов в день · 30 дней · Spring Boot · Java · PostgreSQL · Anthropic API · Telegram · Docker**

---

## Как работать с этим документом

Каждый день устроен одинаково:

| Блок | Что делать |
|---|---|
| 📚 Материалы | Читать/смотреть ДО начала кода — 30–60 мин |
| 💡 Почему | Понять решение, не просто скопировать код |
| 🔨 Задача | Реализовать с нуля, код — пример, не шаблон |
| ✅ Критерий | Выполнить все пункты, иначе день не закрыт |

**Правило:** критерий готовности — не "написал код", а "система делает вот это поведение".

---

## Неделя 1 — Фундамент
> **После недели:** приложение стартует без секретов в коде, схема управляется миграциями,
> ошибки предсказуемы, JWT авторизация работает, первый интеграционный тест зелёный.

---

### День 1 — Переменные окружения, профили `[лёгкий]`

**📚 Материалы:**
- 📹 Amigoscode — [Spring Boot Application Properties and Profiles](https://www.youtube.com/results?search_query=spring+boot+application+properties+profiles+amigoscode) (~25 мин)
- 📖 Spring Docs — [Externalized Configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html)
- 📖 Baeldung — [Spring Profiles](https://www.baeldung.com/spring-profiles)

**💡 Почему такое решение:**
Секреты в коде — это не просто "плохая практика", это реальная угроза. Если JWT-секрет попадёт в git, любой получивший доступ к репозиторию сможет авторизоваться в системе. Переменные окружения — стандарт [12-Factor App](https://12factor.net/config), которому следуют все серьёзные команды.

Профили нужны чтобы одно приложение вело себя по-разному: в dev — verbose логи, Swagger открыт; в prod — минимальные логи, Swagger закрыт. Без профилей появляются `if (isDev)` в коде — это катастрофа.

`open-in-view: false` — без этого JPA-сессия открыта до конца HTTP-запроса, вызывая lazy loading в слое представления. Классический N+1.

**🔨 Задача:**

```
src/main/resources/
    application.yaml          ← общее для всех профилей
    application-dev.yaml      ← локальная разработка
    application-prod.yaml     ← продакшн
    application-test.yaml     ← тесты
.env.example                  ← шаблон (в git)
.env                          ← реальные значения (.gitignore)
```

**application.yaml:**
```yaml
spring:
  application:
    name: ai-assistant-klawa
  datasource:
    url:      ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
    locations: classpath:db/migration

application:
  jwt:
    secret:     ${JWT_SECRET}
    expiration: ${JWT_EXPIRATION:900000}
  anthropic:
    api-key:    ${ANTHROPIC_API_KEY}
    model:      ${ANTHROPIC_MODEL:claude-sonnet-4-20250514}
    max-tokens: ${ANTHROPIC_MAX_TOKENS:1000}
  telegram:
    bot-token:   ${TELEGRAM_BOT_TOKEN}
    webhook-url: ${TELEGRAM_WEBHOOK_URL}
```

**application-dev.yaml:**
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
logging:
  level:
    org.example.aiassistantklawa: DEBUG
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{HH:mm:ss} [%X{traceId}] %-5level %logger{36} - %msg%n"
```

**application-prod.yaml:**
```yaml
spring:
  jpa:
    show-sql: false
logging:
  level:
    root: WARN
    org.example.aiassistantklawa: INFO
```

**.env.example:**
```dotenv
DB_URL=jdbc:postgresql://localhost:5432/klawa
DB_USERNAME=klawa_user
DB_PASSWORD=changeme
JWT_SECRET=your-256-bit-secret-minimum-32-characters-long
JWT_EXPIRATION=900000
ANTHROPIC_API_KEY=sk-ant-...
ANTHROPIC_MODEL=claude-sonnet-4-20250514
TELEGRAM_BOT_TOKEN=123456:ABC-DEF...
TELEGRAM_WEBHOOK_URL=https://your-domain.com
SPRING_PROFILES_ACTIVE=dev
```

**✅ Критерий готовности:**
- `git grep -r "password:" src/` → пусто
- `export $(cat .env | xargs) && mvn spring-boot:run` → приложение стартует
- `open-in-view: false` выставлен

---

### День 2 — Flyway: управление схемой + стратегия индексов `[средний]`

**📚 Материалы:**
- 📹 Dan Vega — [Flyway with Spring Boot](https://www.youtube.com/results?search_query=flyway+spring+boot+dan+vega) (~30 мин)
- 📖 Flyway Docs — [How Flyway Works](https://documentation.red-gate.com/fd/quickstart-how-flyway-works-184127223.html)
- 📖 use-the-index-luke.com — [Index Basics](https://use-the-index-luke.com/sql/anatomy) — лучший ресурс по индексам

**💡 Почему такое решение:**
`ddl-auto: create` удобен на старте, но убивает в продакшне — при каждом перезапуске все данные теряются. Flyway версионирует каждое изменение схемы. Историю изменений можно проверить, откатить, воспроизвести.

`TIMESTAMPTZ` вместо `TIMESTAMP`: хранит UTC, отображает в любой временной зоне. `TIMESTAMP` при смене timezone сервера даёт некорректные данные.

Частичный индекс `WHERE status = 'PENDING'` — планировщик каждую минуту ищет только PENDING записи. Индекс по всем строкам был бы в 10 раз больше.

**🔨 Задача:**

**V1__init_schema.sql:**
```sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE _user (
    id               BIGSERIAL    PRIMARY KEY,
    first_name       VARCHAR(100) NOT NULL,
    last_name        VARCHAR(100) NOT NULL,
    email            VARCHAR(255) NOT NULL,
    password         VARCHAR(255) NOT NULL,
    role             VARCHAR(20)  NOT NULL DEFAULT 'USER',
    telegram_chat_id BIGINT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_email         UNIQUE (email),
    CONSTRAINT uq_user_telegram_chat UNIQUE (telegram_chat_id)
);

CREATE INDEX idx_user_email ON _user(email);

CREATE TABLE refresh_tokens (
    id         BIGSERIAL    PRIMARY KEY,
    token      VARCHAR(512) NOT NULL,
    user_id    BIGINT       NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_refresh_token UNIQUE (token),
    CONSTRAINT fk_refresh_user  FOREIGN KEY (user_id)
        REFERENCES _user(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token  ON refresh_tokens(token);
CREATE INDEX idx_refresh_user   ON refresh_tokens(user_id);
-- Частичный индекс — только активные токены
CREATE INDEX idx_refresh_active ON refresh_tokens(user_id)
    WHERE revoked = false AND expires_at > now();
```

**✅ Критерий готовности:**
- `SELECT * FROM flyway_schema_history;` — одна строка с `success = true`
- `EXPLAIN SELECT * FROM _user WHERE email = 'x';` — план показывает `Index Scan`
- `ddl-auto: validate` — Hibernate не ругается

---

### День 3 — shared.error: единый формат + MDC трассировка `[средний]`

**📚 Материалы:**
- 📹 Java Techie — [Spring Boot Exception Handling](https://www.youtube.com/results?search_query=spring+boot+exception+handling+restcontrolleradvice+java+techie) (~25 мин)
- 📖 Baeldung — [Exception Handling for REST](https://www.baeldung.com/exception-handling-for-rest-with-spring)
- 📖 Baeldung — [MDC Logging](https://www.baeldung.com/mdc-in-log4j2-logback)

**💡 Почему такое решение:**
Без единого обработчика клиент получает разные форматы ошибок: JSON с `message`, HTML страницу, пустой 500. Фронтенд не может написать универсальный обработчик.

`traceId` в ответе — ID запроса который сквозно проходит через все логи. Когда пользователь пишет "у меня ошибка", он передаёт `traceId`, и разработчик находит весь путь запроса в логах за секунду. Без него поиск ошибки — угадывание.

MDC (Mapped Diagnostic Context) — thread-local хранилище для логгера. `MDC.clear()` в `finally` обязателен, иначе значения утекут в следующий запрос через thread pool.

**🔨 Задача:**

```java
// shared/web/RequestIdFilter.java
@Component @Order(1)
public class RequestIdFilter extends OncePerRequestFilter {
    private static final String TRACE_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String traceId = Optional.ofNullable(req.getHeader(TRACE_HEADER))
            .orElse(UUID.randomUUID().toString().substring(0, 8));
        MDC.put("traceId", traceId);
        res.setHeader(TRACE_HEADER, traceId);
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.clear(); // обязательно!
        }
    }
}

// shared/error/ApiError.java
public record ApiError(Instant timestamp, int status, String code,
                       String message, String path, String traceId) {
    public static ApiError of(int status, String code, String message, String path) {
        return new ApiError(Instant.now(), status, code, message, path, MDC.get("traceId"));
    }
}

// shared/error/GlobalExceptionHandler.java
@RestControllerAdvice @Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        log.warn("Not found: {}", ex.getMessage());
        return ResponseEntity.status(404)
            .body(ApiError.of(404, "NOT_FOUND", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest req) {
        return ResponseEntity.status(409)
            .body(ApiError.of(409, "CONFLICT", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbidden(ForbiddenException ex, HttpServletRequest req) {
        return ResponseEntity.status(403)
            .body(ApiError.of(403, "FORBIDDEN", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        String fields = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining("; "));
        return ResponseEntity.status(400)
            .body(ApiError.of(400, "VALIDATION_ERROR", fields, req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(500)
            .body(ApiError.of(500, "INTERNAL_ERROR", "Internal server error", req.getRequestURI()));
    }
}
```

**✅ Критерий готовности:**
- `POST /api/v1/auth/register {}` → `{"status":400,"code":"VALIDATION_ERROR","traceId":"abc12345"}`
- В логах каждая строка содержит `[traceId]`
- `X-Trace-Id` присутствует в заголовках каждого ответа

---

### День 4 — User: JWT + Refresh token rotation `[сложный]`

**📚 Материалы:**
- 📹 Amigoscode — [Spring Boot 3 Security + JWT](https://www.youtube.com/watch?v=KxqlJblhzfI)
- 📖 Auth0 Blog — [Refresh Token Rotation](https://auth0.com/blog/refresh-tokens-what-are-they-and-when-to-use-them/)
- 📖 OWASP — [JWT Security Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)

**💡 Почему такое решение:**
Access token живёт 15 минут — если украдут, ущерб ограничен. Refresh token живёт 30 дней, но используется только для получения нового access token.

**Refresh token rotation:** при каждом обновлении старый инвалидируется и выдаётся новый. Если хакер украл refresh и использует его после пользователя — сервер видит повторное использование и инвалидирует ВСЕ сессии пользователя. Это стандарт OAuth 2.0.

BCrypt с work factor ≥ 10 — однонаправленный хэш. MD5/SHA256 для паролей — это уязвимость, они слишком быстры для брутфорса.

**🔨 Задача:**

```java
// user/api/RegisterRequest.java
public record RegisterRequest(
    @NotBlank @Size(max=100) String firstName,
    @NotBlank @Size(max=100) String lastName,
    @Email    @NotBlank       String email,
    @Size(min=8, max=72)      String password
) {}

// user/api/AuthenticationResponse.java
public record AuthenticationResponse(
    String  accessToken,
    String  refreshToken,
    Instant accessTokenExpiresAt,
    Instant refreshTokenExpiresAt
) {}

// user/application/RefreshTokenService.java
@Service @RequiredArgsConstructor @Transactional
public class RefreshTokenService {
    private final RefreshTokenRepository repo;

    public String createToken(User user) {
        repo.revokeAllByUserId(user.getId()); // инвалидируем старые
        RefreshToken token = RefreshToken.builder()
            .token(UUID.randomUUID().toString())
            .user(user)
            .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
            .revoked(false).build();
        return repo.save(token).getToken();
    }

    public AuthenticationResponse rotate(String tokenValue, JwtService jwtService) {
        RefreshToken token = repo.findByToken(tokenValue)
            .orElseThrow(() -> new ForbiddenException("Invalid refresh token"));

        if (token.isRevoked() || token.getExpiresAt().isBefore(Instant.now())) {
            // Повторное использование = компрометация
            repo.revokeAllByUserId(token.getUser().getId());
            throw new ForbiddenException("Refresh token reuse detected. All sessions invalidated.");
        }

        token.setRevoked(true);
        User user = token.getUser();
        return new AuthenticationResponse(
            jwtService.generateToken(user),
            createToken(user),
            Instant.now().plus(15, ChronoUnit.MINUTES),
            Instant.now().plus(30, ChronoUnit.DAYS)
        );
    }
}
```

Добавить `POST /api/v1/auth/refresh` endpoint.

**✅ Критерий готовности:**
- Register → оба токена в ответе
- Refresh → новая пара, старый refresh инвалидирован
- Повторный refresh старым токеном → 403 + все сессии инвалидированы

---

### День 5 — Testcontainers + шаблон тестов `[средний]`

**📚 Материалы:**
- 📹 Dan Vega — [Testcontainers with Spring Boot 3](https://www.youtube.com/results?search_query=testcontainers+spring+boot+3+dan+vega) (~30 мин)
- 📖 Testcontainers — [Spring Boot Guide](https://testcontainers.com/guides/testing-spring-boot-rest-api-using-testcontainers/)
- 📖 Baeldung — [Spring Boot Tests](https://www.baeldung.com/spring-boot-testing)

**💡 Почему такое решение:**
H2 ведёт себя иначе чем PostgreSQL: другой SQL диалект, нет `TIMESTAMPTZ`, нет частичных индексов. Тест на H2 может быть зелёным, а на реальной БД падать. Testcontainers запускает настоящий PostgreSQL — тесты проверяют то что будет в продакшне.

Базовый тест-класс `IntegrationTestBase` — чтобы не дублировать настройку Postgres в каждом тесте. `withReuse(true)` — контейнер переиспользуется между тестами, что ускоряет прогон в 3–5 раз.

**🔨 Задача:**

```java
// test/IntegrationTestBase.java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16-alpine").withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired protected TestRestTemplate rest;

    protected String getToken(String email, String password) {
        var req  = new RegisterRequest("Test","User", email, password);
        var resp = rest.postForEntity("/api/v1/auth/register", req,
                                      AuthenticationResponse.class);
        return Objects.requireNonNull(resp.getBody()).accessToken();
    }

    protected HttpHeaders authHeaders(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        return h;
    }
}

// test/AuthControllerTest.java
class AuthControllerTest extends IntegrationTestBase {

    @Test
    void register_returns_token_pair() {
        var req  = new RegisterRequest("Ivan","Petrov","ivan@test.com","password123");
        var resp = rest.postForEntity("/api/v1/auth/register", req, AuthenticationResponse.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().accessToken()).isNotBlank();
        assertThat(resp.getBody().refreshToken()).isNotBlank();
    }

    @Test
    void duplicate_email_returns_409() {
        var req = new RegisterRequest("A","B","dup@test.com","password123");
        rest.postForEntity("/api/v1/auth/register", req, Void.class);
        var resp = rest.postForEntity("/api/v1/auth/register", req, ApiError.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resp.getBody().traceId()).isNotBlank();
    }

    @Test
    void refresh_token_rotation_invalidates_old_token() {
        var tokens = registerAndGetTokens("rotate@test.com");
        var newTokens = refresh(tokens.refreshToken());
        assertThat(newTokens.refreshToken()).isNotEqualTo(tokens.refreshToken());

        var resp = rest.postForEntity("/api/v1/auth/refresh",
            Map.of("refreshToken", tokens.refreshToken()), ApiError.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
```

**✅ Критерий готовности:**
- `mvn test` — зелёный с реальным PostgreSQL (не H2)
- 3 сценария авторизации покрыты
- `IntegrationTestBase` переиспользуется во всех последующих тестах
---

## Неделя 2 — Task модуль + Качество кода
> **После недели:** CRUD задач с кэшированием, Soft Delete, State Machine переходов статусов, method-level security.

---

### День 6 — Task/Project: миграция + BaseEntity + Soft Delete `[средний]`

**📚 Материалы:**
- 📹 Amigoscode — [JPA Relationships](https://www.youtube.com/results?search_query=jpa+relationships+manytoone+onetomany+spring+boot+amigoscode) (~35 мин)
- 📖 Baeldung — [Hibernate Soft Delete](https://www.baeldung.com/hibernate-soft-delete)
- 📖 Baeldung — [Spring Data Auditing](https://www.baeldung.com/database-auditing-jpa)

**💡 Почему такое решение:**
**Soft delete** — вместо физического удаления ставится флаг `deleted_at`. Это даёт: историю удалённых записей, возможность восстановления, отсутствие broken foreign keys. `@Where(clause = "deleted_at IS NULL")` на entity — Hibernate автоматически добавляет условие ко всем запросам.

`@EntityListeners(AuditingEntityListener.class)` + `@CreatedDate` + `@LastModifiedDate` — Spring Data автоматически выставляет временные метки. Не нужно делать это вручную в каждом use case.

`BaseEntity` — чтобы не дублировать `id`, `createdAt`, `updatedAt`, `deletedAt` в каждой entity. Изменение аудита в одном месте применяется ко всем.

Частичный индекс `WHERE deleted_at IS NULL` — все рабочие запросы ищут только активные записи. Индекс в несколько раз меньше полного.

**🔨 Задача:**

```java
// shared/domain/BaseEntity.java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate @Column(nullable=false, updatable=false)
    private Instant createdAt;

    @LastModifiedDate @Column(nullable=false)
    private Instant updatedAt;

    @Column
    private Instant deletedAt;

    public boolean isDeleted() { return deletedAt != null; }
    public void softDelete()   { this.deletedAt = Instant.now(); }
}

// task/domain/Task.java
@Entity @Table(name="tasks")
@Where(clause = "deleted_at IS NULL")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Task extends BaseEntity {
    @Column(nullable=false)
    private String title;

    @Column(columnDefinition="TEXT")
    private String description;

    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private TaskStatus status = TaskStatus.OPEN;

    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private Priority priority = Priority.MEDIUM;

    @Column
    private Instant dueDate;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="project_id")
    private Project project;
}

// task/domain/TaskStatus.java
public enum TaskStatus { OPEN, IN_PROGRESS, COMPLETED, ARCHIVED }

// task/domain/Priority.java
public enum Priority { LOW, MEDIUM, HIGH, URGENT }
```

**V2__tasks_projects.sql:**
```sql
CREATE TABLE projects (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    user_id     BIGINT       NOT NULL REFERENCES _user(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMPTZ
);

CREATE TABLE tasks (
    id          BIGSERIAL    PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    priority    VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    due_date    TIMESTAMPTZ,
    project_id  BIGINT       REFERENCES projects(id) ON DELETE SET NULL,
    user_id     BIGINT       NOT NULL REFERENCES _user(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMPTZ
);

-- Частичные индексы — только активные записи (без deleted_at)
CREATE INDEX idx_task_user_active    ON tasks(user_id)    WHERE deleted_at IS NULL;
CREATE INDEX idx_task_project_active ON tasks(project_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_task_status         ON tasks(status)     WHERE deleted_at IS NULL;
CREATE INDEX idx_task_due_date       ON tasks(due_date)   WHERE deleted_at IS NULL;
```

Добавить `@EnableJpaAuditing` в `AiAssistantKlawaApplication`.

**✅ Критерий готовности:**
- `task.softDelete()` выставляет `deletedAt`, запись остаётся в БД
- `taskRepo.findAll()` НЕ возвращает soft-deleted задачи
- `createdAt` и `updatedAt` выставляются автоматически без кода в use case

---

### День 7 — Task use cases + Specifications + State Machine `[сложный]`

**📚 Материалы:**
- 📖 Spring Docs — [JPA Specifications](https://docs.spring.io/spring-data/jpa/reference/jpa/specifications.html)
- 📖 Baeldung — [REST Search with Specifications](https://www.baeldung.com/rest-api-search-language-spring-data-specifications)
- 📖 Baeldung — [@Transactional](https://www.baeldung.com/transaction-configuration-with-jpa-and-spring)

**💡 Почему такое решение:**
`Specification<T>` — паттерн Specification из DDD. Каждый критерий фильтрации — отдельный объект. Их комбинируют через `and()`, `or()`. Легко тестировать изолированно, легко добавлять новые фильтры без изменения существующих.

`@Transactional(readOnly = true)` для GET — Hibernate пропускает dirty checking в read-only транзакциях. 10–30% ускорение на сложных запросах.

**State Machine** для статусов — бизнес-правила переходов живут в domain, а не в контроллере. `Task::transitionTo(newStatus)` проверяет допустимость перехода. Нельзя перевести ARCHIVED задачу в COMPLETED, минуя проверку.

**🔨 Задача:**

```java
// task/infrastructure/TaskSpecifications.java
public class TaskSpecifications {

    public static Specification<Task> hasUserId(Long userId) {
        return (root, q, cb) -> cb.equal(root.get("user").get("id"), userId);
    }
    public static Specification<Task> hasStatus(TaskStatus s) {
        return (root, q, cb) ->
            s == null ? cb.conjunction() : cb.equal(root.get("status"), s);
    }
    public static Specification<Task> hasPriority(Priority p) {
        return (root, q, cb) ->
            p == null ? cb.conjunction() : cb.equal(root.get("priority"), p);
    }
    public static Specification<Task> hasProjectId(Long pid) {
        return (root, q, cb) ->
            pid == null ? cb.conjunction() : cb.equal(root.get("project").get("id"), pid);
    }
    public static Specification<Task> isOverdue() {
        return (root, q, cb) -> cb.and(
            cb.lessThan(root.get("dueDate"), Instant.now()),
            cb.notEqual(root.get("status"), TaskStatus.COMPLETED),
            cb.notEqual(root.get("status"), TaskStatus.ARCHIVED)
        );
    }
}

// task/domain/Task.java — добавить State Machine
private static final Map<TaskStatus, Set<TaskStatus>> TRANSITIONS = Map.of(
    TaskStatus.OPEN,        Set.of(TaskStatus.IN_PROGRESS, TaskStatus.ARCHIVED),
    TaskStatus.IN_PROGRESS, Set.of(TaskStatus.COMPLETED, TaskStatus.OPEN, TaskStatus.ARCHIVED),
    TaskStatus.COMPLETED,   Set.of(TaskStatus.OPEN),
    TaskStatus.ARCHIVED,    Set.of()
);

public void transitionTo(TaskStatus newStatus) {
    if (!TRANSITIONS.getOrDefault(this.status, Set.of()).contains(newStatus))
        throw new BadRequestException(
            "Cannot transition from %s to %s".formatted(this.status, newStatus));
    this.status = newStatus;
}

// task/application/GetTasksUseCase.java
@UseCase @RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetTasksUseCase {
    private final JpaTaskRepository taskRepo;
    private final TaskMapper mapper;

    public Page<TaskResponse> execute(Long userId, TaskFilter filter, Pageable pageable) {
        Specification<Task> spec = Specification
            .where(hasUserId(userId))
            .and(hasStatus(filter.status()))
            .and(hasPriority(filter.priority()))
            .and(hasProjectId(filter.projectId()));
        return taskRepo.findAll(spec, pageable).map(mapper::toResponse);
    }
}

// task/application/CreateTaskUseCase.java
@UseCase @RequiredArgsConstructor @Transactional
public class CreateTaskUseCase {
    private final JpaTaskRepository    taskRepo;
    private final JpaProjectRepository projectRepo;
    private final TaskMapper           mapper;

    public TaskResponse execute(CreateTaskRequest req, Long userId) {
        Project project = null;
        if (req.projectId() != null) {
            project = projectRepo.findByIdAndUserId(req.projectId(), userId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
        }
        User userRef = new User(); userRef.setId(userId); // JPA proxy
        return mapper.toResponse(taskRepo.save(mapper.toEntity(req, userRef, project)));
    }
}

// task/application/CompleteTaskUseCase.java
@UseCase @RequiredArgsConstructor @Transactional
public class CompleteTaskUseCase {
    private final JpaTaskRepository taskRepo;
    private final TaskMapper mapper;

    public TaskResponse execute(Long taskId, Long userId) {
        Task task = taskRepo.findByIdAndUserId(taskId, userId)
            .orElseThrow(() -> new NotFoundException("Task not found"));
        task.transitionTo(TaskStatus.COMPLETED);
        return mapper.toResponse(task);
    }
}
```

**✅ Критерий готовности:**
- `GET /api/v1/tasks?status=OPEN&priority=HIGH&page=0&size=5&sort=dueDate,asc` → корректный результат
- `POST /api/v1/tasks/1/archive` → `POST /api/v1/tasks/1/complete` → 400
- Unit-тест State Machine: все недопустимые переходы кидают `BadRequestException`

---

### День 8 — Кэширование Caffeine + Method Security `[средний]`

**📚 Материалы:**
- 📖 Baeldung — [Spring Boot Caching with Caffeine](https://www.baeldung.com/spring-boot-caffeine-cache)
- 📖 Baeldung — [Spring Method Security](https://www.baeldung.com/spring-security-method-security)
- 📖 Caffeine — [GitHub Wiki](https://github.com/ben-manes/caffeine/wiki)

**💡 Почему такое решение:**
`UserDetailsService::loadUserByUsername` вызывается при КАЖДОМ HTTP запросе — JwtAuthenticationFilter загружает пользователя из БД на каждый API вызов. Caffeine (in-process кэш) хранит данные в памяти — нет IO. TTL 5 минут — баланс свежести данных и производительности.

Почему не Redis? Redis нужен когда несколько инстансов приложения. На старте Caffeine достаточно. Переход на Redis — замена одной зависимости, код `@Cacheable` не меняется.

`@PreAuthorize("@taskSecurityService.isOwner(...)")` — дополнительный слой защиты. Если разработчик забудет проверку в use case, Spring Security перехватит. Defense in depth.

**🔨 Задача:**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

```java
// config/CacheConfig.java
@Configuration @EnableCaching
public class CacheConfig {
    public static final String USERS_CACHE    = "users";
    public static final String PROJECTS_CACHE = "projects";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.registerCustomCache(USERS_CACHE,
            Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .build());
        manager.registerCustomCache(PROJECTS_CACHE,
            Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build());
        return manager;
    }
}

// user/infrastructure/UserDetailsServiceImpl.java
@Service @RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepo;

    @Override
    @Cacheable(value = USERS_CACHE, key = "#username")
    public UserDetails loadUserByUsername(String username) {
        return userRepo.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}

// security/TaskSecurityService.java
@Service("taskSecurityService") @RequiredArgsConstructor
public class TaskSecurityService {
    private final JpaTaskRepository taskRepo;

    public boolean isOwner(Long taskId, Authentication auth) {
        User user = (User) auth.getPrincipal();
        return taskRepo.existsByIdAndUserId(taskId, user.getId());
    }
}

// Добавить @EnableMethodSecurity в SecurityConfig
// Использование в use case:
@PreAuthorize("@taskSecurityService.isOwner(#taskId, authentication)")
public void execute(Long taskId, Long userId) { ... }
```

**✅ Критерий готовности:**
- Повторные вызовы `/users/me` — нет SELECT в логах (show-sql: true)
- После `PUT /users/me` кэш сбрасывается — следующий запрос идёт в БД
- `DELETE /api/v1/tasks/1` от другого пользователя → 403

---

### День 9 — Task DTO + Mapper + Controller `[средний]`

**📚 Материалы:**
- 📖 Baeldung — [Java Records](https://www.baeldung.com/java-record-keyword)
- 📖 Spring Docs — [@AuthenticationPrincipal](https://docs.spring.io/spring-security/reference/servlet/integrations/mvc.html#mvc-authentication-principal)
- 📖 Baeldung — [Spring Pagination and Sorting](https://www.baeldung.com/rest-api-pagination-in-spring)

**💡 Почему такое решение:**
Java Records — иммутабельные DTO без boilerplate. `record CreateTaskRequest(...)` автоматически создаёт конструктор, геттеры, `equals`, `hashCode`, `toString`. Аннотации валидации на параметрах record работают так же как на полях class.

`Page<TaskResponse>` вместо `List<TaskResponse>` — пагинация обязательна для production. Без неё запрос всех задач пользователя с тысячами задач убьёт память и время ответа.

Контроллер не должен содержать ни одной строки бизнес-логики. Если видишь `if/else` в контроллере — это ошибка архитектуры.

**🔨 Задача:**

```java
// task/api/CreateTaskRequest.java
public record CreateTaskRequest(
    @NotBlank @Size(max=255) String   title,
    @Size(max=2000)          String   description,
                             Priority priority,
    @FutureOrPresent         Instant  dueDate,
                             Long     projectId
) {}

// task/api/TaskResponse.java
public record TaskResponse(
    Long       id, String title, String description,
    TaskStatus status, Priority priority, Instant dueDate,
    Long projectId, String projectName, Instant createdAt, Instant updatedAt
) {}

// task/api/TaskFilter.java
public record TaskFilter(TaskStatus status, Priority priority, Long projectId) {}

// task/application/TaskMapper.java
@Component
public class TaskMapper {
    public TaskResponse toResponse(Task t) {
        return new TaskResponse(
            t.getId(), t.getTitle(), t.getDescription(),
            t.getStatus(), t.getPriority(), t.getDueDate(),
            t.getProject() != null ? t.getProject().getId()   : null,
            t.getProject() != null ? t.getProject().getName() : null,
            t.getCreatedAt(), t.getUpdatedAt()
        );
    }
    public Task toEntity(CreateTaskRequest r, User user, Project project) {
        return Task.builder()
            .title(r.title()).description(r.description())
            .priority(r.priority() != null ? r.priority() : Priority.MEDIUM)
            .dueDate(r.dueDate()).status(TaskStatus.OPEN)
            .user(user).project(project).build();
    }
}

// task/api/TaskController.java
@RestController @RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor @Tag(name="Tasks")
public class TaskController {
    private final CreateTaskUseCase  createTask;
    private final GetTasksUseCase    getTasks;
    private final UpdateTaskUseCase  updateTask;
    private final DeleteTaskUseCase  deleteTask;
    private final CompleteTaskUseCase completeTask;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody CreateTaskRequest req,
                               @AuthenticationPrincipal User user) {
        return createTask.execute(req, user.getId());
    }

    @GetMapping
    public Page<TaskResponse> list(@AuthenticationPrincipal User user,
                                   @RequestParam(required=false) TaskStatus status,
                                   @RequestParam(required=false) Priority   priority,
                                   @RequestParam(required=false) Long       projectId,
                                   Pageable pageable) {
        return getTasks.execute(user.getId(), new TaskFilter(status, priority, projectId), pageable);
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id,
                               @Valid @RequestBody UpdateTaskRequest req,
                               @AuthenticationPrincipal User user) {
        return updateTask.execute(id, req, user.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        deleteTask.execute(id, user.getId());
    }

    @PostMapping("/{id}/complete")
    public TaskResponse complete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return completeTask.execute(id, user.getId());
    }
}
```

**✅ Критерий готовности:**
- Ни одной строки бизнес-логики в контроллере
- Soft-deleted задачи не возвращаются в GET
- `POST /api/v1/tasks` без JWT → 401

---

### День 10 — Тесты Task + кэша + State Machine `[средний]`

**📚 Материалы:**
- 📖 Baeldung — [Testing in Spring Boot](https://www.baeldung.com/spring-boot-testing)
- 📖 Baeldung — [@MockBean vs @Mock](https://www.baeldung.com/java-spring-mockito-mock-mockbean)

**💡 Почему такое решение:**
Пирамида тестов: много unit-тестов (быстрые), меньше интеграционных (Testcontainers, медленные). Unit-тест `Task::transitionTo` не поднимает Spring контекст — запускается за миллисекунды. `@ParameterizedTest` — один тест проверяет все варианты входных данных.

`@Sql` аннотация — вставляет тестовые данные из SQL-файла перед тестом и откатывает после. Чище чем `@BeforeEach` с репозиторием.

**🔨 Задача:**

```java
// test/unit/TaskStateMachineTest.java — без Spring
class TaskStateMachineTest {

    @Test
    void open_to_in_progress_is_allowed() {
        Task task = Task.builder().status(TaskStatus.OPEN).build();
        task.transitionTo(TaskStatus.IN_PROGRESS);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @ParameterizedTest
    @EnumSource(value=TaskStatus.class, names={"COMPLETED","IN_PROGRESS","ARCHIVED"})
    void archived_task_cannot_transition_to_any_status(TaskStatus target) {
        Task task = Task.builder().status(TaskStatus.ARCHIVED).build();
        assertThatThrownBy(() -> task.transitionTo(target))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void completed_task_can_be_reopened() {
        Task task = Task.builder().status(TaskStatus.COMPLETED).build();
        task.transitionTo(TaskStatus.OPEN);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.OPEN);
    }
}

// test/integration/TaskControllerTest.java
class TaskControllerTest extends IntegrationTestBase {

    @Test
    void create_task_returns_201_with_correct_fields() {
        String token = getToken("task@test.com", "pass12345");
        var req = new CreateTaskRequest("Написать тесты", null, Priority.HIGH, null, null);
        var resp = rest.exchange("/api/v1/tasks", HttpMethod.POST,
            new HttpEntity<>(req, authHeaders(token)), TaskResponse.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().title()).isEqualTo("Написать тесты");
        assertThat(resp.getBody().status()).isEqualTo(TaskStatus.OPEN);
    }

    @Test
    void complete_task_twice_returns_400() {
        String token = getToken("twice@test.com", "pass12345");
        Long taskId = createTask(token, "Task").id();
        completeTask(token, taskId);
        var resp = rest.exchange("/api/v1/tasks/" + taskId + "/complete",
            HttpMethod.POST, new HttpEntity<>(authHeaders(token)), ApiError.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().code()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void soft_deleted_task_not_returned_in_list() {
        String token = getToken("delete@test.com", "pass12345");
        Long taskId = createTask(token, "To delete").id();
        rest.exchange("/api/v1/tasks/" + taskId, HttpMethod.DELETE,
            new HttpEntity<>(authHeaders(token)), Void.class);
        var list = rest.exchange("/api/v1/tasks", HttpMethod.GET,
            new HttpEntity<>(authHeaders(token)), new ParameterizedTypeReference<Page<TaskResponse>>() {});
        assertThat(list.getBody().getContent()).noneMatch(t -> t.id().equals(taskId));
    }
}
```

**✅ Критерий готовности:**
- `mvn test` зелёный
- State machine покрыт `@ParameterizedTest` для всех переходов
- 8 интеграционных сценариев: happy path + error cases + soft delete

---

## Неделя 3 — Reminder + Notification + Resilience
> **После недели:** напоминания работают через планировщик, уведомления через события,
> внешние вызовы защищены Circuit Breaker, логи трассируются по traceId.

---

### День 11 — Reminder + Notification: миграция + entities + use cases `[средний]`

**📚 Материалы:**
- 📹 [Spring Events — ApplicationEvent и @EventListener](https://www.youtube.com/results?search_query=spring+boot+application+events+eventlistener) (~20 мин)
- 📖 Spring Docs — [Application Events](https://docs.spring.io/spring-boot/reference/features/spring-application.html#features.spring-application.application-events-and-listeners)
- 📖 Baeldung — [Spring Events](https://www.baeldung.com/spring-events)
- 📹 [Spring @Scheduled Tasks](https://www.youtube.com/results?search_query=spring+boot+scheduled+tasks+fixeddelay+cron) (~20 мин)

**💡 Почему такое решение:**
`reminder-модуль` не должен знать про `notification-модуль`. Прямой вызов `NotificationService` из `ReminderScheduler` создаёт связность — нельзя тестировать reminder без notification.

`ApplicationEvent` — Spring публикует событие внутри JVM. Результат: `reminder` публикует `ReminderDueEvent`, `notification` подписывается. Они не импортируют друг друга — только событие. Это Observer pattern в монолите.

`@Scheduled(fixedDelay = 60_000)` vs `cron`: `fixedDelay` — следующий запуск через 60 сек ПОСЛЕ завершения предыдущего. Если обработка задержалась, следующий цикл не начнётся до конца текущего. Для напоминаний это безопаснее чем cron.

**🔨 Задача:**

**V3__reminders_events_notifications.sql:**
```sql
CREATE TABLE events (
    id          BIGSERIAL    PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    start_at    TIMESTAMPTZ  NOT NULL,
    end_at      TIMESTAMPTZ,
    location    VARCHAR(500),
    user_id     BIGINT       NOT NULL REFERENCES _user(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMPTZ
);

CREATE TABLE reminders (
    id         BIGSERIAL    PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    remind_at  TIMESTAMPTZ  NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    task_id    BIGINT       REFERENCES tasks(id)  ON DELETE CASCADE,
    event_id   BIGINT       REFERENCES events(id) ON DELETE CASCADE,
    user_id    BIGINT       NOT NULL REFERENCES _user(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE notifications (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES _user(id) ON DELETE CASCADE,
    type       VARCHAR(30)  NOT NULL,
    title      VARCHAR(255) NOT NULL,
    body       TEXT,
    read_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Частичный индекс — только PENDING записи (планировщик ищет только их)
CREATE INDEX idx_reminder_pending    ON reminders(remind_at) WHERE status = 'PENDING';
CREATE INDEX idx_notification_user   ON notifications(user_id, created_at DESC);
```

```java
// shared/event/ReminderDueEvent.java
public record ReminderDueEvent(Long reminderId, Long userId, String title) {}

// reminder/application/CreateReminderUseCase.java
@UseCase @Transactional
public class CreateReminderUseCase {
    private final ReminderRepository reminderRepo;
    private final JpaTaskRepository  taskRepo;

    public ReminderResponse execute(CreateReminderRequest req, Long userId) {
        if (req.taskId() == null && req.eventId() == null)
            throw new BadRequestException("Reminder must be linked to a task or event");
        if (req.taskId() != null) {
            taskRepo.findByIdAndUserId(req.taskId(), userId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        }
        Reminder r = Reminder.builder()
            .title(req.title()).remindAt(req.remindAt())
            .status(ReminderStatus.PENDING)
            .userId(userId).taskId(req.taskId()).eventId(req.eventId())
            .build();
        return mapper.toResponse(reminderRepo.save(r));
    }
}

// reminder/application/ReminderScheduler.java
@Component @RequiredArgsConstructor @Slf4j
public class ReminderScheduler {
    private final ReminderRepository       reminderRepo;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void checkDueReminders() {
        List<Reminder> due = reminderRepo
            .findByStatusAndRemindAtBefore(ReminderStatus.PENDING, Instant.now());
        if (!due.isEmpty())
            log.info("[Scheduler] Processing {} due reminders", due.size());
        due.forEach(r -> {
            eventPublisher.publishEvent(
                new ReminderDueEvent(r.getId(), r.getUserId(), r.getTitle()));
            r.setStatus(ReminderStatus.SENT);
        });
        reminderRepo.saveAll(due);
    }
}

// notification/application/ReminderDueEventHandler.java
@Component @RequiredArgsConstructor @Slf4j
public class ReminderDueEventHandler {
    private final SendNotificationUseCase sendNotification;

    @EventListener
    @Async("notificationExecutor")
    public void handle(ReminderDueEvent event) {
        log.info("[Notification] Sending for reminder={}", event.reminderId());
        sendNotification.execute(
            event.userId(), NotificationType.REMINDER,
            event.title(), "Напоминание: " + event.title());
    }
}
```

Добавить `@EnableScheduling` в `AsyncConfig` или `AiAssistantKlawaApplication`.

**✅ Критерий готовности:**
- `POST /api/v1/reminders` без taskId и eventId → 400
- SQL insert PENDING reminder с прошедшим `remind_at` → через ≤2 мин запись в `notifications`
- В логах строки `[Scheduler]` и `[Notification]` в разных потоках

---

### День 12 — Resilience4j: Circuit Breaker + @Async Config `[сложный]`

**📚 Материалы:**
- 📖 Resilience4j Docs — [Circuit Breaker](https://resilience4j.readme.io/docs/circuitbreaker)
- 📖 Baeldung — [Spring Boot Resilience4j](https://www.baeldung.com/spring-boot-resilience4j)
- 📖 Baeldung — [Spring @Async](https://www.baeldung.com/spring-async)
- 📹 [Circuit Breaker Pattern explained](https://www.youtube.com/results?search_query=circuit+breaker+pattern+spring+boot+resilience4j) (~25 мин)

**💡 Почему такое решение:**
Без Circuit Breaker: Anthropic API недоступен → каждый запрос зависает 30 сек на таймауте → thread pool исчерпан → вся система встаёт. С Circuit Breaker: после N ошибок подряд он "открывается" — запросы немедленно возвращают fallback без ожидания.

Три состояния: CLOSED (норма) → OPEN (ошибки, быстрый отказ) → HALF_OPEN (пробует один запрос) → CLOSED (если OK) или OPEN (если снова ошибка).

`@Retry` с exponential backoff для Telegram — Telegram Bot API возвращает 429 при превышении rate limit. Три попытки с удвоением интервала (1с, 2с, 4с) решают временные проблемы.

**🔨 Задача:**

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

```yaml
# application.yaml
resilience4j:
  circuitbreaker:
    instances:
      anthropic:
        registerHealthIndicator: true
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
  retry:
    instances:
      telegram:
        maxAttempts: 3
        waitDuration: 1s
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
```

```java
// config/AsyncConfig.java
@Configuration @EnableAsync @EnableScheduling
public class AsyncConfig {

    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(3);
        exec.setMaxPoolSize(10);
        exec.setQueueCapacity(200);
        exec.setThreadNamePrefix("notification-");
        exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        exec.initialize();
        return exec;
    }
}

// agent/infrastructure/AnthropicAiClient.java — добавить Circuit Breaker
@CircuitBreaker(name = "anthropic", fallbackMethod = "fallback")
public String chat(String systemPrompt, List<ChatMessage> history, String userMessage) {
    // ... HTTP запрос
}

public String fallback(String sp, List<ChatMessage> h, String msg, Exception ex) {
    log.warn("[AI] Fallback triggered: {}", ex.getMessage());
    return "AI-ассистент временно недоступен. Попробуй через минуту.";
}

// telegram/infrastructure/TelegramBotClient.java — добавить Retry
@Retry(name = "telegram")
public void sendMessage(Long chatId, String text) { ... }
```

**✅ Критерий готовности:**
- `GET /actuator/health` содержит `circuitBreakers.anthropic.status: UP`
- Неверный API ключ → после 5 запросов circuit breaker OPEN → ответы мгновенные с fallback
- После восстановления (правильный ключ) → CLOSED

---

### День 13 — Notification: MarkRead + тесты событий `[средний]`

**📚 Материалы:**
- 📖 Baeldung — [Testing Spring Events](https://www.baeldung.com/spring-boot-testing-events)
- 📖 Spring Docs — [@RecordApplicationEvents](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/test/context/event/RecordApplicationEvents.html)

**💡 Почему такое решение:**
`readAt` — nullable Instant вместо boolean `isRead`. Так знаем КОГДА прочитано — по этому полю строится аналитика "среднее время реакции на уведомление".

Идемпотентность `MarkRead`: повторный вызов не меняет `readAt`. Клиент может вызвать endpoint дважды (сетевые ретраи) — результат одинаковый.

`@RecordApplicationEvents` + `ApplicationEvents` — тестируем что событие опубликовано, без запуска реального обработчика. Изолированный тест планировщика.

**🔨 Задача:**

```java
// notification/application/MarkReadUseCase.java
@UseCase @Transactional
public class MarkReadUseCase {
    private final NotificationRepository repo;

    public NotificationResponse execute(Long notificationId, Long userId) {
        Notification n = repo.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new NotFoundException("Notification not found"));
        if (n.getReadAt() == null)
            n.setReadAt(Instant.now());
        return mapper.toResponse(n);
    }
}

// test/ReminderSchedulerTest.java
@SpringBootTest @Testcontainers @RecordApplicationEvents
class ReminderSchedulerTest extends IntegrationTestBase {

    @Autowired ApplicationEvents events;
    @Autowired ReminderRepository reminderRepo;
    @Autowired ReminderScheduler  scheduler;

    @Test
    void due_reminder_publishes_event_and_changes_status() {
        Long userId = createTestUser();
        Reminder r = reminderRepo.save(Reminder.builder()
            .title("Test").remindAt(Instant.now().minusSeconds(60))
            .status(ReminderStatus.PENDING).userId(userId).build());

        scheduler.checkDueReminders();

        assertThat(events.stream(ReminderDueEvent.class)).hasSize(1);
        assertThat(events.stream(ReminderDueEvent.class)
            .findFirst().get().title()).isEqualTo("Test");
        assertThat(reminderRepo.findById(r.getId()).get().getStatus())
            .isEqualTo(ReminderStatus.SENT);
    }
}
```

**✅ Критерий готовности:**
- `PUT /api/v1/notifications/1/read` → `readAt` заполнен
- Повторный вызов → `readAt` не изменился (идемпотентность)
- Тест планировщика зелёный через `@RecordApplicationEvents`

---

## Неделя 4 — Agent (AI чат) + Memory
> **После недели:** рабочий AI-чат с историей в БД, memory между сессиями, ежедневный summary,
> всё защищено Circuit Breaker.

---

### День 14 — Agent: миграция + AiClient порт + AnthropicAiClient `[сложный]`

**📚 Материалы:**
- 📖 Anthropic Docs — [Messages API Reference](https://docs.anthropic.com/en/api/messages)
- 📖 Anthropic Docs — [System Prompts](https://docs.anthropic.com/en/docs/build-with-claude/prompt-engineering/system-prompts)
- 📖 Spring Docs — [RestClient](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-restclient)

**💡 Почему такое решение:**
`AiClient` — интерфейс в `domain/`. `AnthropicAiClient` — реализация в `infrastructure/`. Паттерн Ports & Adapters. Use case знает только об интерфейсе. В тестах подставляется `MockAiClient`. Если переключиться на OpenAI — меняется только адаптер, весь остальной код остаётся.

UUID для conversations — не раскрывает порядковый номер (хакер не угадает ID чужого разговора). Для tasks подходит BIGSERIAL потому что их больше и они участвуют в аналитических JOIN'ах.

`findTop20ByConversationIdOrderByCreatedAtAsc` — последние 20 сообщений. Не все — потому что Anthropic API имеет лимит контекста. 20 сообщений ≈ 3000–5000 токенов — баланс памяти и стоимости.

**🔨 Задача:**

**V4__agent_memory.sql:**
```sql
CREATE TABLE conversations (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    title      VARCHAR(255),
    user_id    BIGINT      NOT NULL REFERENCES _user(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE messages (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID        NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    role            VARCHAR(20) NOT NULL,
    content         TEXT        NOT NULL,
    token_count     INTEGER,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE memory_entries (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES _user(id) ON DELETE CASCADE,
    key        VARCHAR(100) NOT NULL,
    value      TEXT         NOT NULL,
    source     VARCHAR(20)  NOT NULL DEFAULT 'MANUAL',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (user_id, key)
);

CREATE INDEX idx_messages_conv_time ON messages(conversation_id, created_at ASC);
CREATE INDEX idx_memory_user        ON memory_entries(user_id);
CREATE INDEX idx_conv_user_time     ON conversations(user_id, created_at DESC);
```

```java
// agent/domain/AiClient.java — порт (интерфейс)
public interface AiClient {
    String chat(String systemPrompt, List<ChatMessage> history, String userMessage);
}

// agent/domain/ChatMessage.java — DTO для AiClient (не entity)
public record ChatMessage(String role, String content) {}

// agent/infrastructure/AnthropicAiClient.java
@Component @RequiredArgsConstructor @Slf4j
public class AnthropicAiClient implements AiClient {
    private final AnthropicConfig config;
    private final RestClient      restClient;

    @Override
    @CircuitBreaker(name = "anthropic", fallbackMethod = "fallback")
    public String chat(String systemPrompt, List<ChatMessage> history, String userMessage) {
        List<Map<String, String>> messages = new ArrayList<>();
        history.forEach(m -> messages.add(Map.of("role", m.role(), "content", m.content())));
        messages.add(Map.of("role", "user", "content", userMessage));

        var body = Map.of(
            "model",      config.model(),
            "system",     systemPrompt.isBlank() ? "You are a helpful assistant." : systemPrompt,
            "messages",   messages,
            "max_tokens", config.maxTokens()
        );

        log.debug("[AI] Sending {} messages to Anthropic", messages.size());

        var response = restClient.post()
            .uri("https://api.anthropic.com/v1/messages")
            .header("x-api-key",         config.apiKey())
            .header("anthropic-version", "2023-06-01")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(Map.class);

        var content = (List<Map<String, Object>>) response.get("content");
        String reply = (String) content.get(0).get("text");
        log.debug("[AI] Reply: {} chars", reply.length());
        return reply;
    }

    public String fallback(String sp, List<ChatMessage> h, String msg, Exception ex) {
        log.warn("[AI] Fallback: {}", ex.getMessage());
        return "AI-ассистент временно недоступен. Попробуй через минуту.";
    }
}

// test/MockAiClient.java
@Component @Primary @Profile("test")
public class MockAiClient implements AiClient {
    private String nextResponse = "Mock AI response";

    @Override
    public String chat(String sp, List<ChatMessage> history, String msg) {
        return nextResponse;
    }

    public void willRespond(String response) { this.nextResponse = response; }
}
```

**✅ Критерий готовности:**
- `MockAiClient` используется в тестах автоматически (`@Profile("test")`)
- `AnthropicConfig` валидируется при старте: пустой `apiKey` → ошибка запуска
- Circuit breaker для Anthropic виден в `/actuator/health`

---

### День 15 — ChatUseCase + PromptBuilder + Memory `[сложный]`

**📚 Материалы:**
- 📖 Anthropic Docs — [Give Claude a Role](https://docs.anthropic.com/en/docs/build-with-claude/prompt-engineering/give-claude-a-role)
- 📖 Anthropic Docs — [Context Window](https://docs.anthropic.com/en/docs/build-with-claude/context-window)
- 📖 PostgreSQL Docs — [INSERT ON CONFLICT](https://www.postgresql.org/docs/current/sql-insert.html#SQL-ON-CONFLICT)

**💡 Почему такое решение:**
System prompt с данными пользователя (memory + активные задачи) делает ассистента персонализированным. AI видит контекст без необходимости пользователю каждый раз объяснять кто он и что у него происходит.

Upsert для memory (`findByUserIdAndKey().orElse(new MemoryEntry())`) — ключ уникален на уровне БД `UNIQUE (user_id, key)`. Повторное сохранение обновляет, не дублирует.

`ExtractMemoryUseCase` вызывается `@Async` — пользователь получает ответ чата немедленно, AI анализирует диалог в фоне за 1–3 секунды.

**🔨 Задача:**

```java
// agent/application/PromptBuilder.java
@Component @RequiredArgsConstructor
public class PromptBuilder {
    private final MemoryRepository  memoryRepo;
    private final JpaTaskRepository taskRepo;

    public String build(Long userId) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            Ты — персональный AI-ассистент для управления задачами.
            Отвечай чётко и по делу. Используй язык пользователя.
            Не придумывай данные которых нет в контексте.
            """);

        List<MemoryEntry> memories = memoryRepo.findByUserId(userId);
        if (!memories.isEmpty()) {
            sb.append("\n## О пользователе:\n");
            memories.forEach(m ->
                sb.append("- ").append(m.getKey())
                  .append(": ").append(m.getValue()).append("\n"));
        }

        List<Task> activeTasks = taskRepo.findWithFilters(
            userId, TaskStatus.IN_PROGRESS, null, null, PageRequest.of(0, 10)).getContent();
        if (!activeTasks.isEmpty()) {
            sb.append("\n## Задачи в работе:\n");
            activeTasks.forEach(t -> sb.append("- ").append(t.getTitle()).append("\n"));
        }
        return sb.toString();
    }
}

// agent/application/ChatUseCase.java
@UseCase @RequiredArgsConstructor @Transactional
public class ChatUseCase {
    private final ConversationRepository conversationRepo;
    private final MessageRepository      messageRepo;
    private final AiClient               aiClient;
    private final PromptBuilder          promptBuilder;
    private final ExtractMemoryUseCase   extractMemory;

    public ChatResponse execute(ChatRequest req, Long userId) {
        Conversation conv = req.conversationId() != null
            ? conversationRepo.findByIdAndUserId(UUID.fromString(req.conversationId()), userId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"))
            : conversationRepo.save(Conversation.builder().userId(userId).build());

        List<ChatMessage> history = messageRepo
            .findTop20ByConversationIdOrderByCreatedAtAsc(conv.getId())
            .stream()
            .map(m -> new ChatMessage(m.getRole().name().toLowerCase(), m.getContent()))
            .toList();

        String systemPrompt = promptBuilder.build(userId);
        String reply = aiClient.chat(systemPrompt, history, req.message());

        saveMessage(conv, MessageRole.USER,      req.message());
        saveMessage(conv, MessageRole.ASSISTANT, reply);

        if (history.isEmpty() && conv.getTitle() == null)
            conv.setTitle(req.message().substring(0, Math.min(50, req.message().length())));

        // Фоновый анализ диалога на факты о пользователе
        extractMemory.execute(userId, req.message(), reply);

        return new ChatResponse(conv.getId().toString(), reply, conv.getTitle());
    }

    private void saveMessage(Conversation conv, MessageRole role, String content) {
        messageRepo.save(
            Message.builder().conversation(conv).role(role).content(content).build());
    }
}

// memory/application/SaveMemoryUseCase.java
@UseCase @Transactional
public class SaveMemoryUseCase {
    private final MemoryRepository memoryRepo;

    public void execute(Long userId, String key, String value, MemorySource source) {
        MemoryEntry entry = memoryRepo.findByUserIdAndKey(userId, key.toLowerCase().trim())
            .orElse(MemoryEntry.builder().userId(userId)
                .key(key.toLowerCase().trim()).source(source).build());
        entry.setValue(value);
        entry.setUpdatedAt(Instant.now());
        memoryRepo.save(entry);
    }
}

// memory/application/ExtractMemoryUseCase.java
@UseCase @RequiredArgsConstructor @Slf4j
public class ExtractMemoryUseCase {
    private final AiClient          aiClient;
    private final SaveMemoryUseCase saveMemory;
    private final ObjectMapper      objectMapper;

    @Async("notificationExecutor")
    public void execute(Long userId, String userMessage, String assistantReply) {
        String prompt = """
            Из диалога выдели факты о ПОЛЬЗОВАТЕЛЕ (предпочтения, привычки, рабочий контекст).
            Верни ТОЛЬКО JSON массив без пояснений: [{"key":"snake_case","value":"значение"}]
            Если фактов нет — верни: []

            Пользователь: %s
            Ассистент: %s
            """.formatted(userMessage, assistantReply);
        try {
            String json = aiClient.chat("", List.of(), prompt)
                .replaceAll("```json|```", "").trim();
            List<Map<String, String>> facts =
                objectMapper.readValue(json, new TypeReference<>() {});
            facts.stream()
                .filter(f -> f.get("key") != null && f.get("value") != null)
                .filter(f -> !f.get("key").isBlank() && !f.get("value").isBlank())
                .forEach(f -> saveMemory.execute(
                    userId, f.get("key"), f.get("value"), MemorySource.AI_EXTRACTED));
            log.debug("[Memory] Extracted {} facts for user {}", facts.size(), userId);
        } catch (Exception e) {
            log.warn("[Memory] Extraction failed: {}", e.getMessage());
        }
    }
}
```

**✅ Критерий готовности:**
- `POST /api/v1/agent/chat {"message":"Привет"}` → ответ с `conversationId`
- Второй запрос с `conversationId` → AI видит историю (в таблице `messages` 4 строки)
- Memory из диалога появляется в таблице через несколько секунд
- System prompt в debug-логах содержит memory и активные задачи

---

### День 16 — Daily Summary + тесты AI-слоя `[средний]`

**📚 Материалы:**
- 📖 Anthropic Docs — [Summarization](https://docs.anthropic.com/en/docs/build-with-claude/summarize-long-documents)
- 📖 Baeldung — [MockRestServiceServer](https://www.baeldung.com/spring-mock-rest-service-server)

**💡 Почему такое решение:**
`DailySummaryUseCase` — отдельный use case, не часть `ChatUseCase`. Single Responsibility: чат — это диалог, summary — одностороннее резюме. Разные требования к контексту, разный prompt, разный ответ.

Тест ChatUseCase использует `MockAiClient` — не нужно мокировать HTTP. MockAiClient — это честная замена: реализует тот же интерфейс. Можно управлять ответом через `willRespond()`.

**🔨 Задача:**

```java
// agent/application/DailySummaryUseCase.java
@UseCase @RequiredArgsConstructor @Transactional(readOnly = true)
public class DailySummaryUseCase {
    private final JpaTaskRepository taskRepo;
    private final AiClient          aiClient;
    private final PromptBuilder     promptBuilder;

    public SummaryResponse execute(Long userId) {
        Instant start = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end   = start.plus(1, ChronoUnit.DAYS);

        List<Task> todayTasks = taskRepo.findByUserIdAndDueDateBetween(userId, start, end);
        List<Task> overdue    = taskRepo.findAll(
            Specification.where(hasUserId(userId)).and(isOverdue()),
            PageRequest.of(0, 10)).getContent();

        String context = buildContext(todayTasks, overdue);
        String prompt  = "Составь краткое резюме дня (3-4 предложения):\n" + context;
        String summary = aiClient.chat(promptBuilder.build(userId), List.of(), prompt);

        List<String> highlights = Stream.concat(
            todayTasks.stream().map(Task::getTitle),
            overdue.stream().map(t -> "⚠️ " + t.getTitle())
        ).limit(5).toList();

        return new SummaryResponse(summary, "DAILY", highlights);
    }

    private String buildContext(List<Task> today, List<Task> overdue) {
        StringBuilder sb = new StringBuilder();
        if (!today.isEmpty()) {
            sb.append("Задачи на сегодня:\n");
            today.forEach(t -> sb.append("- ").append(t.getTitle())
                .append(" [").append(t.getStatus()).append("]\n"));
        }
        if (!overdue.isEmpty()) {
            sb.append("\nПросрочено:\n");
            overdue.forEach(t -> sb.append("- ").append(t.getTitle()).append("\n"));
        }
        return sb.toString();
    }
}

// test/ChatUseCaseTest.java
@SpringBootTest @Testcontainers @ActiveProfiles("test")
class ChatUseCaseTest extends IntegrationTestBase {

    @Autowired MockAiClient   mockAiClient;
    @Autowired ChatUseCase    chatUseCase;
    @Autowired MessageRepository messageRepo;

    @Test
    void new_conversation_is_created_when_no_id_provided() {
        Long userId = createTestUser("chat1@test.com");
        mockAiClient.willRespond("Привет! Чем могу помочь?");

        ChatResponse resp = chatUseCase.execute(new ChatRequest("Привет", null), userId);

        assertThat(resp.conversationId()).isNotNull();
        assertThat(resp.reply()).isEqualTo("Привет! Чем могу помочь?");
    }

    @Test
    void history_persisted_across_messages() {
        Long userId = createTestUser("chat2@test.com");
        mockAiClient.willRespond("Ответ 1");
        ChatResponse first = chatUseCase.execute(new ChatRequest("Вопрос 1", null), userId);

        mockAiClient.willRespond("Ответ 2");
        chatUseCase.execute(new ChatRequest("Вопрос 2", first.conversationId()), userId);

        int count = messageRepo.countByConversationId(UUID.fromString(first.conversationId()));
        assertThat(count).isEqualTo(4); // 2 USER + 2 ASSISTANT
    }

    @Test
    void invalid_conversation_id_returns_404() {
        Long userId = createTestUser("chat3@test.com");
        assertThatThrownBy(() ->
            chatUseCase.execute(
                new ChatRequest("msg", UUID.randomUUID().toString()), userId))
            .isInstanceOf(NotFoundException.class);
    }
}
```

**✅ Критерий готовности:**
- `GET /api/v1/agent/summaries/daily` → AI-сгенерированное резюме с highlights
- Все 3 теста ChatUseCase зелёные без реального API вызова
- `GET /api/v1/agent/conversations` → список разговоров с пагинацией

---

## Неделя 5 — Telegram + Analytics
> **После недели:** Telegram бот принимает сообщения через AI-агент с Deep Linking для привязки аккаунта.
> Analytics с JPQL агрегацией считает метрики продуктивности.

---

### День 17 — Telegram: webhook + TelegramBotClient + Deep Linking `[сложный]`

**📚 Материалы:**
- 📖 Telegram Bot API — [Webhooks](https://core.telegram.org/bots/webhooks)
- 📖 Telegram Bot API — [Deep Linking](https://core.telegram.org/bots/features#deep-linking)
- 📹 [Spring Boot Telegram Bot](https://www.youtube.com/results?search_query=spring+boot+telegram+bot+webhook+java) (~30 мин)

**💡 Почему такое решение:**
Webhook vs polling: polling — постоянный цикл опроса с задержкой. Webhook — Telegram сам присылает сообщения мгновенно. Webhook — стандарт для production ботов.

Deep linking: пользователь кликает `t.me/YourBot?start=TOKEN` → Telegram шлёт `/start TOKEN` боту → бот находит пользователя по токену и привязывает `telegram_chat_id`. Токен одноразовый с TTL в БД. Безопасно — не нужно вводить пароль в Telegram.

`@Retry(name = "telegram")` — Telegram Bot API возвращает 429 при rate limit. Retry с exponential backoff делает 3 попытки прежде чем сдаться.

Endpoint `/api/v1/telegram/webhook` должен быть в списке `permitAll()` в SecurityConfig — Telegram не отправляет JWT.

**🔨 Задача:**

```java
// telegram/api/dto
public record TelegramUpdate(
    @JsonProperty("update_id") Long            updateId,
    @JsonProperty("message")   TelegramMessage message
) {}

public record TelegramMessage(
    @JsonProperty("message_id") Long         messageId,
    @JsonProperty("from")       TelegramUser from,
    @JsonProperty("chat")       TelegramChat chat,
    @JsonProperty("text")       String       text
) {}

public record TelegramUser(
    Long id, String username,
    @JsonProperty("first_name") String firstName
) {}

// telegram/api/TelegramController.java
@RestController @RequestMapping("/api/v1/telegram")
@RequiredArgsConstructor
public class TelegramController {
    private final HandleWebhookUseCase handleWebhook;

    @PostMapping("/webhook")
    @ResponseStatus(HttpStatus.OK)
    public void webhook(@RequestBody TelegramUpdate update) {
        handleWebhook.execute(update);
    }
}

// telegram/application/HandleWebhookUseCase.java
@UseCase @RequiredArgsConstructor @Slf4j @Transactional
public class HandleWebhookUseCase {
    private final UserRepository     userRepo;
    private final ChatUseCase        chatUseCase;
    private final TelegramClient     telegramClient;
    private final LinkAccountUseCase linkAccount;

    public void execute(TelegramUpdate update) {
        if (update.message() == null || update.message().text() == null) return;

        Long   chatId     = update.message().chat().id();
        Long   telegramId = update.message().from().id();
        String text       = update.message().text();

        log.info("[Telegram] chatId={} text={}", chatId, text.substring(0, Math.min(30, text.length())));

        // Deep link привязка аккаунта
        if (text.startsWith("/start ")) {
            String token = text.substring(7).trim();
            try {
                linkAccount.execute(token, chatId, telegramId);
                telegramClient.sendMessage(chatId,
                    "✅ Аккаунт привязан! Теперь пиши мне здесь.");
            } catch (NotFoundException e) {
                telegramClient.sendMessage(chatId,
                    "❌ Код недействителен. Получи новый в приложении.");
            }
            return;
        }

        User user = userRepo.findByTelegramChatId(chatId).orElse(null);
        if (user == null) {
            telegramClient.sendMessage(chatId,
                "Привяжи аккаунт: открой приложение → Профиль → Telegram → Получить код");
            return;
        }

        try {
            ChatResponse resp = chatUseCase.execute(new ChatRequest(text, null), user.getId());
            telegramClient.sendMessage(chatId, resp.reply());
        } catch (Exception e) {
            log.error("[Telegram] Chat failed for userId={}: {}", user.getId(), e.getMessage());
            telegramClient.sendMessage(chatId, "Произошла ошибка. Попробуй ещё раз.");
        }
    }
}

// telegram/infrastructure/TelegramBotClient.java
@Component @RequiredArgsConstructor @Slf4j
public class TelegramBotClient implements TelegramClient {
    private final RestClient     restClient;
    private final TelegramConfig config;

    @Override
    @Retry(name = "telegram")
    public void sendMessage(Long chatId, String text) {
        restClient.post()
            .uri("https://api.telegram.org/bot{token}/sendMessage", config.botToken())
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("chat_id", chatId, "text", text, "parse_mode", "HTML"))
            .retrieve()
            .toBodilessEntity();
        log.debug("[Telegram] Sent to chatId={}", chatId);
    }

    @Override
    public void setWebhook(String url) {
        restClient.post()
            .uri("https://api.telegram.org/bot{token}/setWebhook", config.botToken())
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("url", url))
            .retrieve()
            .toBodilessEntity();
        log.info("[Telegram] Webhook set to {}", url);
    }
}
```

Добавить `SendNotificationUseCase` — если есть `telegramChatId`, отправить уведомление в Telegram.

**✅ Критерий готовности:**
- `POST /api/v1/telegram/webhook` (без JWT) → 200 OK
- Незарегистрированный пользователь → инструкция по привязке
- Цепочка: PENDING reminder → планировщик → событие → notification → Telegram сообщение

---

### День 18 — Analytics: JPQL агрегации + тесты Telegram `[средний]`

**📚 Материалы:**
- 📖 Baeldung — [JPQL GROUP BY, COUNT](https://www.baeldung.com/jpql-hql-criteria-builder)
- 📖 Spring Docs — [Projections](https://docs.spring.io/spring-data/jpa/reference/repositories/projections.html)
- 📖 Baeldung — [MockRestServiceServer](https://www.baeldung.com/spring-mock-rest-service-server)

**💡 Почему такое решение:**
Analytics запросы агрегирующие. `findAll().stream().collect(groupingBy(...))` — загружает все строки в память. JPQL `GROUP BY` считает на стороне БД — в тысячи раз эффективнее на больших данных.

Spring Projections — интерфейс с геттерами. Spring Data создаёт SQL только с нужными колонками. Не тянем весь Task entity когда нужны только `title` и `status`.

Тесты Telegram не должны обращаться к реальному Telegram API — `MockRestServiceServer` мокирует HTTP на уровне Spring `RestTemplate`/`RestClient`.

**🔨 Задача:**

```java
// analytics/application/ProductivityUseCase.java
@UseCase @RequiredArgsConstructor @Transactional(readOnly = true)
public class ProductivityUseCase {
    private final JpaTaskRepository taskRepo;

    public ProductivityResponse execute(Long userId, String period) {
        Instant from = switch (period.toUpperCase()) {
            case "WEEK"  -> Instant.now().minus(7,  ChronoUnit.DAYS);
            case "MONTH" -> Instant.now().minus(30, ChronoUnit.DAYS);
            default      -> LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
        };

        int total     = taskRepo.countByUserIdAndCreatedAtAfter(userId, from);
        int completed = taskRepo.countByUserIdAndStatusAndCreatedAtAfter(
                            userId, TaskStatus.COMPLETED, from);
        double rate   = total > 0
            ? Math.round(((double) completed / total) * 1000.0) / 10.0
            : 0.0;

        return new ProductivityResponse(completed, total, rate, period.toUpperCase());
    }
}

// analytics/application/WorkloadUseCase.java
@UseCase @RequiredArgsConstructor @Transactional(readOnly = true)
public class WorkloadUseCase {
    private final JpaTaskRepository taskRepo;
    private final TaskMapper        mapper;

    public WorkloadResponse execute(Long userId) {
        Instant now  = Instant.now();
        Instant week = now.plus(7, ChronoUnit.DAYS);

        long overdueCount  = taskRepo.count(
            Specification.where(hasUserId(userId)).and(isOverdue()));
        List<Task> upcoming = taskRepo.findByUserIdAndDueDateBetween(userId, now, week);

        return new WorkloadResponse(
            (int) overdueCount,
            upcoming.stream().map(mapper::toResponse).toList()
        );
    }
}

// analytics/api/AnalyticsController.java
@RestController @RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor @Tag(name = "Analytics")
public class AnalyticsController {
    private final ProductivityUseCase productivity;
    private final WorkloadUseCase     workload;

    @GetMapping("/productivity")
    public ProductivityResponse productivity(
        @AuthenticationPrincipal User user,
        @RequestParam(defaultValue="DAY") String period) {
        return productivity.execute(user.getId(), period);
    }

    @GetMapping("/workload")
    public WorkloadResponse workload(@AuthenticationPrincipal User user) {
        return workload.execute(user.getId());
    }
}

// test/TelegramWebhookTest.java
class TelegramWebhookTest extends IntegrationTestBase {

    @Test
    void webhook_without_auth_returns_200() {
        var update = Map.of("update_id", 1,
            "message", Map.of("message_id", 1,
                "from",  Map.of("id", 99999L, "first_name", "Test"),
                "chat",  Map.of("id", 99999L),
                "text",  "Привет"));
        var resp = rest.postForEntity("/api/v1/telegram/webhook", update, Void.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void productivity_returns_correct_rate() {
        String token = getToken("analytics@test.com", "pass12345");
        // Создать 2 задачи, завершить 1
        Long t1 = createTask(token, "Task 1").id();
        Long t2 = createTask(token, "Task 2").id();
        completeTask(token, t1);

        var resp = rest.exchange("/api/v1/analytics/productivity?period=DAY",
            HttpMethod.GET, new HttpEntity<>(authHeaders(token)), ProductivityResponse.class);
        assertThat(resp.getBody().completionRate()).isEqualTo(50.0);
    }
}
```

**✅ Критерий готовности:**
- `GET /api/v1/analytics/productivity?period=WEEK` → корректный `completionRate`
- `GET /api/v1/analytics/workload` → overdue count + upcoming список
- Тест аналитики математически проверяет `completionRate`

---

## Неделя 6 — Production-Ready
> **После недели:** Docker Compose запускает всё одной командой. OpenAPI документация.
> Rate Limiting. HikariCP настроен. Health checks. README для быстрого старта. Финальный E2E тест.

---

### День 19 — Docker: multi-stage build + Docker Compose `[сложный]`

**📚 Материалы:**
- 📹 TechWorld with Nana — [Docker Tutorial for Beginners](https://www.youtube.com/results?search_query=docker+tutorial+beginners+techworld+nana) (~45 мин)
- 📖 Spring Docs — [Spring Boot Docker](https://spring.io/guides/gs/spring-boot-docker)
- 📖 Docker Docs — [Multi-stage builds](https://docs.docker.com/build/building/multi-stage/)

**💡 Почему такое решение:**
Multi-stage build: первый stage собирает JAR (нужен JDK, ~450MB), второй только запускает (JRE, ~80MB). Итоговый образ в 4–5 раз меньше.

`adduser appuser` — приложение запускается от непривилегированного пользователя. Если контейнер скомпрометирован, атакующий не получит root на хосте.

`depends_on` с `condition: service_healthy` — приложение стартует только когда PostgreSQL готов принимать соединения. Flyway не упадёт с "connection refused".

`.dockerignore` — без него Docker копирует `target/`, `.git/`, `.env` в build context. С .dockerignore build в 10 раз быстрее.

Layered JAR (`-Djarmode=layertools`) — библиотеки (зависимости) в отдельном Docker layer. При пересборке только код приложения — слой зависимостей берётся из кэша.

**🔨 Задача:**

```dockerfile
# Dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Отдельный шаг для зависимостей — кэшируется если pom.xml не менялся
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw package -DskipTests -B && \
    java -Djarmode=layertools -jar target/*.jar extract

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Безопасность: непривилегированный пользователь
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Слоистая распаковка для максимального кэширования
COPY --from=builder /app/dependencies/          ./
COPY --from=builder /app/spring-boot-loader/    ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/           ./

USER appuser
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
    CMD wget -q -O /dev/null http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```

```yaml
# docker-compose.yml
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB:       klawa
      POSTGRES_USER:     klawa_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U klawa_user -d klawa"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    env_file: .env
    environment:
      DB_URL:                 jdbc:postgresql://postgres:5432/klawa
      SPRING_PROFILES_ACTIVE: prod
    depends_on:
      postgres:
        condition: service_healthy
    restart: unless-stopped

volumes:
  postgres_data:
```

```gitignore
# .dockerignore
target/
.git/
.env
*.md
.mvn/wrapper/maven-wrapper.jar
node_modules/
```

**✅ Критерий готовности:**
- `docker compose up --build` → `app` стартует после postgres
- `docker compose down -v && docker compose up --build` — повторный запуск без ошибок
- `docker images` — образ приложения меньше 200MB
- `docker exec <container> whoami` → `appuser` (не root)

---

### День 20 — OpenAPI + Rate Limiting + CORS + Security Headers `[средний]`

**📚 Материалы:**
- 📖 springdoc-openapi — [Getting Started](https://springdoc.org/#getting-started)
- 📖 Baeldung — [Rate Limiting with Bucket4j](https://www.baeldung.com/spring-bucket4j)
- 📖 MDN — [CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)
- 📖 OWASP — [Security Headers](https://owasp.org/www-project-secure-headers/)

**💡 Почему такое решение:**
OpenAPI — машиночитаемый контракт API. По нему автоматически генерируется клиентский код (TypeScript, Python SDK). Без него фронтенд-разработчик читает код или угадывает.

Swagger выключен в prod — `/swagger-ui.html` в продакшне это лишняя attack surface.

Rate limiting на AI endpoint — один запрос к Anthropic стоит денег. Без ограничений один пользователь может исчерпать месячную квоту за час.

Security headers:
- `X-Frame-Options: DENY` — защита от clickjacking
- `X-Content-Type-Options: nosniff` — браузер не угадывает MIME-тип
- `Referrer-Policy` — URL не утекает в referer

**🔨 Задача:**

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.10.1</version>
</dependency>
```

```java
// config/OpenApiConfig.java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info().title("AI Assistant Klawa API").version("1.0.0")
                .description("Персональный AI-ассистент для управления задачами"))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components().addSecuritySchemes("bearerAuth",
                new SecurityScheme().type(HTTP).scheme("bearer").bearerFormat("JWT")
                    .description("Вставь accessToken из /api/v1/auth/register")));
    }
}

// web/RateLimitInterceptor.java
@Component @RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    private final Map<Long, Bucket> userBuckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res,
                             Object handler) throws Exception {
        if (!req.getRequestURI().startsWith("/api/v1/agent/chat")) return true;

        User user = (User) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();

        Bucket bucket = userBuckets.computeIfAbsent(user.getId(), k ->
            Bucket.builder()
                .addLimit(Bandwidth.builder()
                    .capacity(20).refillGreedy(20, Duration.ofHours(1)).build())
                .build());

        if (bucket.tryConsume(1)) return true;

        res.setStatus(429);
        res.setContentType("application/json");
        res.getWriter().write("""
            {"status":429,"code":"RATE_LIMIT_EXCEEDED",
             "message":"Too many AI requests. Limit: 20 per hour.",
             "traceId":"%s"}""".formatted(MDC.get("traceId")));
        return false;
    }
}

// config/SecurityConfig.java — добавить в filterChain
http
    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
    .headers(headers -> headers
        .frameOptions(FrameOptionsConfig::deny)
        .contentTypeOptions(Customizer.withDefaults())
        .referrerPolicy(r ->
            r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN))
    );

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of("http://localhost:*","https://your-domain.com"));
    config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization","Content-Type","X-Trace-Id"));
    config.setExposedHeaders(List.of("X-Trace-Id"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}
```

```yaml
# application-dev.yaml
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
    try-it-out-enabled: true

# application-prod.yaml
springdoc:
  swagger-ui:
    enabled: false
  api-docs:
    enabled: false
```

**✅ Критерий готовности:**
- `http://localhost:8080/swagger-ui.html` — все endpoint'ы видны, авторизация работает
- `POST /api/v1/agent/chat` 21-й раз → 429 с `RATE_LIMIT_EXCEEDED`
- Response headers: `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`
- В prod профиле `/swagger-ui.html` → 404

---

### День 21 — HikariCP + Actuator + Custom Health Indicator `[средний]`

**📚 Материалы:**
- 📖 HikariCP — [Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- 📖 Baeldung — [Spring Boot Actuator](https://www.baeldung.com/spring-boot-actuators)
- 📖 Baeldung — [Custom Health Indicator](https://www.baeldung.com/spring-boot-health-indicators)

**💡 Почему такое решение:**
HikariCP — пул соединений к БД. По умолчанию 10 соединений. Правило Hikari: `(core_count × 2) + disk_count`. Для 2-ядерного сервера: `(2×2)+1 = 5` — оптимум, не 10.

`connection-timeout: 5000` — 5 сек вместо дефолтных 30. Быстрая понятная ошибка лучше чем зависший запрос на 30 секунд.

Custom Health Indicator для Anthropic — `/actuator/health` показывает состояние circuit breaker. Если AI недоступен — monitoring (Grafana, Datadog) видит деградацию и может алертить.

**🔨 Задача:**

```yaml
# application.yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 3
      connection-timeout: 5000
      idle-timeout: 300000
      max-lifetime: 1800000
      connection-test-query: SELECT 1
      pool-name: KlawaPool

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,circuitbreakers
  endpoint:
    health:
      show-details: when_authorized
      show-components: when_authorized
  health:
    circuitbreakers:
      enabled: true
```

```java
// infrastructure/health/AnthropicHealthIndicator.java
@Component @RequiredArgsConstructor
public class AnthropicHealthIndicator implements HealthIndicator {
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Override
    public Health health() {
        CircuitBreaker cb    = circuitBreakerRegistry.circuitBreaker("anthropic");
        CircuitBreaker.State state = cb.getState();

        return switch (state) {
            case CLOSED    -> Health.up()
                .withDetail("state", "CLOSED")
                .withDetail("failureRate", cb.getMetrics().getFailureRate() + "%")
                .build();
            case OPEN      -> Health.down()
                .withDetail("state", "OPEN")
                .withDetail("reason", "Too many failures").build();
            case HALF_OPEN -> Health.status("RECOVERING")
                .withDetail("state", "HALF_OPEN").build();
            default        -> Health.unknown().build();
        };
    }
}
```

```yaml
# application-prod.yaml — скрыть actuator за нестандартным путём
management:
  endpoints:
    web:
      base-path: /internal/actuator
      exposure:
        include: health,info
```

**✅ Критерий готовности:**
- `GET /actuator/health` → `{"status":"UP","components":{"anthropic":...,"db":...}}`
- `GET /actuator/metrics/hikaricp.connections.active` → число активных соединений
- При ошибках Anthropic → `components.anthropic.status: "DOWN"`

---

### День 22 — Финальные тесты: E2E + Security `[средний]`

**📚 Материалы:**
- 📖 Baeldung — [Spring Security Test](https://www.baeldung.com/spring-security-integration-tests)
- 📖 Spring Docs — [MockMvc Security](https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/index.html)

**💡 Почему такое решение:**
E2E тест — smoke test всей системы: убеждаемся что все части работают вместе. Не мокируем ничего кроме внешних API (Anthropic, Telegram).

Security тесты — проверяем что защита работает: `/actuator` не открыт без авторизации, все `/api/**` требуют JWT, `/api/v1/auth/**` публичны.

**🔨 Задача:**

```java
// test/E2EJourneyTest.java
class E2EJourneyTest extends IntegrationTestBase {

    @Test
    void full_user_journey_works_end_to_end() {
        // 1. Регистрация
        String token = getToken("journey@test.com", "password123");

        // 2. Создать проект
        var project = createProject(token, "Мой проект");
        assertThat(project.name()).isEqualTo("Мой проект");

        // 3. Создать задачу в проекте
        var task = createTask(token, "Написать тесты", project.id());
        assertThat(task.status()).isEqualTo(TaskStatus.OPEN);
        assertThat(task.projectName()).isEqualTo("Мой проект");

        // 4. Перевести в IN_PROGRESS
        var inProgress = updateTaskStatus(token, task.id(), TaskStatus.IN_PROGRESS);
        assertThat(inProgress.status()).isEqualTo(TaskStatus.IN_PROGRESS);

        // 5. Завершить задачу
        var completed = completeTask(token, task.id());
        assertThat(completed.status()).isEqualTo(TaskStatus.COMPLETED);

        // 6. Аналитика показывает 100%
        var analytics = rest.exchange(
            "/api/v1/analytics/productivity?period=DAY",
            HttpMethod.GET, new HttpEntity<>(authHeaders(token)),
            ProductivityResponse.class).getBody();
        assertThat(analytics.completionRate()).isGreaterThanOrEqualTo(100.0);

        // 7. AI чат работает (MockAiClient)
        var chat = rest.exchange("/api/v1/agent/chat",
            HttpMethod.POST,
            new HttpEntity<>(new ChatRequest("Как дела с задачами?", null), authHeaders(token)),
            ChatResponse.class).getBody();
        assertThat(chat.reply()).isNotBlank();
        assertThat(chat.conversationId()).isNotNull();

        // 8. История чата сохранена
        var history = rest.exchange(
            "/api/v1/agent/conversations/" + chat.conversationId() + "/messages",
            HttpMethod.GET, new HttpEntity<>(authHeaders(token)),
            new ParameterizedTypeReference<Page<MessageResponse>>() {}).getBody();
        assertThat(history.getTotalElements()).isEqualTo(2); // USER + ASSISTANT
    }
}

// test/SecurityTest.java
class SecurityTest extends IntegrationTestBase {

    @Test
    void all_api_endpoints_require_jwt() {
        List<String> protectedEndpoints = List.of(
            "/api/v1/tasks", "/api/v1/users/me",
            "/api/v1/agent/chat", "/api/v1/memories"
        );
        protectedEndpoints.forEach(url -> {
            var resp = rest.getForEntity(url, ApiError.class);
            assertThat(resp.getStatusCode())
                .as("Endpoint %s should require auth", url)
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        });
    }

    @Test
    void auth_endpoints_are_public() {
        var resp = rest.postForEntity("/api/v1/auth/register",
            new RegisterRequest("A","B","sec@test.com","pass12345"), Object.class);
        assertThat(resp.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void rate_limiting_returns_429_after_limit() {
        String token = getToken("rate@test.com", "password123");
        // 20 запросов — в пределах лимита
        for (int i = 0; i < 20; i++) {
            rest.exchange("/api/v1/agent/chat", HttpMethod.POST,
                new HttpEntity<>(new ChatRequest("msg " + i, null), authHeaders(token)),
                ChatResponse.class);
        }
        // 21-й — превышает лимит
        var resp = rest.exchange("/api/v1/agent/chat", HttpMethod.POST,
            new HttpEntity<>(new ChatRequest("over limit", null), authHeaders(token)),
            ApiError.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(resp.getBody().code()).isEqualTo("RATE_LIMIT_EXCEEDED");
    }
}
```

**✅ Критерий готовности:**
- E2E тест: все 8 шагов зелёные
- Security тест: все публичные и защищённые endpoint'ы проверены
- Rate limiting тест работает без реального AI (MockAiClient)

---

### День 23 — Рефакторинг: code review + удаление мёртвого кода `[лёгкий]`

**📚 Материалы:**
- 📖 Baeldung — [Clean Code in Java](https://www.baeldung.com/java-clean-code)
- 📖 SOLID — [Baeldung: SOLID Principles](https://www.baeldung.com/solid-principles)

**💡 Почему такое решение:**
Технический долг накапливается незаметно. Один день в конце проекта на ревью кода позволяет найти и исправить: `return null` (должен быть `Optional`), `System.out.println` вместо логгера, import неиспользуемых классов, дублирующийся код в контроллерах.

`mvn dependency:analyze` — показывает неиспользуемые зависимости в pom.xml. Меньше зависимостей = меньше уязвимостей = быстрее сборка.

**🔨 Задача — Чеклист ревью:**

```
[ ] Каждый контроллер — только DTO в параметрах и возвращаемых типах
[ ] Каждый use case — @Transactional или @Transactional(readOnly=true)
[ ] Нет return null в контроллерах (заменить на Optional или исключение)
[ ] Нет System.out.println — везде @Slf4j + log.info/warn/error
[ ] Все entity наследуют BaseEntity (createdAt, updatedAt, deletedAt)
[ ] Все endpoint'ы в TaskController, AuthController аннотированы @Operation
[ ] Нет циклических зависимостей между модулями
[ ] Нет кода вида: if (isDev) { ... }
[ ] @Scheduled метод вызывается только из самого класса (не из другого)
[ ] Kafka, RabbitMQ, лишние стартеры — удалены из pom.xml если не используются
[ ] mvn dependency:analyze — нет "Used undeclared" зависимостей
[ ] mvn test — все тесты зелёные
```

**✅ Критерий готовности:**
- Все пункты чеклиста отмечены
- `mvn test` — зелёный после рефакторинга
- `mvn dependency:analyze` — нет критических предупреждений

---

### День 24 — README + Production Checklist + Финальный запуск `[лёгкий]`

**📚 Материалы:**
- 📖 The Twelve-Factor App — [12factor.net](https://12factor.net/)
- 📖 Spring Boot Docs — [Production Checklist](https://docs.spring.io/spring-boot/reference/deployment/index.html)

**💡 Почему такое решение:**
README — первое что видит человек (включая тебя через 3 месяца). Хороший README: `git clone` + `cp .env.example .env` + `docker compose up` = работающая система. Без инструкции — 30 минут разбора.

Production checklist — формализованный список проверок перед деплоем. В командах называется "release checklist" или "go-live checklist". Проверяется перед каждым релизом.

**🔨 Задача — написать README.md:**

```markdown
# AI Assistant Klawa

Персональный AI-ассистент для управления задачами с Telegram интеграцией.

## Технологии
- Java 21, Spring Boot 3.x
- PostgreSQL 16, Flyway
- Anthropic Claude API
- Telegram Bot API
- Docker, Docker Compose

## Быстрый старт

### Требования
- Docker & Docker Compose
- Anthropic API ключ (получить на console.anthropic.com)
- Telegram Bot Token (создать через @BotFather)

### Запуск
git clone <repo_url>
cp .env.example .env
# Заполнить .env реальными значениями
docker compose up --build

### Документация API
http://localhost:8080/swagger-ui.html (только в dev)

## Переменные окружения
| Переменная | Описание |
|---|---|
| DB_PASSWORD | Пароль PostgreSQL |
| JWT_SECRET | Секрет JWT, минимум 32 символа |
| ANTHROPIC_API_KEY | Ключ API Anthropic |
| TELEGRAM_BOT_TOKEN | Токен Telegram бота |
| TELEGRAM_WEBHOOK_URL | Публичный URL для webhook |

## Архитектура модулей
- **user** — регистрация, JWT авторизация, refresh tokens
- **task** — задачи и проекты, soft delete, state machine статусов
- **reminder** — напоминания, планировщик, события
- **notification** — уведомления через события + Telegram
- **agent** — AI чат (Anthropic), история, memory
- **analytics** — статистика продуктивности
- **telegram** — webhook, привязка аккаунта (deep linking)
- **shared** — BaseEntity, ApiError, трассировка (MDC)
```

**Финальный production checklist:**
```
Инфраструктура:
[ ] docker compose up работает с нуля (чистый том)
[ ] docker compose down -v && docker compose up — без ошибок
[ ] Docker образ запускается от appuser (не root)
[ ] Образ меньше 200MB

БД и миграции:
[ ] flyway_schema_history — 4 миграции с success=true
[ ] Нет ddl-auto: create или update в production профиле
[ ] Все FK имеют индексы

Безопасность:
[ ] git grep -r "password:" src/ — пусто
[ ] git grep -r "secret:" src/ — пусто
[ ] JWT expiration <= 15 минут (access token)
[ ] Refresh token rotation работает
[ ] Rate limiting 429 после 20 AI запросов
[ ] Response headers: X-Frame-Options, X-Content-Type-Options
[ ] Swagger недоступен в prod профиле

API и логи:
[ ] Все ошибки возвращают ApiError с traceId
[ ] Нет stack trace в JSON ответах
[ ] В логах каждая строка содержит [traceId]
[ ] Нет System.out.println в коде

Функциональность:
[ ] Register → Login → Create task → Complete → Analytics = 100%
[ ] PENDING reminder → через 1 мин → notification в БД
[ ] /actuator/health → UP (с деталями компонентов)
[ ] Circuit breaker видно в health endpoint
[ ] mvn test — все тесты зелёные

Документация:
[ ] README — инструкция запуска с нуля
[ ] .env.example содержит все необходимые переменные
[ ] Swagger аннотации на всех контроллерах
```

**✅ Критерий готовности:**
- Все пункты production checklist выполнены
- README позволяет запустить проект с нуля без дополнительных вопросов
- `mvn test` зелёный финальный прогон

---

## Итоговая архитектура системы

```
Клиент (браузер / мобилка)      Telegram Bot API
              │                          │
              └────────────┬─────────────┘
                      HTTPS │
              ┌─────────────▼──────────────────────┐
              │           Spring Boot               │
              │                                     │
              │  ① RequestIdFilter  (MDC traceId)  │
              │  ② RateLimitInterceptor             │
              │  ③ JwtAuthenticationFilter          │
              │                                     │
              │  ┌────────────────────────────┐     │
              │  │   Контроллеры (api/)        │     │
              │  │   Только DTO, @Valid        │     │
              │  └──────────┬─────────────────┘     │
              │             │                        │
              │  ┌──────────▼─────────────────┐     │
              │  │   Use Cases (application/) │     │
              │  │   @Transactional           │     │
              │  │   @Cacheable / @Async      │     │
              │  │   @PreAuthorize            │     │
              │  └────┬─────────────────┬─────┘     │
              │       │                 │             │
              │  Domain/          Infrastructure/    │
              │  Entities         JpaRepository      │
              │  Interfaces       AnthropicClient ───┼──► Anthropic API
              │  Events           TelegramClient  ───┼──► Telegram API
              │       │           (CircuitBreaker)   │
              │  ┌────▼──────────────────────┐       │
              │  │   ApplicationEvents        │       │
              │  │   ReminderDueEvent         │       │
              │  │   @EventListener @Async    │       │
              │  └────────────────────────────┘      │
              └─────────────┬──────────────────────── ┘
                            │
                     ┌──────▼──────┐
                     │ PostgreSQL  │
                     │ HikariCP    │
                     │ Flyway      │
                     └─────────────┘
```

## Шпаргалки

### Команды каждый день
```bash
# Запуск
export $(cat .env | xargs) && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Тесты
mvn test

# Docker
docker compose up --build
docker compose down -v   # с очисткой данных

# Проверить нет секретов
git grep -r "password:" src/ && git grep -r "secret:" src/

# История миграций
psql -U klawa_user -d klawa \
  -c "SELECT version, description, success FROM flyway_schema_history;"

# Метрики пула соединений
curl -s http://localhost:8080/actuator/metrics/hikaricp.connections.active | jq

# Состояние Circuit Breaker
curl -s http://localhost:8080/actuator/health | jq '.components.anthropic'
```

### Проверочные вопросы перед каждым коммитом
1. Контроллер возвращает только DTO?
2. Use case имеет `@Transactional` или `@Transactional(readOnly=true)`?
3. Новые таблицы созданы через Flyway (`V_N__description.sql`)?
4. Новые поля в WHERE-условиях покрыты индексами?
5. Внешний HTTP вызов обёрнут в `@CircuitBreaker` или `@Retry`?
6. `mvn test` зелёный?

### Структура нового модуля
```
module/
  api/
    XController.java          ← @RestController, только DTO
    CreateXRequest.java       ← record с валидацией
    UpdateXRequest.java       ← record для PUT/PATCH
    XResponse.java            ← record — ответ клиенту
    XFilter.java              ← record — параметры фильтрации
  application/
    CreateXUseCase.java       ← @UseCase @Transactional
    GetXUseCase.java          ← @UseCase @Transactional(readOnly=true)
    UpdateXUseCase.java       ← @UseCase @Transactional
    DeleteXUseCase.java       ← @UseCase @Transactional (soft delete)
    XMapper.java              ← @Component entity ↔ DTO
  domain/
    X.java                    ← @Entity extends BaseEntity
    XStatus.java              ← enum (если нужен)
    XRepository.java          ← interface — порт (impl в infrastructure)
  infrastructure/
    JpaXRepository.java       ← implements XRepository + JpaRepository<X,Long>
    XSpecifications.java      ← static Specification<X> методы для фильтров
```

---

## Неделя 7 — Трудоустройство
> **После недели:** GitHub репо выглядит профессионально, CI зелёный, ты умеешь отвечать  
> на 90% вопросов интервью, резюме написано, первые отклики отправлены.

---

### День 25 — Git workflow + GitHub репо + README с бейджами `[лёгкий]`

**📚 Материалы:**
- 📖 Conventional Commits — [conventionalcommits.org](https://www.conventionalcommits.org/en/v1.0.0/)
- 📖 GitHub Docs — [About README](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-readmes)
- 📖 Shields.io — [README Badges](https://shields.io/)

**💡 Почему такое решение:**
Работодатель открывает твой GitHub ещё до того как читает резюме. Первые 10 секунд — это README и история коммитов. Репо без README и с сообщениями типа `fix`, `updated`, `changes` выглядит как учебная работа. Репо с осмысленными коммитами, зелёным CI бейджем и структурированным README выглядит как production проект.

Conventional Commits — стандарт который читается как changelog: `feat`, `fix`, `test`, `docs`, `refactor`, `chore`. По этим префиксам можно автоматически генерировать CHANGELOG.md и видеть историю проекта.

Ретроактивно переписать историю коммитов нельзя (не стоит). Но начать правильно вести её с сегодняшнего дня — можно и нужно.

**🔨 Задача:**

Создать публичный репозиторий на GitHub, залить проект, настроить README.

**Правила коммитов:**
```bash
# Формат: тип(область): описание в настоящем времени

git commit -m "feat(auth): add refresh token rotation with reuse detection"
git commit -m "feat(task): implement soft delete with @Where clause"
git commit -m "feat(task): add state machine for status transitions"
git commit -m "fix(scheduler): prevent duplicate notification on reminder retry"
git commit -m "test(task): cover all state machine transitions with @ParameterizedTest"
git commit -m "test(auth): verify refresh token rotation invalidates old token"
git commit -m "docs: update README with docker compose quick start"
git commit -m "chore: configure HikariCP pool size and connection timeout"
git commit -m "refactor(agent): extract PromptBuilder from ChatUseCase"
git commit -m "perf(user): add Caffeine cache to UserDetailsService"
```

**Типы:** `feat` (новая функция), `fix` (баг), `test` (тесты), `docs` (документация), `refactor` (рефакторинг без изменения поведения), `chore` (конфиг, зависимости), `perf` (оптимизация)

**README.md — финальная версия:**
```markdown
# AI Assistant Klawa

Персональный AI-ассистент для управления задачами с Telegram интеграцией.
Построен на Spring Boot 3 с Anthropic Claude API.

![CI](https://github.com/USERNAME/ai-assistant-klawa/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)

## Возможности

- Управление задачами и проектами с soft delete и state machine переходов статусов
- AI-чат через Anthropic Claude с историей разговоров и персонализированным контекстом
- Умные напоминания: планировщик → событие → Telegram уведомление
- Telegram бот с Deep Linking для привязки аккаунта
- Аналитика продуктивности (completionRate, overdue, upcoming)

## Архитектура

```
user/ task/ reminder/ notification/ agent/ memory/ telegram/ analytics/ shared/
  └── api/           ← DTO, контроллеры (@Valid, @Tag)
  └── application/   ← Use Cases (@Transactional, @Cacheable, @Async)
  └── domain/        ← JPA entities (extends BaseEntity), enums, interfaces
  └── infrastructure/← JPA реализации, RestClient адаптеры
```

**Ключевые технические решения:**
- Refresh Token Rotation (OAuth 2.0) — повторное использование = инвалидация всех сессий
- Soft Delete через `@Where(clause = "deleted_at IS NULL")` + частичные индексы
- State Machine переходов статусов задач в domain entity
- Circuit Breaker (Resilience4j) для Anthropic API с fallback
- Caffeine кэш для `UserDetailsService` — нет N+1 при аутентификации
- MDC трассировка — `traceId` в каждом лог-сообщении и HTTP заголовке

## Быстрый старт

**Требования:** Docker & Docker Compose, Anthropic API ключ, Telegram Bot Token

```bash
git clone https://github.com/USERNAME/ai-assistant-klawa
cd ai-assistant-klawa
cp .env.example .env
# Заполнить .env реальными значениями
docker compose up --build
```

API документация: http://localhost:8080/swagger-ui.html _(только dev профиль)_

## Конфигурация

| Переменная | Описание | Пример |
|---|---|---|
| `DB_PASSWORD` | Пароль PostgreSQL | `changeme` |
| `JWT_SECRET` | Секрет JWT (≥32 символа) | `your-secret-key...` |
| `ANTHROPIC_API_KEY` | Ключ Anthropic Claude API | `sk-ant-...` |
| `TELEGRAM_BOT_TOKEN` | Токен Telegram бота | `123456:ABC...` |
| `TELEGRAM_WEBHOOK_URL` | Публичный URL для webhook | `https://domain.com` |

## Тестирование

```bash
mvn test                    # все тесты (Testcontainers PostgreSQL)
mvn test -Dtest=TaskControllerTest   # конкретный класс
mvn test -pl task           # конкретный модуль
```

## Стек

Java 21 · Spring Boot 3 · Spring Security · Spring Data JPA · Flyway · PostgreSQL ·
Resilience4j · Caffeine · Bucket4j · Testcontainers · Docker · springdoc OpenAPI
```

**✅ Критерий готовности:**
- Репо публичный, README открывается и выглядит профессионально
- Последние 10 коммитов написаны в Conventional Commits формате
- README содержит бейджи (CI появится после следующего дня)
- В описании репо на GitHub: краткое описание + ссылка на Swagger

---

### День 26 — GitHub Actions: CI пайплайн + зелёный бейдж `[средний]`

**📚 Материалы:**
- 📖 GitHub Docs — [GitHub Actions Quickstart](https://docs.github.com/en/actions/quickstart)
- 📖 GitHub Docs — [PostgreSQL Service Container](https://docs.github.com/en/actions/use-cases-and-examples/using-containerized-services/creating-postgresql-service-containers)
- 📖 Baeldung — [GitHub Actions with Maven](https://www.baeldung.com/spring-boot-github-actions)

**💡 Почему такое решение:**
CI пайплайн на GitHub Actions — это первое что интервьюер проверяет после README. Зелёный бейдж означает: тесты запускаются автоматически при каждом push, код не сломан. Репо без CI выглядит как учебный проект. Репо с CI выглядит как production.

Testcontainers в CI работают через `services` в GitHub Actions — PostgreSQL стартует как Docker контейнер рядом с job. Не нужен H2, не нужна моковая БД — те же тесты что локально.

`actions/cache@v4` для Maven — зависимости кэшируются между запусками. Без кэша каждый run скачивает ~200MB зависимостей. С кэшем — секунды.

**🔨 Задача:**

Создать файл `.github/workflows/ci.yml`:

```yaml
name: CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB:       klawa_test
          POSTGRES_USER:     klawa
          POSTGRES_PASSWORD: klawa
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Cache Maven packages
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-maven-

      - name: Run tests
        env:
          DB_URL:        jdbc:postgresql://localhost:5432/klawa_test
          DB_USERNAME:   klawa
          DB_PASSWORD:   klawa
          JWT_SECRET:    test-secret-key-minimum-32-characters-long
          JWT_EXPIRATION: 900000
          ANTHROPIC_API_KEY: test-key-not-used-mock-is-active
          TELEGRAM_BOT_TOKEN: test-token
          TELEGRAM_WEBHOOK_URL: https://example.com
          SPRING_PROFILES_ACTIVE: test
        run: mvn test -B --no-transfer-progress

      - name: Upload test results
        uses: actions/upload-artifact@v4
        if: failure()
        with:
          name: test-results
          path: target/surefire-reports/
```

Добавить в начало README.md:
```markdown
![CI](https://github.com/ВАШ_USERNAME/ai-assistant-klawa/actions/workflows/ci.yml/badge.svg)
```

**application-test.yaml** — убедиться что MockAiClient используется:
```yaml
spring:
  profiles:
    active: test
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  flyway:
    enabled: true
```

**✅ Критерий готовности:**
- `git push` → в GitHub Actions вкладке запускается пайплайн
- Все тесты зелёные в CI
- В README появился зелёный бейдж `CI passing`
- При падении теста — в артефактах CI лежат surefire отчёты

---

### День 27 — N+1 проблема: обнаружить + исправить тремя способами `[сложный]`

**📚 Материалы:**
- 📖 Vlad Mihalcea — [N+1 Query Problem](https://vladmihalcea.com/n-plus-1-query-problem/) — лучший материал по теме
- 📖 Baeldung — [@EntityGraph](https://www.baeldung.com/spring-data-jpa-named-entity-graphs)
- 📖 Baeldung — [Hibernate Batch Fetching](https://www.baeldung.com/hibernate-fetchmode)
- 📹 [N+1 Problem in Spring Boot](https://www.youtube.com/results?search_query=n+1+problem+spring+boot+jpa+hibernate) (~20 мин)

**💡 Почему такое решение:**
N+1 — топ-1 вопрос на каждом Java собеседовании. Нужно не просто знать что это такое, а уметь: найти N+1 в логах (`show-sql: true`), объяснить почему LAZY загрузка его вызывает, и исправить тремя способами в зависимости от контекста.

В твоём проекте N+1 возникает в `TaskMapper::toResponse` — при маппинге `task.getProject().getName()` каждая задача делает отдельный SELECT для загрузки проекта, если задачи загружены без JOIN.

**🔨 Задача:**

Обнаружить и исправить N+1 в `GetTasksUseCase`:

```java
// КАК ОБНАРУЖИТЬ: включить show-sql и посмотреть в логи
// GET /api/v1/tasks с 10 задачами → в логах 11 SELECT:
// SELECT * FROM tasks WHERE user_id = ?           -- 1 запрос
// SELECT * FROM projects WHERE id = ?             -- 10 раз для каждой задачи
// Итого: 11 запросов вместо 1 с JOIN

// СПОСОБ 1: @EntityGraph на методе репозитория
// Добавляет LEFT JOIN FETCH автоматически к любому методу
@Repository
public interface JpaTaskRepository
    extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    @EntityGraph(attributePaths = {"project", "user"})
    Page<Task> findAll(Specification<Task> spec, Pageable pageable);

    // Для findByIdAndUserId тоже нужен @EntityGraph если маппишь проект
    @EntityGraph(attributePaths = {"project"})
    Optional<Task> findByIdAndUserId(Long id, Long userId);
}

// СПОСОБ 2: JOIN FETCH в кастомном @Query
@Query("""
    SELECT t FROM Task t
    LEFT JOIN FETCH t.project
    LEFT JOIN FETCH t.user
    WHERE t.user.id = :userId
    AND (:status IS NULL OR t.status = :status)
    """)
List<Task> findWithProjectAndUser(@Param("userId") Long userId,
                                  @Param("status") TaskStatus status);

// СПОСОБ 3: Batch fetching в конфиге — самый простой, не меняет запросы
// application.yaml:
// spring.jpa.properties.hibernate.default_batch_fetch_size: 20
// Hibernate загрузит проекты для 20 задач за 1 запрос вместо 20
```

Написать тест который проверяет количество запросов:
```java
// test/N1QueryTest.java
@SpringBootTest @Testcontainers @ActiveProfiles("test")
class N1QueryTest extends IntegrationTestBase {

    @Autowired DataSource dataSource;

    @Test
    void get_tasks_makes_constant_number_of_queries() throws Exception {
        String token = getToken("n1@test.com", "pass12345");
        // создать 10 задач
        for (int i = 0; i < 10; i++) createTask(token, "Task " + i);

        // Подсчитать SQL запросы через P6Spy или простой подсчёт в show-sql
        // Ожидаем: 1-2 SELECT (tasks + optional projects JOIN), не 11
        var resp = rest.exchange("/api/v1/tasks", HttpMethod.GET,
            new HttpEntity<>(authHeaders(token)),
            new ParameterizedTypeReference<Page<TaskResponse>>() {});

        assertThat(resp.getBody().getTotalElements()).isEqualTo(10);
        // Визуально проверить логи: должен быть 1 SELECT с LEFT JOIN, не 11
    }
}
```

**✅ Критерий готовности:**
- `GET /api/v1/tasks` с 10 задачами → в логах 1–2 SELECT (с JOIN), не 11
- Умеешь объяснить разницу между тремя способами решения
- Знаешь ответ на вопрос "когда @EntityGraph, а когда JOIN FETCH"

---

### День 28 — @Transactional глубже: ловушки, propagation, изоляция `[сложный]`

**📚 Материалы:**
- 📖 Baeldung — [Spring @Transactional](https://www.baeldung.com/transaction-configuration-with-jpa-and-spring)
- 📖 Baeldung — [Transactional Propagation](https://www.baeldung.com/spring-transactional-propagation-isolation)
- 📖 Vlad Mihalcea — [@Transactional Pitfalls](https://vladmihalcea.com/spring-transactional-annotation/)

**💡 Почему такое решение:**
`@Transactional` спрашивают в 90% интервью по Spring. Мало сказать "это транзакция". Нужно знать три главные ловушки наизусть — именно их и проверяют.

**🔨 Задача:**

Изучить три ловушки и убедиться что в твоём проекте их нет:

```java
// ЛОВУШКА 1: Self-invocation — транзакция не создаётся
// Spring создаёт прокси вокруг бина. Вызов через this обходит прокси.
@Service class TaskService {
    public void doA() {
        this.doB(); // вызов через this — прокси не участвует
    }
    @Transactional
    public void doB() { ... } // @Transactional ИГНОРИРУЕТСЯ!
}
// В твоём проекте это не проблема: каждый UseCase — отдельный Spring бин.
// CreateTaskUseCase не вызывает CompleteTaskUseCase через this.

// ЛОВУШКА 2: Checked Exception не откатывает транзакцию
@Transactional
public void process() throws IOException {
    repo.save(entity);      // сохранилось
    throw new IOException(); // транзакция НЕ откатится (только RuntimeException)
}
// Исправление:
@Transactional(rollbackFor = IOException.class)

// ЛОВУШКА 3: LazyInitializationException вне транзакции
// Сессия Hibernate закрывается в конце @Transactional метода.
Task task = taskRepo.findById(1L).get(); // транзакция из репозитория закрылась
String name = task.getProject().getName(); // BOOM: LazyInitializationException
// В твоём проекте решено через @EntityGraph или open-in-view: false (правильно)

// PROPAGATION — когда нужно:
@Transactional(propagation = Propagation.REQUIRES_NEW)
// Используй когда нужно зафиксировать запись независимо от внешней транзакции.
// Пример: аудит-лог должен сохраниться даже если основная операция откатилась.
public void saveAuditLog(String event) {
    auditRepo.save(new AuditEntry(event)); // фиксируется отдельно
}

// ISOLATION — когда нужно:
@Transactional(isolation = Isolation.REPEATABLE_READ)
// READ_COMMITTED (по умолчанию в PostgreSQL) — можно читать зафиксированные данные
// REPEATABLE_READ — повторное чтение той же строки даст тот же результат
// SERIALIZABLE — полная изоляция, медленно, редко нужно
```

Чеклист по своему проекту:
```
[ ] Все UseCase методы имеют @Transactional (или readOnly=true для GET)
[ ] Нигде нет вызова @Transactional метода через this внутри того же класса
[ ] Нет мест где entity загружается вне транзакции и потом читается lazy поле
[ ] RuntimeException везде (не checked) — или явно указан rollbackFor
[ ] @Async методы не участвуют в транзакции вызывающего метода (это правильно)
```

**✅ Критерий готовности:**
- Умеешь объяснить self-invocation проблему с примером кода
- Знаешь разницу REQUIRES и REQUIRES_NEW и когда каждый нужен
- Можешь объяснить почему `readOnly = true` ускоряет запросы

---

### День 29 — Подготовка к интервью: отвечать по своему проекту `[средний]`

**📚 Материалы:**
- 📖 Baeldung — [Spring Boot Interview Questions](https://www.baeldung.com/spring-boot-interview-questions)
- 📖 Spring Docs — [Bean Scopes](https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html)
- 📹 [Spring Boot Interview Questions 2024](https://www.youtube.com/results?search_query=spring+boot+interview+questions+2024) (~30 мин)

**💡 Почему такое решение:**
Интервьюер берёт твоё резюме и спрашивает по каждому пункту. Нужно уметь объяснить КАЖДОЕ решение в проекте за 30–60 секунд. Не "я использовал Caffeine", а "я использовал Caffeine потому что `loadUserByUsername` вызывается при каждом HTTP запросе, без кэша это N SELECT в минуту".

STAR-формат (Situation → Task → Action → Result) — стандарт для ответов на поведенческие вопросы.

**🔨 Задача — выучить ответы на топ-вопросы:**

**"Расскажи о своём проекте" (2 минуты, всегда первый вопрос):**
```
Это AI-ассистент для управления задачами с Telegram интеграцией.
Построен на Spring Boot 3, PostgreSQL, Anthropic Claude API.

Архитектура — модульный монолит: 8 модулей (user, task, reminder, notification,
agent, memory, telegram, analytics), каждый с тремя слоями: api (DTO, контроллеры),
application (use cases с @Transactional), infrastructure (JPA, HTTP клиенты).

Из интересных технических решений:
- Refresh token rotation по OAuth 2.0: повторное использование старого refresh
  инвалидирует все сессии пользователя
- State machine в domain entity: Task::transitionTo() проверяет допустимость
  перехода OPEN → IN_PROGRESS → COMPLETED, нельзя обойти через контроллер
- Circuit Breaker через Resilience4j: при сбое Anthropic API пользователь
  получает fallback вместо зависшего запроса
- Тесты с Testcontainers: реальный PostgreSQL в Docker, не H2

Задеплоен через Docker Compose, образ 150MB (multi-stage build).
```

**Топ-15 вопросов которые спросят по проекту:**

| Вопрос | Ключевые слова ответа |
|---|---|
| Зачем Flyway вместо ddl-auto? | Версионирование схемы, история, воспроизводимость, prod-safe |
| Что такое N+1? | LAZY + цикл = N SELECT, решение: @EntityGraph / JOIN FETCH |
| Self-invocation @Transactional | Spring прокси, вызов через this обходит прокси |
| LAZY vs EAGER | LAZY = по требованию, EAGER = сразу; всегда LAZY для @ManyToOne |
| Зачем records для DTO? | Иммутабельность, нет boilerplate, Java 16+ |
| Зачем Caffeine не Redis? | Один инстанс = in-process достаточно; Redis нужен при scale-out |
| Что такое Circuit Breaker? | CLOSED → OPEN (ошибки) → HALF_OPEN (проба) → CLOSED |
| Зачем Testcontainers? | H2 не эмулирует PostgreSQL: нет TIMESTAMPTZ, частичных индексов |
| Что такое Soft Delete? | @Where(deleted_at IS NULL), данные не теряются, частичный индекс |
| Зачем @Async для ExtractMemory? | Не блокировать ответ чата, фоновая задача |
| Зачем MDC traceId? | Сквозная трассировка запроса через все логи |
| open-in-view: false зачем? | Закрывает JPA сессию до HTTP слоя, предотвращает N+1 в view |
| Bean Scope @Service? | Singleton — один объект, stateless, thread-safe |
| @PreAuthorize vs ручная проверка? | Defense in depth, дополнительный слой если пропустил в use case |
| UUID vs BIGSERIAL для conversations? | UUID не раскрывает количество записей, безопаснее для ID в URL |

**Практика ответов:**
Возьми каждый вопрос из таблицы и проговори ответ вслух за 60 секунд. Без подглядывания. Запиши ответ на видео и посмотри — так находятся слова-паразиты и неуверенность.

**✅ Критерий готовности:**
- "Расскажи о проекте" рассказываешь без запинок за 2 минуты
- На 10 из 15 вопросов в таблице отвечаешь без подглядывания
- Для каждого технического решения в проекте можешь назвать причину "почему"

---

### День 30 — Резюме + LinkedIn + план поиска `[лёгкий]`

**📚 Материалы:**
- 📖 LinkedIn — [How to Write a Great Summary](https://www.linkedin.com/help/linkedin/answer/a553470)
- 📖 HH.ru — [Как составить резюме разработчика](https://hh.ru/article/327004)

**💡 Почему такое решение:**
Техническая часть готова. Осталось правильно её подать. Рекрутер смотрит на резюме 6–10 секунд прежде чем решить читать дальше или нет. В эти секунды он должен увидеть: стек, проект, уровень.

Первые отклики всегда страшнее всего. После 5–10 отказов страх уходит, приходит навык. Начинай сегодня, не "когда буду готов".

**🔨 Задача:**

**Блок "Проект" в резюме (точные формулировки):**
```
AI Assistant Klawa — персональный ИИ-ассистент (личный проект)
GitHub: github.com/USERNAME/ai-assistant-klawa

Стек: Java 21, Spring Boot 3, PostgreSQL, Flyway, Docker, Anthropic Claude API, Telegram Bot API

• Реализовал модульную архитектуру (8 модулей): api / application / domain / infrastructure
• Разработал JWT авторизацию с refresh token rotation по стандарту OAuth 2.0
• Добавил Circuit Breaker (Resilience4j) для защиты от сбоев Anthropic API с fallback
• Настроил Caffeine кэш для UserDetailsService, устранив N SELECT/запрос
• Реализовал event-driven уведомления: @Scheduled → ApplicationEvent → @Async → Telegram
• Написал интеграционные тесты с Testcontainers (PostgreSQL), E2E и Security тесты
• Задеплоил через Docker Compose, multi-stage build, образ 150MB
```

**Блок "Навыки":**
```
Языки:       Java 21 (Records, Sealed Classes, Switch Expressions)
Фреймворки:  Spring Boot 3, Spring Security, Spring Data JPA, Spring MVC
БД:          PostgreSQL, Flyway, HikariCP, JPQL, Specifications
Тестирование: JUnit 5, Mockito, Testcontainers, @ParameterizedTest
Инфраструктура: Docker, Docker Compose, GitHub Actions
Паттерны:    Hexagonal Architecture, Use Case, Repository, Circuit Breaker, State Machine
Прочее:      Resilience4j, Caffeine, Bucket4j, springdoc OpenAPI, Lombok
```

**LinkedIn "О себе" секция:**
```
Java Backend разработчик, специализируюсь на Spring Boot.

Только что завершил pet-проект — AI-ассистент на Spring Boot 3 + Anthropic Claude API
с Telegram интеграцией. Реализовал: JWT auth с refresh token rotation, Circuit Breaker,
event-driven архитектуру, интеграционные тесты с Testcontainers, Docker деплой.

Ищу возможности в роли Junior/Middle Java Developer.
GitHub: github.com/USERNAME/ai-assistant-klawa
```

**Куда откликаться (в порядке приоритета):**

Позиции: Junior Java Developer, Junior Spring Boot Developer, Junior Backend Developer (Java), Java Trainee.

Площадки: hh.ru — самая большая база в СНГ. LinkedIn Jobs — международные компании и удалёнка. Habr Career — IT-компании. Telegram каналы: @javatips_jobs, @java_jobs_ru, @devhunt.

**Первая неделя откликов:**
- День 1: оформить профили на hh.ru и LinkedIn
- День 2–5: откликаться на 5–10 вакансий в день
- День 6–7: анализировать фидбек, дорабатывать сопроводительное

**Сопроводительное письмо (шаблон 3 предложения):**
```
Привет! Откликаюсь на позицию [название]. Только что завершил pet-проект —
AI-ассистент на Spring Boot 3 + PostgreSQL + Docker с интеграцией Anthropic Claude API
и Telegram ботом (github.com/USERNAME/ai-assistant-klawa).

В проекте реализовал JWT авторизацию, event-driven архитектуру, Circuit Breaker,
интеграционные тесты с Testcontainers — то есть именно тот стек что у вас в вакансии.

Готов к техническому интервью в любое удобное время.
```

**✅ Критерий готовности:**
- Резюме готово, выложено на hh.ru
- LinkedIn профиль заполнен, ссылка на GitHub в контактах
- Первые 5 откликов отправлены
- GitHub репо публичный с зелёным CI бейджем

---

## Приложение A — Вопросы на интервью

Полный список вопросов с ответами привязанными к проекту. Учи в этом порядке — от самых частых к редким.

---

### Блок 1: JPA и Hibernate (спрашивают везде)

**Q: Что такое N+1 проблема и как её решить?**

N+1 возникает когда для N объектов выполняется N дополнительных запросов вместо одного JOIN. Классика: загрузили 10 задач (1 SELECT), потом в маппере вызвали `task.getProject().getName()` для каждой — это ещё 10 SELECT. Итого 11 вместо 1.

Обнаружить: включить `show-sql: true` и посмотреть в логи при реальном запросе.

Три решения в зависимости от контекста:
- `@EntityGraph(attributePaths = {"project"})` на методе репозитория — добавляет LEFT JOIN FETCH к любому существующему методу
- `JOIN FETCH` в `@Query` — для кастомных запросов с условиями
- `hibernate.default_batch_fetch_size: 20` в конфиге — автоматическая пакетная загрузка без изменения запросов

В моём проекте использую `@EntityGraph` на `JpaTaskRepository::findAll` чтобы загружать `project` и `user` одним запросом при маппинге `TaskResponse`.

---

**Q: Чем отличается LAZY от EAGER загрузки?**

LAZY (ленивая) — связанная сущность загружается только при первом обращении к ней в коде. В БД SELECT уходит в момент вызова геттера. EAGER — загружается сразу вместе с основной сущностью, JOIN в том же запросе.

Правило: всегда используй LAZY для `@ManyToOne` и `@ManyToMany`. EAGER загружает данные даже когда они не нужны, что может вызвать огромные запросы при сложном графе объектов.

В моём проекте все связи LAZY. Когда нужен проект задачи — явно добавляю `@EntityGraph`.

Ловушка: если обратиться к LAZY полю вне транзакции — `LazyInitializationException`. Решение: `open-in-view: false` + явная загрузка нужных данных в транзакционном слое.

---

**Q: Что такое `@MappedSuperclass`?**

Аннотация для базового класса который не является самостоятельной entity, но его поля наследуются дочерними entity. Таблица для `@MappedSuperclass` не создаётся — поля включаются в таблицы дочерних классов.

В моём проекте: `BaseEntity` с `id`, `createdAt`, `updatedAt`, `deletedAt`. Все доменные entity наследуют его. Аудитинг (`@CreatedDate`, `@LastModifiedDate`) настроен один раз в `BaseEntity` и работает для всех.

---

### Блок 2: Spring и Spring Boot (спрашивают везде)

**Q: Объясни self-invocation проблему `@Transactional`**

Spring создаёт прокси-объект вокруг бина. Когда внешний код вызывает метод бина — вызов идёт через прокси, который оборачивает в транзакцию. Когда метод того же класса вызывает другой метод через `this` — прокси обходится, `@Transactional` игнорируется.

```java
// СЛОМАНО: self-invocation
@Service class A {
    public void outer() { this.inner(); }  // обходит прокси
    @Transactional public void inner() { ... } // не работает
}
// ПРАВИЛЬНО: инжектировать другой бин
@Service class A {
    @Autowired B b;
    public void outer() { b.inner(); }  // вызов через прокси B
}
```

В моём проекте это не проблема по архитектуре: каждый UseCase — отдельный Spring бин. `CreateTaskUseCase` не вызывает методы других UseCase через `this`.

---

**Q: Что такое Bean Scope? Какой у `@Service`?**

Определяет жизненный цикл Spring bean:
- `Singleton` (по умолчанию) — один экземпляр на ApplicationContext. `@Service`, `@Repository`, `@Component` — singleton.
- `Prototype` — новый экземпляр при каждом `getBean()`.
- `Request` — новый на каждый HTTP запрос (только в web контексте).
- `Session` — новый на каждую HTTP сессию.

Singleton бины должны быть stateless — нельзя хранить состояние конкретного запроса в полях. В моём проекте все UseCase — singleton и stateless, состояние передаётся через параметры методов.

---

**Q: Чем `@Component`, `@Service`, `@Repository` отличаются?**

Все три регистрируют бин в Spring контексте. Функционально почти одинаковы. Разница — семантическая и инфраструктурная:
- `@Repository` — дополнительно включает перевод исключений JPA в Spring `DataAccessException`
- `@Service` — маркер сервисного слоя, никакой дополнительной логики
- `@Component` — общий маркер для любых компонентов

Используй их по назначению для читаемости кода, а не взаимозаменяемо.

---

**Q: Как работает `@Transactional(propagation = REQUIRES_NEW)`?**

`REQUIRED` (по умолчанию) — использует текущую транзакцию если есть, иначе создаёт новую.

`REQUIRES_NEW` — всегда создаёт новую транзакцию, приостанавливая текущую. Текущая транзакция возобновляется после завершения вложенной.

Используется когда нужно зафиксировать данные независимо от результата внешней транзакции. Пример: лог аудита должен сохраниться даже если основная бизнес-операция откатилась.

---

**Q: Что такое Circuit Breaker и зачем он в проекте?**

Паттерн устойчивости для защиты от сбоев внешних сервисов. Три состояния:
- CLOSED — норма, запросы проходят
- OPEN — сервис падает, Circuit Breaker открывается, запросы возвращают fallback немедленно без ожидания таймаута
- HALF_OPEN — после паузы пробует один запрос; если успешен — переходит в CLOSED

Без Circuit Breaker: Anthropic API падает → запросы зависают 30 секунд → thread pool исчерпан → вся система не отвечает.

В моём проекте: `@CircuitBreaker(name = "anthropic")` на `AnthropicAiClient::chat`. При 50% ошибок за 10 запросов — OPEN на 30 секунд. Пользователь получает fallback "AI временно недоступен" вместо зависшего запроса.

---

### Блок 3: Тестирование (спрашивают в 70% интервью)

**Q: Почему Testcontainers а не H2?**

H2 — in-memory БД с другим SQL диалектом. Не поддерживает TIMESTAMPTZ, частичные индексы, `gen_random_uuid()`, некоторые PostgreSQL-специфичные функции. Тест на H2 может быть зелёным но упасть на реальной PostgreSQL.

Testcontainers запускает настоящий PostgreSQL в Docker. Тесты проверяют именно то что будет в продакшне.

Компромисс: Testcontainers медленнее. Решение: `withReuse(true)` — контейнер переиспользуется между тестами. Полный прогон занимает ~30 секунд вместо минут.

---

**Q: В чём разница `@Mock` и `@MockBean`?**

`@Mock` (Mockito) — создаёт мок в юнит-тесте без Spring контекста. Быстро.

`@MockBean` (Spring Boot Test) — создаёт мок и регистрирует его в Spring ApplicationContext, заменяя реальный бин. Используется в `@SpringBootTest` тестах.

Правило: для юнит-тестов UseCase — `@Mock` + `@InjectMocks` без Spring. Для интеграционных тестов — `@MockBean` для замены внешних зависимостей (Anthropic API, Telegram).

---

**Q: Что такое `@ParameterizedTest`?**

Позволяет запустить один тест с разными входными данными. Вместо трёх одинаковых тест-методов — один с `@EnumSource`, `@ValueSource`, `@CsvSource` или `@MethodSource`.

В моём проекте: `TaskStateMachineTest` проверяет все недопустимые переходы статусов через `@EnumSource` — один метод покрывает все варианты.

---

### Блок 4: Архитектура и паттерны (спрашивают на Middle+)

**Q: Объясни архитектуру своего проекта**

Модульный монолит с Hexagonal Architecture (Ports & Adapters) внутри каждого модуля.

Три слоя: `api/` — только HTTP и DTO, никакой логики. `application/` — Use Cases с @Transactional, вся бизнес-логика. `domain/` — JPA entities, enums, интерфейсы репозиториев. `infrastructure/` — реализации репозиториев, HTTP клиенты.

Ключевой принцип: domain не знает о JPA реализации, application не знает об HTTP, контроллер не знает о БД.

Слабая связь между модулями через Spring ApplicationEvents: reminder публикует `ReminderDueEvent`, notification подписывается. Модули не импортируют друг друга.

---

**Q: Зачем Soft Delete вместо физического удаления?**

Физическое удаление необратимо и ломает foreign keys если есть ссылки. Soft Delete: `deleted_at` timestamp, `@Where(clause = "deleted_at IS NULL")` на entity — Hibernate автоматически фильтрует удалённые записи во всех запросах.

Преимущества: история удалённых данных, возможность восстановления, отсутствие cascade проблем. Минус: таблицы растут; решается частичными индексами `WHERE deleted_at IS NULL` — индекс включает только активные записи.

---

**Q: Зачем `@Transactional(readOnly = true)` для GET операций?**

Hibernate в read-only транзакции пропускает dirty checking — проверку изменений всех загруженных entity перед коммитом. Это даёт 10–30% ускорение на сложных запросах с большим количеством объектов.

Дополнительно: некоторые JDBC драйверы и read replicas используют этот флаг для маршрутизации на реплику.

---

## Приложение Б — Что учить после первого месяца

### Первые 3 месяца работы или поиска

**Месяц 2: Углубить существующее**
- Spring Cloud Config — централизованная конфигурация для нескольких сервисов
- Redis — заменить Caffeine, понять разницу in-process vs distributed кэш
- @Async + CompletableFuture — более сложные сценарии асинхронности
- Hibernate second-level cache — когда и зачем
- Liquibase — альтернатива Flyway, чаще встречается в enterprise

**Месяц 3: Новые темы**
- Apache Kafka basics — producer, consumer, топики; Spring Kafka
- Design Patterns глубже: Factory, Strategy, Decorator на реальных примерах
- System Design основы: как проектировать системы, CAP теорема
- Spring Cloud Gateway — API Gateway паттерн

**Параллельно всегда:**
- LeetCode: решать по 1 задаче в день (Easy/Medium, Java). За месяц — 30 задач, это уже результат
- Baeldung: читать по одной статье в день по темам которые встречаются на работе
- Чужой код: смотреть open source Spring Boot проекты на GitHub

### Как понять что готов к Middle

Не количество месяцев опыта, а конкретные умения:
- Можешь найти N+1 в production логах и исправить без подсказки
- Понимаешь что происходит когда Spring стартует (component scan, bean lifecycle)
- Можешь объяснить junior коллеге почему self-invocation @Transactional не работает
- Можешь спроектировать схему БД для новой фичи с индексами
- Пишешь тест прежде чем исправляешь баг

---

## Финальный production + job-ready checklist

### Технический (проект)
```
Безопасность:
[ ] Нет секретов в git (git grep -r "password:" src/)
[ ] JWT access token <= 15 минут
[ ] Refresh token rotation работает
[ ] Security headers в ответах (X-Frame-Options, X-Content-Type-Options)
[ ] Rate limiting на AI endpoint

БД и производительность:
[ ] 4 Flyway миграции с success=true
[ ] N+1 устранён (@EntityGraph на findAll задач)
[ ] Частичные индексы на soft-deleted таблицах
[ ] HikariCP настроен (connection-timeout: 5000)

Надёжность:
[ ] Circuit Breaker на Anthropic и Telegram вызовах
[ ] @Async для ExtractMemoryUseCase
[ ] /actuator/health → UP с деталями компонентов
[ ] GlobalExceptionHandler — все ошибки в ApiError с traceId

Тесты:
[ ] mvn test — все зелёные
[ ] E2E тест проходит полный user journey
[ ] Security тест — все endpoint'ы требуют JWT
[ ] State Machine тест — все переходы покрыты

Docker:
[ ] docker compose up --build — работает с нуля
[ ] Образ < 200MB, запускается от appuser
```

### Профессиональный (трудоустройство)
```
GitHub:
[ ] Репо публичный, README профессиональный
[ ] CI зелёный — бейдж в README
[ ] Последние 10+ коммитов в Conventional Commits формате
[ ] .env.example в репо, .env в .gitignore

Резюме:
[ ] Блок проекта с конкретными метриками и технологиями
[ ] Блок навыков: Java 21, Spring Boot 3, PostgreSQL, Docker, Testcontainers
[ ] Ссылка на GitHub репо в контактах

Интервью:
[ ] "Расскажи о проекте" — рассказываешь за 2 минуты без запинок
[ ] N+1 — объясняешь и называешь 3 решения
[ ] @Transactional ловушки — знаешь все три
[ ] Circuit Breaker — объясняешь три состояния
[ ] Bean Scope — знаешь разницу Singleton vs Prototype
[ ] На 10/15 вопросов из таблицы отвечаешь без подглядывания

Отклики:
[ ] Профиль на hh.ru заполнен
[ ] Профиль на LinkedIn заполнен, ссылка на GitHub
[ ] Первые 10 откликов отправлены
[ ] Сопроводительное письмо написано (3 предложения)
```

---

## Приложение В — Реальные проблемы на работе

> Это не теория — это конкретные баги и ситуации с которыми сталкивается каждый Spring Boot разработчик.
> Изучи до первого рабочего дня, возвращайся когда встретишь на практике.

---

### В.1 JPA и Hibernate — типичные баги

---

#### @OneToMany + два EAGER = CartesianProduct взрыв

**Симптом:** запрос возвращает дублированные строки, или в логах тысячи SQL при загрузке одного объекта.

**Причина:** если entity имеет два `@OneToMany` с `fetch = EAGER`, Hibernate делает JOIN для обоих одновременно. Результат: декартово произведение. 100 задач × 10 тегов = 1000 строк вместо 100.

```java
// ПРОБЛЕМА — декартово произведение
@Entity public class Project {
    @OneToMany(fetch = FetchType.EAGER) // ОПАСНО
    private List<Task> tasks;
    @OneToMany(fetch = FetchType.EAGER) // вместе = декартово произведение!
    private List<Member> members;
}

// РЕШЕНИЕ — всегда LAZY + явная загрузка через @EntityGraph
@Entity public class Project {
    @OneToMany(fetch = FetchType.LAZY)
    private List<Task> tasks;
    @OneToMany(fetch = FetchType.LAZY)
    private List<Member> members;
}

// Загрузить с tasks отдельным запросом:
@EntityGraph(attributePaths = {"tasks"})
Optional<Project> findWithTasksById(Long id);
```

**Правило:** никогда не ставь `EAGER` на `@OneToMany`. Только `@ManyToOne` может быть EAGER в редких обоснованных случаях, и то лучше LAZY.

---

#### `Page<T>` с `JOIN FETCH` — `CountQuery` падает

**Симптом:** `HibernateException: query specified join fetching, but the owner of the fetched association was not present in the FROM clause`.

**Причина:** Spring Data JPA автоматически генерирует COUNT запрос для пагинации. Если в основном `@Query` есть `JOIN FETCH` — сгенерированный COUNT тоже пытается сделать `JOIN FETCH` и падает.

```java
// ПРОБЛЕМА
@Query("SELECT t FROM Task t JOIN FETCH t.project WHERE t.user.id = :id")
Page<Task> findByUserId(@Param("id") Long id, Pageable p); // CountQuery сломается!

// РЕШЕНИЕ — явно указать отдельный countQuery
@Query(
    value      = "SELECT t FROM Task t JOIN FETCH t.project WHERE t.user.id = :id",
    countQuery = "SELECT COUNT(t) FROM Task t WHERE t.user.id = :id"
)
Page<Task> findByUserId(@Param("id") Long id, Pageable p);
```

---

#### Optimistic Locking — `@Version` для конкурентных обновлений

**Симптом:** два пользователя одновременно редактируют одну задачу — один перезаписывает изменения другого без предупреждения.

**Причина:** без версионирования последний `UPDATE` побеждает. Изменения первого пользователя теряются.

```java
// Добавить @Version в BaseEntity или конкретную entity
@Entity public class Task extends BaseEntity {
    @Version
    private Long version; // автоматически инкрементируется при каждом UPDATE

    // Hibernate генерирует:
    // UPDATE tasks SET title=?, version=2 WHERE id=? AND version=1
    // Если version уже не 1 — бросает OptimisticLockException
}

// Обработать в GlobalExceptionHandler:
@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
public ResponseEntity<ApiError> handleConcurrentUpdate(
        ObjectOptimisticLockingFailureException ex, HttpServletRequest req) {
    return ResponseEntity.status(409).body(
        ApiError.of(409, "CONCURRENT_UPDATE",
            "Запись была изменена другим пользователем. Обнови данные.", req.getRequestURI()));
}

// В ответе клиенту — включить version в DTO:
public record TaskResponse(Long id, String title, ..., Long version) {}
// Клиент шлёт version обратно в PUT запросе — Hibernate проверяет
```

---

#### Flyway migration failed — как починить в production

**Симптом:** `FlywayException: Validate failed. Migration V3__ is still pending` — приложение не стартует.

**Алгоритм исправления:**

```sql
-- 1. Узнать что именно упало
SELECT version, description, success, installed_on
FROM flyway_schema_history
WHERE success = false;

-- 2. Откатить вручную то что успело выполниться
ALTER TABLE tasks DROP COLUMN IF EXISTS bad_column;

-- 3. Удалить запись о провалившейся миграции
DELETE FROM flyway_schema_history WHERE success = false;
```

```bash
# 4. Или через Maven plugin (автоматически удаляет FAILED):
mvn flyway:repair

# 5. Исправить SQL файл, перезапустить приложение
```

**Профилактика — использовать транзакционные миграции:**

```sql
-- V3__safe_migration.sql
BEGIN;
ALTER TABLE tasks ADD COLUMN priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM';
UPDATE tasks SET priority = 'HIGH' WHERE due_date < now();
COMMIT;
-- Если что-то упадёт — весь блок откатится, Flyway не пометит как FAILED
```

**Правило:** никогда не редактируй уже применённый файл миграции. Flyway хранит checksum — при несовпадении приложение не стартует. Исправления — только новым файлом `V4__fix_...`.

---

#### Database Deadlock — PostgreSQL блокировки

**Симптом:** `ERROR: deadlock detected — Process 123 waits for ShareLock on transaction 456; Process 456 waits for ShareLock on transaction 123`.

**Причина:** транзакция A держит блокировку строки 1 и ждёт строку 2; транзакция B держит строку 2 и ждёт строку 1. PostgreSQL убивает одну из них.

```java
// ОПАСНО — массовые UPDATE в случайном порядке
tasks.forEach(t -> taskRepo.save(t)); // порядок не определён = потенциальный deadlock

// БЕЗОПАСНО — фиксированный порядок обработки
tasks.stream()
     .sorted(Comparator.comparing(Task::getId)) // всегда по возрастанию id
     .forEach(t -> taskRepo.save(t));
```

```sql
-- Диагностика активных блокировок:
SELECT pid, now() - query_start AS duration, state, wait_event_type, query
FROM pg_stat_activity
WHERE wait_event_type = 'Lock'
ORDER BY duration DESC;
```

**Правило:** при массовом обновлении нескольких строк — всегда сортируй по id перед обработкой. Это устраняет 90% deadlock'ов.

---

### В.2 Async и Scheduler — тихие ошибки

---

#### @Async исключение исчезает молча

**Симптом:** метод помечен `@Async`, падает с `NullPointerException`, но в логах нет ни одной строки об ошибке.

**Причина:** `@Async` методы выполняются в отдельном потоке. Необработанное исключение не всплывает в вызывающий поток — оно теряется если не настроен `AsyncUncaughtExceptionHandler`.

```java
// Настроить перехватчик исключений из @Async методов
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
            log.error("[Async] Uncaught exception in method '{}': {}",
                      method.getName(), ex.getMessage(), ex);
    }

    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(3);
        exec.setMaxPoolSize(10);
        exec.setQueueCapacity(200);
        exec.setThreadNamePrefix("notification-");
        exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        exec.initialize();
        return exec;
    }
}

// Для критичных @Async методов — использовать CompletableFuture:
@Async("notificationExecutor")
public CompletableFuture<Void> extractMemory(Long userId, String msg) {
    try {
        // ... логика
        return CompletableFuture.completedFuture(null);
    } catch (Exception e) {
        log.error("[Memory] Extraction failed for userId={}: {}", userId, e.getMessage(), e);
        return CompletableFuture.failedFuture(e);
        // caller может проверить: future.exceptionally(ex -> ...)
    }
}
```

**Правило:** всегда настраивай `AsyncUncaughtExceptionHandler`. Никогда не узнаешь о проблеме в фоновой задаче без него.

---

#### @Scheduled дублируется при нескольких инстансах — ShedLock

**Симптом:** при deploy в Kubernetes с 3 репликами `ReminderScheduler` запускается 3 раза каждую минуту, создавая тройные уведомления.

**Причина:** `@Scheduled` не знает о других инстансах. ShedLock использует БД-блокировку — только один инстанс выполняет задачу.

```xml
<!-- pom.xml -->
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-spring</artifactId>
    <version>5.13.0</version>
</dependency>
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-provider-jdbc-template</artifactId>
    <version>5.13.0</version>
</dependency>
```

```sql
-- V5__shedlock.sql
CREATE TABLE shedlock (
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMP(3) NOT NULL,
    locked_at  TIMESTAMP(3) NOT NULL,
    locked_by  VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);
```

```java
// Включить в конфигурации:
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
@Configuration
public class SchedulerConfig {
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
            JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .usingDbTime()
                .build()
        );
    }
}

// Аннотировать @Scheduled методы:
@Scheduled(fixedDelay = 60_000)
@SchedulerLock(
    name = "checkDueReminders",
    lockAtMostFor  = "PT55S",   // максимально держать блокировку 55 сек
    lockAtLeastFor = "PT30S"    // минимально держать 30 сек (защита от быстрых повторов)
)
public void checkDueReminders() { ... }
```

**Правило:** как только появляется второй инстанс приложения — ShedLock обязателен для всех `@Scheduled` задач которые изменяют данные.

---

### В.3 Jackson / JSON — сюрпризы сериализации

---

#### Круговые ссылки — StackOverflowError при сериализации entity

**Симптом:** `StackOverflowError` при ответе контроллера. `Task → Project → List<Task> → Task → ...`

**Причина:** двусторонние JPA связи вызывают бесконечную рекурсию Jackson. Главная причина почему entity нельзя возвращать из контроллера напрямую.

```java
// ПРОБЛЕМА — entity напрямую в ответе
@GetMapping("/tasks/{id}")
public Task getTask(@PathVariable Long id) {
    return taskRepo.findById(id).get(); // StackOverflowError!
}

// РЕШЕНИЕ 1 (правильное) — использовать DTO
public record TaskResponse(Long id, String title, Long projectId, String projectName) {}
// projectId вместо полного Project — нет рекурсии

// РЕШЕНИЕ 2 — если DTO невозможен
@Entity public class Task {
    @ManyToOne
    @JsonManagedReference  // включается в JSON
    private Project project;
}
@Entity public class Project {
    @OneToMany
    @JsonBackReference  // исключается из JSON (разрывает цикл)
    private List<Task> tasks;
}

// РЕШЕНИЕ 3 — на уровне Jackson
@Bean
public Jackson2ObjectMapperBuilderCustomizer customizer() {
    return builder -> builder.featuresToEnable(
        SerializationFeature.INDENT_OUTPUT
    ).featuresToDisable(
        SerializationFeature.FAIL_ON_EMPTY_BEANS
    );
}
```

---

#### `Instant` сериализуется как массив чисел

**Симптом:** клиент получает `[2024,6,15,10,30,0]` вместо `"2024-06-15T10:30:00Z"`.

```yaml
# application.yaml — решает проблему глобально
spring:
  jackson:
    serialization:
      write-dates-as-timestamps: false   # ISO-8601 строки вместо массивов
    deserialization:
      fail-on-unknown-properties: false  # неизвестные поля не ломают
    default-property-inclusion: non_null # null поля не включаются в JSON
    time-zone: UTC                       # единая timezone
```

```java
// Или через @Bean для полного контроля:
@Bean
public ObjectMapper objectMapper() {
    return JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build();
}
```

---

#### `@JsonIgnore` ломает десериализацию

**Симптом:** поставил `@JsonIgnore` на `password` чтобы не возвращать в ответе — теперь `password` не приходит при регистрации.

**Причина:** `@JsonIgnore` работает в обоих направлениях: скрывает при сериализации И игнорирует при десериализации.

```java
// ОПАСНО
public class User {
    @JsonIgnore                 // password не придёт из POST /register!
    private String password;
}

// ПРАВИЛЬНО 1 — разные DTO (предпочтительно)
public record RegisterRequest(String email, String password) {}   // input: password есть
public record UserResponse(Long id, String email, String role) {} // output: password нет

// ПРАВИЛЬНО 2 — если один класс неизбежен
public class User {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // только входящий
    private String password;
}
```

---

### В.4 Production и деплой — что ломается в реальной среде

---

#### Connection pool exhaustion — приложение зависает под нагрузкой

**Симптом:** `HikariPool: Connection is not available, request timed out after 5000ms`. Все запросы висят.

**Диагностика:**

```bash
# Actuator метрики — смотреть в real-time
curl localhost:8080/actuator/metrics/hikaricp.connections.active
curl localhost:8080/actuator/metrics/hikaricp.connections.pending
curl localhost:8080/actuator/metrics/hikaricp.connections.timeout

# PostgreSQL — долгие активные транзакции
psql -c "
SELECT pid, now() - query_start AS duration, state, left(query, 80) AS query
FROM pg_stat_activity
WHERE state = 'active'
  AND now() - query_start > interval '3 seconds'
ORDER BY duration DESC;"
```

**Частая причина: `@Transactional` + внешний HTTP вызов:**

```java
// ПРОБЛЕМА — соединение держится пока AI думает (2-30 сек)
@Transactional
public ChatResponse execute(ChatRequest req, Long userId) {
    saveUserMessage(userId, req.message());   // BEGIN транзакция
    String reply = aiClient.chat(...);         // HTTP 5-30 сек, транзакция ОТКРЫТА
    saveAssistantMessage(userId, reply);       // COMMIT
}

// РЕШЕНИЕ — внешний вызов вне транзакции
public ChatResponse execute(ChatRequest req, Long userId) {
    // 1. HTTP вызов ВНЕ транзакции — соединение не занято
    String reply = aiClient.chat(...);

    // 2. Быстрая транзакция только для записи в БД
    saveMessages(userId, req.message(), reply);
    return new ChatResponse(reply);
}

@Transactional
private void saveMessages(Long userId, String userMsg, String aiReply) {
    messageRepo.save(Message.user(userId, userMsg));
    messageRepo.save(Message.assistant(userId, aiReply));
}
```

**Правило:** никогда не держи `@Transactional` открытым во время HTTP вызовов к внешним API. Это главная причина исчерпания пула под нагрузкой.

---

#### Работает локально, падает в Docker / CI

**Чеклист "работает везде":**

```bash
# 1. Timezone — всегда UTC в хранении
# application.yaml:
spring.jpa.properties.hibernate.jdbc.time_zone: UTC

# Dockerfile:
ENV TZ=UTC

# 2. Имена сервисов — не localhost
# НЕПРАВИЛЬНО (локально работает, Docker — нет):
DB_URL=jdbc:postgresql://localhost:5432/klawa

# ПРАВИЛЬНО (имя сервиса из docker-compose):
DB_URL=jdbc:postgresql://postgres:5432/klawa

# 3. .env файл передаётся в compose
services:
  app:
    env_file: .env   # обязательно!

# 4. Версия Java в Dockerfile = локальной версии
FROM eclipse-temurin:21-jre-alpine  # не latest!

# 5. Запускать тесты в том же окружении что CI:
docker compose run --rm app mvn test -B

# 6. Проверить что MockAiClient активен в тестах:
# application-test.yaml должен содержать SPRING_PROFILES_ACTIVE=test
```

---

#### Загрузка файлов — multipart/form-data

```yaml
# application.yaml
spring:
  servlet:
    multipart:
      enabled:          true
      max-file-size:    10MB
      max-request-size: 10MB
```

```java
@PostMapping("/upload")
public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {

    if (file.isEmpty())
        throw new BadRequestException("File is empty");

    // Sanitize имя файла — защита от path traversal
    String safeName = StringUtils.cleanPath(
        Objects.requireNonNull(file.getOriginalFilename())
    );
    if (safeName.contains(".."))
        throw new BadRequestException("Invalid file name");

    // Стримить в хранилище — не загружать в память целиком
    String storedName = UUID.randomUUID() + "_" + safeName;
    Path target = Paths.get(uploadDir).resolve(storedName);
    Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

    return ResponseEntity.ok("/files/" + storedName);
}

// GlobalExceptionHandler — добавить обработчик:
@ExceptionHandler(MaxUploadSizeExceededException.class)
public ResponseEntity<ApiError> handleSizeLimit(
        MaxUploadSizeExceededException ex, HttpServletRequest req) {
    return ResponseEntity.status(413)
        .body(ApiError.of(413, "FILE_TOO_LARGE", "Max file size is 10MB", req.getRequestURI()));
}
```

---

### В.5 Отладка — инструменты которые нужны каждый день

---

#### Читать stack trace правильно

Stack trace читается **снизу вверх** по пакетам. Строки `org.springframework`, `org.hibernate`, `sun.reflect` — это фреймворк. Ищи свой пакет: `org.example.klawa`.

```
java.lang.NullPointerException: Cannot invoke "String.length()" because "str" is null
  at o.e.klawa.task.application.TaskMapper.toResponse(TaskMapper.java:23)  ← ЗДЕСЬ
  at o.e.klawa.task.application.GetTasksUseCase.execute(GetTasksUseCase.java:41)
  at o.e.klawa.task.api.TaskController.list(TaskController.java:55)
  at o.s.web.servlet.FrameworkServlet.processRequest(...)     ← фреймворк, пропускаем
  at o.a.catalina.core.StandardWrapper.service(...)            ← Tomcat, пропускаем

Алгоритм:
1. Тип исключения в первой строке: NullPointerException
2. Первая строка С НАШИМ ПАКЕТОМ: TaskMapper.java:23
3. Открыть файл, перейти на строку 23
4. Что там вызывается на null объекте?
5. Поставить breakpoint, запустить дебаггер
```

**Частые причины `NullPointerException` в Spring:**

```java
// Причина 1: lazy поле = null когда project не загружен
task.getProject().getName(); // getProject() вернул null — проект не загружен

// Причина 2: @Autowired поле null в тесте без Spring контекста
class MyTest {
    @Autowired TaskService service; // null — нет @SpringBootTest!
}

// Причина 3: Optional.get() без проверки
taskRepo.findById(id).get(); // NoSuchElementException если не найдено
// Правильно:
taskRepo.findById(id).orElseThrow(() -> new NotFoundException("Task " + id + " not found"));
```

---

#### Custom Bean Validation — @ConstraintValidator

Встроенные аннотации (`@NotBlank`, `@Email`, `@Size`) проверяют формат. Бизнес-правила нужно писать самому.

```java
// 1. Аннотация-маркер
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
public @interface ValidDateRange {
    String message() default "End date must be after start date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// 2. Логика валидации
public class DateRangeValidator
        implements ConstraintValidator<ValidDateRange, CreateEventRequest> {

    @Override
    public boolean isValid(CreateEventRequest req, ConstraintValidatorContext ctx) {
        if (req.startAt() == null || req.endAt() == null)
            return true; // null проверяется @NotNull отдельно
        return req.endAt().isAfter(req.startAt());
    }
}

// 3. Применить на DTO
@ValidDateRange
public record CreateEventRequest(
    @NotBlank String title,
    @NotNull  Instant startAt,
              Instant endAt    // опциональный
) {}

// 4. GlobalExceptionHandler уже обрабатывает MethodArgumentNotValidException —
//    кастомный валидатор автоматически туда попадёт
```

---

#### Spring Auto-configuration: понять что происходит при старте

```bash
# Увидеть все decisions auto-configuration при старте:
# application-dev.yaml (только для отладки!):
logging.level.org.springframework.boot.autoconfigure: DEBUG

# В логах появятся:
# Positive matches: DataSourceAutoConfiguration — MATCHED (есть HikariCP в classpath)
# Negative matches: RedisAutoConfiguration — NOT MATCHED (нет spring-data-redis)
```

```java
// ConflictingBeanDefinitionException — два бина с одним именем
// Решение: переопределить через @Primary или @Bean в своём @Configuration
@Configuration
public class JacksonConfig {
    @Bean           // перекрывает auto-configured ObjectMapper
    @Primary
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();
    }
}

// Исключить ненужный auto-configuration:
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class  // если подключаешь БД сам
})
```

---

### В.6 Шпаргалка — что делать когда что-то сломалось

| Симптом | Первый шаг | Вероятная причина |
|---|---|---|
| `StackOverflowError` в контроллере | Проверить есть ли entity в ответе | Круговые Jackson-ссылки |
| `LazyInitializationException` | Включить `show-sql`, найти где entity покидает транзакцию | Lazy поле читается вне `@Transactional` |
| `CountQuery failed` при пагинации | Добавить явный `countQuery` в `@Query` | `JOIN FETCH` в запросе без `countQuery` |
| `Connection is not available` | Проверить Actuator + pg_stat_activity | `@Transactional` + медленный внешний HTTP вызов |
| Нет ошибки но данные неправильные | Проверить `@Async` + `AsyncUncaughtExceptionHandler` | Исключение в `@Async` потерялось |
| Работает локально, не работает в Docker | Проверить `localhost` в конфиге | Имя сервиса вместо `localhost` в compose |
| `FlywayException: Validate failed` | `SELECT * FROM flyway_schema_history` | Изменён уже применённый файл миграции |
| Двойные уведомления при масштабировании | Добавить ShedLock | `@Scheduled` без блокировки на несколько инстансов |
| Дублирование строк в запросе | Убрать `EAGER` на `@OneToMany` | CartesianProduct от нескольких EAGER коллекций |

