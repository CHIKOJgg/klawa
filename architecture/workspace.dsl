workspace "Klawa" "Персональный AI-ассистент для управления задачами" {

    model {
        // ═══════════ АКТОРЫ ═══════════
        user = person "Пользователь" "Управляет задачами, общается с AI-ассистентом"

        // ═══════════ ВНЕШНИЕ СИСТЕМЫ ═══════════
        claudeApi = softwareSystem "Anthropic Claude API" "Внешний AI-провайдер" {
            tags "External"
        }
        telegramApi = softwareSystem "Telegram Bot API" "Мессенджер для уведомлений" {
            tags "External"
        }

        // ═══════════ KLAWA ═══════════
        klawa = softwareSystem "Klawa" "Персональный AI-ассистент: задачи, напоминания, AI-чат, аналитика" {

            // --- Контейнеры ---
            spa = container "React SPA" "Пользовательский интерфейс" "React 19, TypeScript, Vite, Tailwind" {
                tags "Frontend"
            }

            api = container "Spring Boot API" "REST API + бизнес-логика" "Java 25, Spring Boot 4, Spring Modulith" {

                // --- Компоненты (модули) ---
                userModule      = component "User Module"         "Регистрация, JWT-аутентификация, профиль"
                taskModule      = component "Task Module"         "CRUD задач, state machine статусов"
                projectModule   = component "Project Module"      "CRUD проектов, привязка задач к проекту"
                reminderModule  = component "Reminder Module"     "Создание напоминаний, scheduler проверки"
                notifModule     = component "Notification Module" "Уведомления: создание, доставка, mark as read"
                agentModule     = component "Agent Module"        "AI-чат: промпты, контекст, вызов Claude API"
                memoryModule    = component "Memory Module"       "Долгосрочная память: факты о пользователе"
                telegramModule  = component "Telegram Module"     "Webhook, deep-linking, отправка сообщений"
                analyticsModule = component "Analytics Module"    "Агрегированная статистика по задачам"
            }

            db = container "PostgreSQL" "Основное хранилище данных" "PostgreSQL 17" {
                tags "Database"
            }

            cache = container "Redis" "Кэш, очереди событий" "Redis" {
                tags "Database"
            }
        }

        // ═══════════ СВЯЗИ: System Context ═══════════
        user -> klawa "Использует" "HTTPS, Telegram"
        klawa -> claudeApi "Отправляет промпты, получает ответы" "HTTPS"
        klawa -> telegramApi "Отправляет/принимает сообщения" "HTTPS"

        // ═══════════ СВЯЗИ: Container ═══════════
        user -> spa "Открывает в браузере" "HTTPS"
        spa -> api "REST API запросы" "HTTPS / JSON"
        api -> db "Читает и пишет данные" "JDBC"
        api -> cache "Кэширует, очереди" "Redis Protocol"

        // ═══════════ СВЯЗИ: Component ═══════════

        // SPA → модули (через REST контроллеры)
        spa -> userModule "Регистрация, логин, /auth/*"
        spa -> taskModule "CRUD задач, /tasks/*"
        spa -> projectModule "CRUD проектов, /projects/*"
        spa -> reminderModule "Напоминания, /reminders/*"
        spa -> notifModule "Уведомления, /notifications/*"
        spa -> agentModule "AI-чат, /agent/*"
        spa -> memoryModule "Память, /memories/*"
        spa -> analyticsModule "Статистика, /analytics/*"

        // Модуль → Модуль (синхронные вызовы)
        taskModule -> projectModule "Проверяет существование проекта"
        reminderModule -> taskModule "Читает информацию о задаче"
        agentModule -> memoryModule "Читает/пишет контекст пользователя"
        agentModule -> taskModule "Создаёт задачу программно (tool_use)"
        analyticsModule -> taskModule "Агрегирует данные по задачам"
        telegramModule -> userModule "Привязывает Telegram-аккаунт"

        // Модуль → Модуль (события, асинхронно)
        reminderModule -> notifModule "ReminderDueEvent" "Spring Event"
        notifModule -> telegramModule "Отправить уведомление в Telegram"

        // Модуль → Внешние системы
        agentModule -> claudeApi "Вызывает AI" "HTTPS"
        telegramModule -> telegramApi "Webhook, sendMessage" "HTTPS"

        // Все модули → БД
        userModule -> db "Читает/пишет" "JDBC"
        taskModule -> db "Читает/пишет" "JDBC"
        projectModule -> db "Читает/пишет" "JDBC"
        reminderModule -> db "Читает/пишет" "JDBC"
        notifModule -> db "Читает/пишет" "JDBC"
        agentModule -> db "Читает/пишет" "JDBC"
        memoryModule -> db "Читает/пишет" "JDBC"
        telegramModule -> db "Читает/пишет" "JDBC"
        analyticsModule -> db "Читает/пишет" "JDBC"

        // ═══════════ DEPLOYMENT: Local Development ═══════════
        deploymentEnvironment "Local Development" {
            deploymentNode "Developer Laptop" "" "Windows 11" {
                deploymentNode "Docker Desktop" "" "Docker" {
                    containerInstance db
                    containerInstance cache
                }
                deploymentNode "JVM" "" "Java 25" {
                    containerInstance api
                }
                deploymentNode "Node.js" "" "Vite Dev Server" {
                    containerInstance spa
                }
            }
        }
    }

    views {

        // ─── Уровень 1: System Context ───
        systemContext klawa "SystemContext" "Klawa и её окружение" {
            include *
            autoLayout
        }

        // ─── Уровень 2: Containers ───
        container klawa "Containers" "Из чего Klawa физически состоит" {
            include *
            autoLayout
        }

        // ─── Уровень 3: Components ───
        component api "Components" "Модули внутри Spring Boot API" {
            include *
            autoLayout
        }

        // ─── Сценарий: Отправка сообщения в AI-чат ───
        dynamic api "SendMessageFlow" "Пользователь отправляет сообщение в AI-чат" {
            spa -> agentModule "POST /agent/chat {message}"
            agentModule -> memoryModule "Получить контекст (факты, задачи)"
            agentModule -> claudeApi "Сформировать промпт, отправить"
            claudeApi -> agentModule "Ответ AI"
            agentModule -> db "Сохранить сообщение и ответ"
            autoLayout
        }

        // ─── Сценарий: Срабатывание напоминания ───
        dynamic api "ReminderFlow" "Наступает время напоминания" {
            reminderModule -> taskModule "Scheduler: задача ещё актуальна?"
            reminderModule -> notifModule "Публикует ReminderDueEvent"
            notifModule -> db "Сохранить уведомление"
            notifModule -> telegramModule "Отправить в Telegram (если привязан)"
            telegramModule -> telegramApi "sendMessage"
            autoLayout
        }

        // ─── Сценарий: Создание задачи ───
        dynamic api "CreateTaskFlow" "Пользователь создаёт новую задачу" {
            spa -> taskModule "POST /tasks {title, projectId, priority}"
            taskModule -> projectModule "Проверить: проект существует?"
            projectModule -> db "SELECT project WHERE id = ?"
            taskModule -> db "INSERT INTO task"
            taskModule -> spa "201 Created: TaskResponse"
            autoLayout
        }

        // ─── Сценарий: Регистрация пользователя ───
        dynamic api "RegisterFlow" "Новый пользователь регистрируется в системе" {
            spa -> userModule "POST /auth/register {email, password, firstName}"
            userModule -> db "Проверить уникальность email"
            userModule -> db "INSERT INTO _user (BCrypt hash)"
            userModule -> spa "201 Created: {accessToken, refreshToken}"
            autoLayout
        }

        // ─── Сценарий: AI создаёт задачу из чата ───
        dynamic api "AICreateTaskFlow" "AI-ассистент создаёт задачу по запросу из чата" {
            spa -> agentModule "POST /agent/chat 'создай задачу купить молоко'"
            agentModule -> memoryModule "Получить контекст"
            agentModule -> claudeApi "Промпт с tool_use"
            claudeApi -> agentModule "tool_call: create_task"
            agentModule -> taskModule "Создать задачу программно"
            taskModule -> db "INSERT INTO task"
            agentModule -> spa "Ответ: 'Создал задачу Купить молоко'"
            autoLayout
        }

        // ─── Deployment: Local Development ───
        deployment klawa "Local Development" "LocalDev" "Локальное окружение разработчика" {
            include *
            autoLayout
        }

        // ─── Стили ───
        styles {
            element "Person" {
                shape Person
                background #1e3a5f
                color #ffffff
            }
            element "Software System" {
                background #2563eb
                color #ffffff
            }
            element "Container" {
                background #3b82f6
                color #ffffff
            }
            element "Component" {
                background #60a5fa
                color #000000
            }
            element "External" {
                background #6b7280
                color #ffffff
            }
            element "Database" {
                shape Cylinder
            }
            element "Frontend" {
                shape WebBrowser
            }
            relationship "Spring Event" {
                style dashed
                color #f59e0b
            }
        }
    }

    configuration {
        scope softwaresystem
    }
}