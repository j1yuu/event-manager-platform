# Итерация 4: архитектура EventNotificator и план для Gradle

Этот документ переводит формулировки задания в конкретный план для текущего
проекта. Главная цель итерации — не «добавить ещё один контроллер», а разделить
систему на два независимо запускаемых приложения, которые обмениваются
доменными событиями асинхронно.

## 1. Что в итоге должно получиться

```text
                    один Gradle-репозиторий
┌──────────────────────────────────────────────────────────────────┐
│                                                                  │
│  event-manager            event-common          event-notificator│
│  Spring Boot :8080        Java library          Spring Boot :8081│
│  своя PostgreSQL БД       Kafka DTO             своя PostgreSQL БД│
│          │                     ▲                         ▲        │
│          └── producer ─ Kafka topic ─ consumer ─────────┘        │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

Это **три Gradle-модуля, но только два сервиса**:

| Модуль | Можно запустить? | Ответственность |
|---|---:|---|
| `event-manager` | да | Пользователи, login/JWT, локации, мероприятия, регистрации; при изменении мероприятия публикует событие в Kafka |
| `event-common` | нет | Небольшая Java-библиотека с Kafka-контрактом, одинаковым для producer и consumer |
| `event-notificator` | да | Читает Kafka, хранит inbox уведомлений, проверяет JWT, реализует `/notifications` и удаляет старые записи |

`event-common` — не микросервис: у него нет `main`, порта, контроллеров, БД и
Spring Boot application. Это обычный JAR, который подключают два сервиса.

## 2. Границы ответственности

### `event-manager`

Он остаётся владельцем данных мероприятий и регистраций. Только здесь известно:

* кто владелец мероприятия;
* какие пользователи на него зарегистрированы;
* какие поля и кем были изменены;
* когда scheduler сменил статус.

Поэтому именно `event-manager` формирует полный список `subscribers` и diff
`changes`, после успешного бизнес-изменения публикует их в Kafka. Регистрация и
отмена регистрации сами по себе Kafka-сообщений этой итерации не создают.

### `event-common`

Здесь фиксируется **внутренний межсервисный контракт**, например:

```java
public record EventChangedMessage(
        UUID messageId,
        EventChangeType eventType,
        Long eventId,
        Instant occurredAt,
        Long ownerId,
        Long changedById,
        List<Long> subscribers,
        List<FieldChange> changes
) {}

public record FieldChange(String field, Object oldValue, Object newValue) {}
```

Не нужно помещать сюда JPA-сущности, репозитории, контроллеры или DTO ответов
HTTP API. Общая библиотека отвечает только на вопрос: «Как выглядит сообщение
в Kafka?»

### `event-notificator`

Этот сервис не запрашивает мероприятие или пользователя у `event-manager` при
каждом чтении inbox. Consumer получает достаточный snapshot из сообщения,
сохраняет его локально и создаёт пользовательские ссылки на него.

Он не регистрирует пользователей и не выдаёт JWT. Его security-слой только:

1. получает `Authorization: Bearer ...`;
2. проверяет подпись, issuer и срок действия по тем же JWT-настройкам;
3. извлекает identity текущего пользователя;
4. использует её для фильтрации `notifications`.

В текущем проекте subject токена — login, а не `userId`. Значит, до начала
реализации нужно выбрать и зафиксировать один из вариантов:

* **предпочтительно:** добавить `userId` claim при выпуске JWT и читать его в
  обоих сервисах;
* либо передавать login в Kafka и хранить inbox по login;
* либо делать синхронный запрос из notificator в manager, что усложняет систему
  и противоречит смыслу этой итерации.

Просто вызвать в notificator существующий `getLogin()` и считать результат
числовым `userId` нельзя.

## 3. Полный путь одного изменения

Допустим, пользователь 44 зарегистрирован на мероприятие 10, а владелец меняет
дату.

1. `event-manager` загружает исходное мероприятие.
2. До изменения запоминает старые значения.
3. Валидирует и сохраняет новую дату.
4. Строит только фактический diff, например
   `FieldChange("date", oldDate, newDate)`.
5. Получает ID зарегистрированных пользователей. Владелец или инициатор не
   должны автоматически считаться подписчиками, если это отдельно не сказано в
   требованиях.
6. Создаёт один `messageId` и отправляет **одно** сообщение в Kafka, даже если
   подписчиков десять.
7. `event-notificator` получает сообщение.
8. По уникальному `messageId` проверяет, не обрабатывалось ли оно ранее.
9. Создаёт одну строку `notification_event_payloads`.
10. Создаёт по строке `notifications` для каждого subscriber; все они ссылаются
    на один `payload_id`.
11. Пользователь 44 вызывает `GET /notifications` со своим JWT и видит свою
    непрочитанную запись.
12. `POST /notifications` помечает её прочитанной, только если она принадлежит
    пользователю 44.

Kafka обычно доставляет сообщение как минимум один раз, поэтому шаг 8 обязателен:
на `notification_event_payloads.message_id` нужен `UNIQUE`. Повторная доставка
не должна создавать второй payload или второй комплект inbox-записей. Полезна
также уникальность `(user_id, payload_id)`.

## 4. Модель данных notificator

### `notification_event_payloads`

Одна строка соответствует одному факту изменения:

```text
id              bigint primary key
message_id      uuid unique not null
event_type      varchar not null
event_id         bigint not null
occurred_at     timestamptz not null
owner_id        bigint not null
changed_by_id   bigint null
payload         jsonb not null
```

В `payload` сохраняются структурированные данные (`eventName`, `changes` и
другие полезные snapshot-поля), а не заранее склеенная фраза.

### `notifications`

Одна строка соответствует месту общего изменения в inbox конкретного
пользователя:

```text
id              bigint primary key
user_id         bigint not null
payload_id      bigint not null references notification_event_payloads(id)
is_read         boolean not null default false
created_at      timestamptz not null
read_at         timestamptz null
unique (user_id, payload_id)
```

Например, одно событие для пользователей 44, 45 и 46 даёт **1 payload + 3
notifications**, а не три копии JSON.

Оба сохранения consumer должен выполнять в одной транзакции. Если создание
inbox-записей упало, не должно остаться «успешно обработанного» payload без
получателей.

## 5. Внешний HTTP API — отдельная модель

Kafka DTO, JPA entity и HTTP DTO — три разные модели. В ответе API не должно
быть `subscribers`, а клиенту не нужно знать `payloadId`.

`GET /notifications`:

* берёт пользователя только из JWT;
* выбирает его `is_read = false` записи;
* возвращает `notificationId`, `type`, `eventId`, `createdAt`, `isRead`, короткий
  `message` и структурированный `payload`;
* возвращает `200 []`, если записей нет.

`POST /notifications`:

```json
{ "notificationIds": [101, 102, 999999] }
```

Должен выполнить ограниченное текущим пользователем обновление, эквивалентное:

```sql
update notifications
set is_read = true, read_at = now()
where user_id = :currentUserId
  and id in (:notificationIds)
  and is_read = false;
```

Чужие и отсутствующие ID не являются ошибкой. Согласно уже добавленной OpenAPI
спецификации успешный ответ — `204 No Content`.

## 6. Как выглядит multi-module проект на Gradle

Требование Maven из методички означает требование к **структуре модулей**, а не
обязательный выбор Maven. Для Gradle эквивалентная структура такая:

```text
event-manager/                  # корень репозитория
├── settings.gradle
├── build.gradle                # общие версии и настройки
├── event-common/
│   ├── build.gradle
│   └── src/main/java/...
├── event-manager/
│   ├── build.gradle
│   └── src/{main,test}/...      # сюда переносится текущий src
└── event-notificator/
    ├── build.gradle
    └── src/{main,test}/...
```

Минимальный `settings.gradle`:

```groovy
rootProject.name = 'event-manager-platform'

include 'event-common'
include 'event-manager'
include 'event-notificator'
```

В корневом `build.gradle` удобно объявить плагины с `apply false`, репозитории,
Java toolchain и общие test-настройки. Далее зависимости задаются по назначению:

```groovy
// event-common/build.gradle
plugins { id 'java-library' }

// event-manager/build.gradle
dependencies {
    implementation project(':event-common')
    implementation 'org.springframework.kafka:spring-kafka'
}

// event-notificator/build.gradle
dependencies {
    implementation project(':event-common')
    implementation 'org.springframework.kafka:spring-kafka'
    // web, security, data-jpa, validation, liquibase, JWT, PostgreSQL
}
```

После этого из корня доступны:

```bash
./gradlew build
./gradlew :event-manager:bootRun
./gradlew :event-notificator:bootRun
./gradlew :event-common:test
```

У двух приложений должны быть разные порты и разные БД/схемы. Нельзя давать
notificator прямой доступ к таблицам manager: иначе формально будет два сервиса,
но фактически останется связанный монолит.

## 7. Где менять текущий код

После переноса текущего `src` в модуль `event-manager` точки интеграции будут
такими:

* `EventService.updateEvent()` — сравнить состояние до/после и опубликовать
  `EVENT_UPDATED`, если `changes` не пуст;
* `EventService.deleteEvent()` — это фактически смена статуса на `CANCELLED`,
  поэтому также публикуется изменение статуса;
* `EventStatusScheduler.updateStatuses()` — публиковать смены на `STARTED` и
  `FINISHED` с `changedById = null`;
* `RegistrationService` — оставить без producer-вызовов;
* relation регистраций в `EventEntity`/`UserEntity` — источник списка
  `subscribers`.

Не стоит передавать в producer изменяемую JPA entity и вычислять diff уже после
выхода из метода: старое значение к этому моменту потеряно. Сначала снимите
нужные значения, затем примените update, потом соберите контракт.

Есть также транзакционная тонкость: простой `kafkaTemplate.send()` внутри
JPA-транзакции не гарантирует атомарность БД и Kafka. Для учебной итерации
обычно достаточно публиковать после успешного commit (например, через
transactional event listener). Промышленное решение — transactional outbox,
но добавлять его следует только если это требуется отдельно.

## 8. Безопасная обработка ошибок Kafka

Consumer не должен бесконечно падать на одном повреждённом сообщении. Нужно
настроить обработчик ошибок с ограниченным числом повторов и затем отправкой в
DLT (dead-letter topic) либо явным логированием/пропуском — в зависимости от
требований курса.

Разделяйте случаи:

* временно недоступна БД — retry имеет смысл;
* JSON не соответствует контракту — повтор не исправит сообщение, нужен DLT;
* `messageId` уже существует — это нормальная идемпотентная повторная доставка;
* список subscribers пуст — payload можно не создавать, если аудит событий не
  требуется заданием.

## 9. Scheduler очистки

Проще и безопаснее сначала удалить старые пользовательские строки, затем
payload, на которые больше никто не ссылается. Возраст обычно считают по
`notifications.created_at < now - 7 days`. Обе операции выполняются
транзакционно. Не удаляйте общий payload, пока на него существует хотя бы одна
inbox-ссылка.

## 10. Рекомендуемый порядок реализации

Каждый следующий этап должен начинаться только после зелёных тестов предыдущего:

1. **Gradle-каркас:** создать три модуля, перенести текущий сервис без изменения
   поведения, добиться `./gradlew build`.
2. **Контракт:** добавить records/enums в `event-common` и тест JSON
   round-trip, включая `changedById = null` и разные типы значений diff.
3. **Инфраструктура:** добавить Kafka и вторую PostgreSQL БД в Compose,
   независимые properties и Liquibase changelog notificator.
4. **Producer:** покрыть unit-тестами построение diff; отдельно update, cancel и
   обе scheduler-смены; убедиться, что registration ничего не публикует.
5. **Consumer и БД:** сначала идемпотентное сохранение `1 + N`, затем обработку
   retry/DLT.
6. **JWT notificator:** валидировать токен manager и надёжно извлекать `userId`.
7. **HTTP API:** repository → service → mapper → controller; реализовать GET и
   POST строго по OpenAPI.
8. **Cleanup scheduler:** удалить данные старше семи дней и покрыть тестом
   граничную дату.
9. **End-to-end:** пройти полный сценарий с двумя пользователями и повторной
   доставкой одного Kafka-сообщения.

## 11. Definition of Done

- [ ] Из корня собираются все три Gradle-модуля.
- [ ] Оба Spring Boot приложения запускаются одновременно.
- [ ] Kafka DTO определён только в `event-common`.
- [ ] Update/cancel/scheduler публикуют diff, registration — нет.
- [ ] Системная смена статуса содержит `changedById = null`.
- [ ] Повтор одного `messageId` не создаёт дубликаты.
- [ ] Одно сообщение для N подписчиков создаёт 1 payload и N inbox-строк.
- [ ] Notificator принимает JWT manager, но не выдаёт собственный.
- [ ] User identity берётся только из проверенного JWT.
- [ ] GET отдаёт только непрочитанные записи пользователя и `200 []` для пустого
  inbox.
- [ ] POST игнорирует чужие/несуществующие ID и возвращает 204.
- [ ] Старые записи удаляются без нарушения foreign key.
- [ ] Ошибки HTTP имеют единый `ErrorMessageResponse`.
- [ ] Плохое Kafka-сообщение не останавливает обработку следующих сообщений.

Если держать в голове одну формулу, то она такая:

> `event-manager` сообщает **что произошло и для кого**, `event-common`
> фиксирует **форму сообщения**, а `event-notificator` превращает сообщение в
> **персональный inbox**.