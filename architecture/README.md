# 📚 Документация архитектуры и схемы базы данных Klawa

Вся проектная и системная документация проекта Klawa собрана в данной директории `architecture/`.

---

## 🗂️ Состав документации

| Файл | Описание | Формат / Как открыть |
|---|---|---|
| 🗄️ [`database-er-model.md`](./database-er-model.md) | **ER-диаграмма БД и аудит готовности Entity**<br>Связи между таблицами, аудит несоответствий между JPA Entity и PostgreSQL миграцией `V0__init_tables.sql`. | Markdown / GitHub / Любой редактор |
| 🌐 [`er-diagram.html`](./er-diagram.html) | **Интерактивный просмотрщик ER-диаграммы**<br>Красивая визуальная схема с возможностью приближения (zoom) и панорамирования (pan). | Открыть двойным кликом в любом браузере |
| 📊 [`er-diagram.mermaid`](./er-diagram.mermaid) | **Спецификация ER-диаграммы**<br>Исходный код схемы для Mermaid CLI, Notion, Obsidian или GitHub. | Текстовый Mermaid код |
| 🏛️ [`workspace.dsl`](./workspace.dsl) | **C4 модель архитектуры**<br>Уровни System Context, Container, Component, Dynamic-пайплайны и Deployment для Structurizr. | Structurizr DSL (`docker run ... local`) |
| 📑 [`openapi.yaml`](./openapi.yaml) | **OpenAPI 3.0.3 Спецификация API**<br>Полный контракт всех эндпоинтов, Request/Response DTO, коды ошибок RFC 7807, пагинация. | [Swagger Editor](https://editor.swagger.io) |
| 🎯 [`use-cases.md`](./use-cases.md) | **35 Сценариев использования (Use Cases)**<br>Пошаговые сценарии для всех модулей с альтернативными потоками и State Machine диаграммами. | Markdown / GitHub |

---

## 🚀 Как быстро открыть и посмотреть

1. **ER-диаграмму визуально**:
   * Дважды кликните по файлу [`architecture/er-diagram.html`](./er-diagram.html) — откроется интерактивная схема в браузере.
2. **C4-диаграммы архитектуры (Structurizr)**:
   * Запустите контейнер:
     ```powershell
     docker run -it --rm -p 8070:8080 -e STRUCTURIZR_AUTOREFRESHINTERVAL=2000 -v "C:\Users\Honor\Desktop\ai-assistant-klawa\architecture:/usr/local/structurizr" structurizr/structurizr local
     ```
   * Перейдите на `http://localhost:8070`.
3. **OpenAPI Спецификацию**:
   * Скопируйте содержимое [`openapi.yaml`](./openapi.yaml) в [editor.swagger.io](https://editor.swagger.io).
