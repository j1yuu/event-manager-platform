## Краткий вывод

Архитектурное направление выбрано правильно: проект разбит на Gradle-модули, Kafka-контракт вынесен в `event-common`, producer использует transactional outbox, notificator разделяет общий payload и пользовательские inbox-записи, а `userId` для HTTP-операций берётся из JWT. При этом текущую реализацию пока нельзя считать готовой: корневая сборка не компилируется, у notificator нет миграций, а несколько SQL-запросов и контрактов содержат блокирующие ошибки.

## Что сделано по заданию

| Требование | Статус | Комментарий |
|---|---|---|
| Multi-module проект | ✅ | Корневой Gradle-проект включает `event-common`, `event-manager`, `event-notificator`. Замена Maven на Gradle согласована и сама по себе не является недостатком. |
| Общий Kafka-контракт | ✅/⚠️ | DTO находятся в `event-common`, однако названия/поля расходятся с заданием и OpenAPI (`UPDATE` вместо `EVENT_UPDATED`, `value` вместо `oldValue`). |
| Публикация обновлений event-manager | ✅/⚠️ | Обновление через `updateEvent` и смена статуса scheduler-ом записываются в outbox. Отмена через `deleteEvent` не создаёт событие. |
| Не публиковать регистрацию | ✅ | `RegistrationService` меняет регистрацию без enqueue Kafka-события. |
| Transactional outbox | ✅/⚠️ | Запись outbox выполняется в одной транзакции с бизнес-изменением. Dispatcher реализует lease/claim и признаёт возможность at-least-once доставки. SQL и cleanup требуют исправления. |
| Kafka consumer | ✅/⚠️ | Listener и JSON deserializer есть, но идемпотентность consumer-а и явная стратегия обработки poison message отсутствуют. |
| Payload один раз + inbox отдельно | ✅/⚠️ | JPA-модель разделена на две сущности, но нет миграции и уникальности `messageId`, поэтому повторная доставка дублирует обе записи. |
| `GET /notifications` | ✅/⚠️ | Возвращает список непрочитанных текущего пользователя и пустой список является нормальным ответом. DTO содержит неправильный `occurredAt`, а `message` всегда пустой. |
| `POST /notifications` | ✅/⚠️ | Фильтрация по текущему пользователю реализована, чужие/несуществующие id игнорируются. Форма body расходится с OpenAPI; repository query не создаётся из-за опечатки параметра. |
| Очистка старше 7 дней | ❌ | Scheduler не включён, удаляются только прочитанные по `readAt`, а не все записи по возрасту `createdAt`; SQL содержит ошибку параметра. |
| Проверка JWT event-manager токена | ✅/⚠️ | Проверяются подпись и issuer общим секретом; `userId` извлекается из subject. Ошибки токена превращаются в 401 через security chain. |
| Единый формат ошибок | ✅/⚠️ | DTO/handlers и security handlers существуют, но JSON-имена DTO (`detailed_message`, `date_time`) расходятся с OpenAPI (`detailedMessage`, `dateTime`). |
| Тесты | ❌ | Есть только пустые smoke-тесты контекста; интеграционный сценарий Kafka/DB/JWT, идемпотентность и API-поведение не покрыты. |

## Блокирующие замечания (исправить в первую очередь)

### 1. `[BLOCKER]` Проект не компилируется

После переноса исключений в `event-common` несколько сервисов продолжают импортировать удалённый пакет `kkashin.dev.eventmanager.exceptions.models`. Команда `./gradlew test` останавливается на `:event-manager:compileJava` с девятью ошибками. Нужно заменить эти импорты на `kkashin.dev.exceptions.*` и добавить компиляционную проверку в CI.

### 2. `[BLOCKER]` База event-notificator не создаётся

`event-notificator/src/main/resources/db/changelog/changelog-master.yaml` пуст. При `ddl-auto: none` Hibernate не создаст таблицы, поэтому consumer и HTTP API не смогут работать. Нужна Liquibase-миграция для `notification_payloads`/`notification_event_payloads` и `notifications` с PK, FK, `NOT NULL`, индексами и уникальными ограничениями.

Минимально полезные ограничения/индексы:

- `UNIQUE (message_id)` на payload — барьер идемпотентности;
- `UNIQUE (user_id, payload_id)` на inbox — защита от дублей получателя;
- FK `notifications.payload_id -> notification_payloads.payload_id`;
- индекс `(user_id, is_read, created_at DESC)` для GET;
- индекс по `created_at` для cleanup.

### 3. `[BLOCKER]` `NotificationRepository` не сможет создать query для cleanup

В SQL используется `:timestapmp`, а Java-параметр объявлен как `@Param("timestamp")`. Spring Data валидирует named parameters при создании repository bean; даже после добавления таблиц это способно остановить старт приложения. Кроме того, требование говорит об удалении записей старше семи дней, тогда как запрос удаляет только прочитанные записи по `read_at`. Следует сравнивать `created_at` с cutoff (если ментор не уточнил иную retention-политику) и покрыть граничное значение тестом.

### 4. `[BLOCKER]` Планировщик notificator не запускается

В `EventNotificatorApplication` отсутствует `@EnableScheduling`. Поэтому метод с `@Scheduled` не будет вызван. Добавьте аннотацию и тест, который проверяет сам метод на подготовленных данных независимо от ожидания таймера.

### 5. `[BLOCKER]` Outbox SQL содержит несколько дефектов

- `claim_token = null` никогда не истинно в SQL; требуется `claim_token IS NULL`.
- `clearSent` принимает `now`, но не использует его; сейчас политика хранения sent-записей не определена.
- PostgreSQL не разрешает квалифицировать целевой столбец alias-ом в `SET`: `set e.status = ...` следует заменить на `set status = ...` (аналогично прочим присваиваниям).
- Миграция создаёт `createdAt`, тогда как native query обращается к `created_at`. Необходимо выбрать единое snake_case имя.

Из-за этих ошибок dispatcher может получить runtime SQL error, а sent-сообщения точно не будут очищаться.

### 6. `[HIGH]` Consumer не идемпотентен при at-least-once доставке

Producer корректно допускает повторную доставку, если Kafka приняла сообщение, но `markSent` не сработал. Однако consumer всегда безусловно создаёт новый payload и новые notifications; `messageId` не проверяется и не защищён уникальным индексом. Это нарушает прямое назначение `messageId` из задания.

Рекомендуемый алгоритм:

1. Начать DB-транзакцию.
2. Вставить payload по уникальному `messageId` (`insert ... on conflict do nothing` либо обработать unique violation).
3. Если payload уже существует, считать сообщение успешно обработанным без создания дублей.
4. Создать inbox-строки с уникальностью `(user_id, payload_id)`.
5. Закоммитить DB-транзакцию; только после успеха listener должен позволить Kafka зафиксировать offset.

Это даёт практически полезную пару: **outbox + idempotent consumer**. Один outbox без дедупликации consumer-а не обеспечивает exactly-once бизнес-эффект.

## Высокий приоритет

### 7. Событие отмены не публикуется

`deleteEvent` меняет статус на `CANCELLED`, но не строит diff и не вызывает outbox. По заданию любая смена статуса должна публиковаться. Следует сформировать изменение `status: WAIT_START -> CANCELLED`, указать `changedById` текущего пользователя и enqueue его в той же транзакции.

### 8. Контракт Kafka и OpenAPI расходится с заданием

- `EventType` содержит `UPDATE`, тогда как задание/спецификация используют `EVENT_UPDATED`.
- `EventChangedFieldDto` называет старое значение `value`; ожидается `oldValue`.
- DTO использует `changedFields`; ожидается `changes`.
- `FieldType` не требуется заданием и делает внутренний контракт сложнее; если он действительно нужен UI, enum-константы принято писать `DECIMAL`, `STRING`, ... и стабилизировать их как часть контракта.

Сейчас JSON producer-а не совпадёт с опубликованным примером и OpenAPI payload. Лучше зафиксировать contract serialization test с точным JSON fixture.

### 9. Теряется `occurredAt`

`NotificationPayload` не хранит `occurredAt` из Kafka-события. Mapper HTTP-ответа вместо него отдаёт `payload.createdAt`, то есть время вставки consumer-ом. При задержке Kafka или replay эти моменты различаются. Добавьте отдельную колонку `occurred_at` и маппинг `dto.occurredAt()`.

### 10. Наружный DTO неполон и частично неверен

- `message` всегда равен пустой строке, хотя контракт требует короткий человеко-читаемый текст.
- В `Payload` отсутствует `messageId`, который обязателен в OpenAPI.
- `POST` принимает сырой JSON-массив (`[1,2]`), а OpenAPI требует объект `{ "notificationIds": [1,2] }`.
- `@Valid` на `List<Long>` не запрещает `null`, пустой список, null/отрицательные элементы. Нужен request DTO, например `@NotEmpty List<@NotNull @Positive Long> notificationIds`.

### 11. Публикуются не фактические изменения

Mapper добавляет поле в `changes`, если оно присутствует в PATCH/PUT DTO, даже когда новое значение равно старому. В результате возможны уведомления с `oldValue == newValue`; при полностью пустом update публикуется событие с пустым diff. Перед enqueue следует сравнивать нормализованные старое и новое значения и не публиковать сообщение, если diff пуст.

Дополнительно `eventName` берётся из старой сущности до применения update. При переименовании payload получит прежнее название, хотя описание предполагает snapshot на момент изменения.

### 12. Проверка capacity использует старую локацию

При одновременной смене `locationId` и `maxPlaces` проверка сравнивает лимит с `source.getEventLocation().getCapacity()`, а не с новой `location`. Это может либо пропустить недопустимое значение для меньшей новой площадки, либо ошибочно отклонить допустимое значение для большей. Нужно вычислить effective location и валидировать против неё.

### 13. Нет явной стратегии для плохих Kafka-сообщений

`ErrorHandlingDeserializer` установлен, но container factory не конфигурирует `DefaultErrorHandler`, backoff и dead-letter/recoverer. Следует явно решить и протестировать:

- сколько раз повторять transient DB/Kafka errors;
- куда отправлять невалидный JSON/несовместимый контракт (DLT);
- когда фиксировать offset, чтобы poison message не блокировал partition;
- какие метрики/логи позволят заметить потерю сообщения.

## Средний приоритет и качество реализации

1. **JPA enum:** `NotificationPayload.eventType` следует пометить `@Enumerated(EnumType.STRING)`, иначе по умолчанию сохраняется ordinal, хрупкий при перестановке enum-констант.
2. **Constraints:** `Notification.userId`, `payload_id`, payload JSON и `owner_id` по смыслу должны иметь `nullable = false` там, где контракт запрещает null.
3. **N+1:** `@EntityGraph` импортирован, но не используется. При `open-in-view: false` native query без fetch graph может привести к `LazyInitializationException` во время маппинга вне транзакции либо к N+1. Сделайте repository-метод с `@EntityGraph(attributePaths = "payload")`/JPQL fetch join или маппинг внутри read-only транзакции.
4. **Clock:** status scheduler использует `LocalDateTime.now()` вместо внедрённого `Clock`, хотя остальная новая логика уже подготовлена к детерминированным тестам.
5. **Лишний API:** `EventUpdatedProducer.sendAll` не используется; небольшой код, но лучше удалить либо применять осмысленно.
6. **Опечатки/именование:** `secondsToSubstract` → `retention`/`retentionDuration`; настройка `status-delay-ms` у notification cleanup названа не по назначению.
7. **Конфигурация:** `spring.json.trusted.packages` в YAML указывает несуществующий пакет `kkashin.dev.eventcommon.kafka`; Java-конфигурация задаёт корректный package программно, но конфликтующие настройки вводят в заблуждение.
8. **Безопасность:** общий HMAC secret приемлем для учебной итерации. Для реальной микросервисной схемы предпочтительнее асимметричная подпись: manager хранит private key, notificator — только public key. Также стоит валидировать назначение токена (`aud`) для API notificator.
9. **PII/логи:** не логируйте полный JWT. Текущий код этого не делает — это хорошо; сообщение причины parser exception допустимо, но stack trace/метрика были бы полезнее для диагностики без токена.
10. **Отдельные wrapper/settings:** внутри обоих сервисов остались собственные Gradle wrapper/settings. В настоящем multi-module репозитории достаточно корневого wrapper; дубли создают второй, потенциально несовместимый способ сборки.

## Что в решении особенно хорошо

1. **Выбор transactional outbox оправдан.** `enqueue` имеет `Propagation.MANDATORY`, поэтому разработчик не сможет случайно вызвать его вне бизнес-транзакции. Это хороший защитный инвариант.
2. **Claim/lease сделаны осмысленно.** `FOR UPDATE SKIP LOCKED`, claim token и lease позволяют масштабировать dispatcher и восстанавливать зависшие записи.
3. **Ключ Kafka — `messageId`.** Это сохраняет стабильное партиционирование конкретного сообщения и удобно для диагностики.
4. **Регистрация отделена от доменных событий изменения.** Требование «не публиковать при регистрации» соблюдено.
5. **Inbox разделён с payload.** Направление модели соответствует заданию и устраняет крупное дублирование JSON.
6. **Mark-as-read ограничен пользователем из JWT.** SQL одновременно фильтрует `notificationIds`, `user_id` и `is_read`; чужие и неизвестные id не вызывают ошибку.
7. **Время через `Clock` в новой логике.** Это хорошая основа для тестов retention и `readAt`.

## Рекомендуемый порядок исправлений

1. Починить импорты и добиться успешного `./gradlew clean test`.
2. Добавить Liquibase-схему notificator и исправить все native SQL; поднять обе PostgreSQL и проверить миграции.
3. Согласовать один контракт (`EVENT_UPDATED`, `oldValue/newValue`, `changes`) и добавить JSON contract test в обоих направлениях.
4. Реализовать consumer idempotency через DB constraints и транзакцию.
5. Исправить все источники событий: ручной update, cancel, scheduler; не отправлять пустой diff.
6. Исправить HTTP request/response по OpenAPI, включая `messageId`, настоящий `occurredAt` и `message`.
7. Включить cleanup scheduler и реализовать retention по `createdAt`.
8. Настроить retry/DLT и наблюдаемость consumer-а.
9. Добавить интеграционные тесты и только затем пройти ручной end-to-end сценарий.

## Минимальный набор тестов перед сдачей

- `EventMapperTest`: одно изменённое поле, несколько полей, равные значения, пустой update, scheduler status, cancel status, `changedById == null` для scheduler.
- `EventOutboxServiceTest` с PostgreSQL/Testcontainers: атомарный rollback, конкурентный claim, истёкший lease, release, mark sent, cleanup.
- Contract test Jackson: producer JSON точно десериализуется consumer DTO и имеет ожидаемые имена полей.
- `NotificationService` integration test: одно сообщение/несколько subscribers создаёт один payload и несколько inbox; повтор того же `messageId` ничего не дублирует.
- Repository test: GET отдаёт только unread текущего пользователя в правильном порядке; mark-read игнорирует чужие/несуществующие id.
- MVC security test: нет/битый/истёкший/чужой issuer JWT → единый 401; валидный manager JWT → 200/204.
- Cleanup test: старше, ровно на границе и младше семи дней; orphan payload удаляется только после удаления последнего inbox.
- End-to-end: update и scheduler/cancel проходят цепочку manager DB → outbox → Kafka → notificator DB → HTTP.

## Итоговая оценка готовности

С точки зрения учебной архитектуры выполнена существенная часть итерации: основные модули и поток данных спроектированы, а outbox — сильное улучшение относительно прямого `KafkaTemplate.send()` внутри транзакции. С точки зрения запуска и проверяемого поведения итерация пока **не готова к приёмке** из-за compile failure, отсутствующих миграций, SQL-дефектов, отсутствия consumer idempotency и расхождения HTTP/Kafka-контрактов. После исправления blocker/high пунктов решение будет иметь хорошую архитектурную основу.