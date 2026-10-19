# Протокол экспертной панели — TWINS-798 Code Review

**Дата:** 2026-06-12
**Задача:** Review ветки TWINS-798 (common duplicate service) — можно ли мёржить в main
**Тип:** Код-ревью

---

## Состав панели

### Ядро
- **Алексей «Навигатор»** — Модератор-фасилитатор
- **Марина «Скальпель»** — Критик / Devil's Advocate
- **Игорь «Телескоп»** — Системный аналитик

### Динамические специалисты
- **Дмитрий «Бин»** — Java/Spring Boot Enterprise — *Причина подключения: Java 21 + Spring Boot 3.5 рефакторинг сервисного слоя*
- **Тимур «Индекс»** — Data Engineer/DBA — *Причина подключения: транзакционность, N+1, batch-операции с БД*
- **Олег «Замок»** — Security-архитектор — *Причина подключения: input validation, authorization, batch API security*

---

## Исследовательская база

### Ключевые источники
1. Spring @Transactional Official Docs — https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html — *Дмитрий, Игорь*
2. Self-Invocation Problem with @Transactional — https://www.linkedin.com/pulse/understanding-self-invocation-problem-springs-annotation-souza-wsynf — *Игорь*
3. Spring Transactional Propagation (Baeldung) — https://www.baeldung.com/spring-transactional-propagation-isolation — *Тимур*
4. N+1 Problem in Hibernate (Baeldung) — https://www.baeldung.com/spring-hibernate-n1-problem — *Тимур*
5. OWASP API Security Top 10 — https://owasp.org/API-Security/ — *Олег*
6. OWASP API4:2023 Unrestricted Resource Consumption — https://www.cloudflare.com/learning/security/api/owasp-api-security-top-10/ — *Олег*
7. Template Method Pattern — https://refactoring.guru/design-patterns/template-method — *Марина*
8. Bulk Insert Performance in Spring Data JPA — https://shekhargulati.com/2020/05/11/improving-spring-data-jpa-hibernate-bulk-insert-performance-by-more-than-100-times/ — *Тимур*
9. Spring @Transactional in Abstract Classes — https://stackoverflow.com/questions/59103394/using-spring-transaction-proxy-between-abstract-class-and-child-method — *Игорь*

---

## Раунд 1 — Разведка (независимые позиции)

### Марина «Скальпель»
**Позиция:** Мёржить — нет блокеров, чистый рефакторинг без изменения поведения
**Найденные проблемы:**
- Нет тестов для `TwinStatusDuplicateService`
- `@Transactional` на self-invocation создаёт ложное чувство безопасности
- Dead fields: `newTwinClassFieldId`, `newTwinStatusId`
- Документация 545 строк на ~435 строк кода — чрезмерно
**Ключевой вопрос:** Стоит ли вынести `EntityDuplicateService` в cambium framework?

### Игорь «Телескоп»
**Позиция:** Мёржить с условиями — 3 минорных фикса
**Найденные проблемы:**
- Лишнее `@Transactional` на self-invoked `duplicateFieldsForClass()` / `duplicateStatusesForClass()`
- Doc drift: сигнатура `createNewEntity(D, E)` в документации не совпадает с `createNewEntity(D)` в коде
- Мёртвый код: `newTwinClassId` генерируется, но не используется
**Позитив:** Template Method применён корректно, SRP достигнут, ROI положительный

### Дмитрий «Бин»
**Позиция:** НЕ мёржить без исправления критического бага
**КРИТИЧЕСКАЯ НАХОДКА:** Отсутствие `rollbackFor = Throwable.class` на `@Transactional` в `EntityDuplicateService`. `ServiceException` — checked exception, Spring по умолчанию откатывает только `RuntimeException`. При ошибке в `afterSave()` TwinClass будет сохранён, а поля/статусы — нет. Нарушает конвенцию проекта (80+ мест используют `rollbackFor = Throwable.class`).
**Дополнительно:**
- Нет валидации null `newKey`
- Нет ограничения batch size
- 4 `@Disabled` теста

### Тимур «Индекс»
**Позиция:** Мёржить с условиями — N+1 и key uniqueness не блокеры (существовали до PR)
**Найденные проблемы:**
- N+1 в i18n: ~460 queries на один duplicate TwinClass с 20 полями и 5 статусами
- Нет проверки key uniqueness в БД (только HashSet в рамках batch)
- Регрессия: старый код batch-загружал fields/statuses для всех классов разом, новый — по одному
- `saveAll()` через Hibernate `merge()` = SELECT + INSERT вместо batch INSERT
**Ключевой вопрос:** Каков реальный batch size в API — сколько TwinClass одновременно дублируют?

### Олег «Замок»
**Позиция:** Мёржить с условиями — 2 action item до мёржа
**Найденные проблемы:**
- Нет batch size лимита (OWASP API4:2023) — можно отправить 10 000+ элементов
- Нет `@Valid` на `@RequestBody` — DTO без bean-validation
- `statusDetails` может утекать в response (содержит пользовательский ввод)
- Нет проверки key uniqueness в БД
- `@CrossOrigin(origins = "*")` на private API (но это общий паттерн проекта)
**Позитив:** Authorization корректна, IDOR защищён через `isEntityReadDenied()`, domain isolation работает

### Точки напряжения (выявлены Навигатором)
1. **rollbackFor** — Дмитрий считает критическим, другие не акцентировали. Кто прав?
2. **Batch size limit** — Олег считает критическим, Марина/Тимур — нет регрессии
3. **@Valid на DTO** — Олег единственный поднял, остальные не фокусировались
4. **Self-invocation @Transactional** — Игорь хочет убрать, Марина хочет оставить с комментарием
5. **N+1 в i18n** — Тимур считает HIGH, другие — существующий долг, не блокер

---

## Раунд 2 — Столкновение

### Точка 1: rollbackFor = Throwable.class
**Дмитрий:** ServiceException — checked, без rollbackFor транзакция не откатится. TwinClass сохранится, поля нет. Нарушает конвенцию проекта (80+ мест).
**Марина:** Это не регрессия — старый код тоже без rollbackFor. Но раз рефакторим — почему не исправить?
**Игорь:** Самая важная находка ревью. 2 строки исправления.
**Олег:** Data integrity issue — "полудублированные" данные.

**КОНСЕНСУС:** Исправить ДО мёржа. 2 строки в `EntityDuplicateService`.

### Точка 2: @Valid и batch-size лимит
**Олег:** OWASP API4:2023. Один запрос = тысячи INSERT. Нет @Valid, нет @Size.
**Дмитрий:** Поддерживаю — не только security, но и performance.
**Марина:** Не регрессия — те же DTO были в старом коде. Но @Valid + @Size — это 3 строки, стоит сделать.
**Игорь:** Минимальное усилие, максимальный эффект.

**КОНСЕНСУС:** Добавить ДО мёржа. ~10 строк в DTO/контроллерах.

### Точка 3: Self-invocation @Transactional
**Игорь:** Убрать — вводит в заблуждение.
**Дмитрий:** Бессмысленна, но безвредна. Можно убрать для чистоты.
**Марина:** Оставить — страховка на случай прямого вызова извне.

**КОНСЕНСУС:** Оставить. Post-merge добавить комментарий. Не блокер.

### Точка 4: N+1 в i18n
**Тимур:** ~460 queries на один duplicate. HIGH severity.
**Марина:** Существующий тех.долг. TODO были в старом коде.
**Дмитрий:** HIGH но не блокер. Отдельный тикет.

**КОНСЕНСУС:** Не блокер. Создать отдельный тикет на batch i18n duplication.

### Точка 5: Doc drift и dead code
**Игорь:** Сигнатура `createNewEntity(D, E)` в doc не соответствует `createNewEntity(D)`.
**Марина:** Dead fields `newTwinClassFieldId`, `newTwinStatusId`.
**Дмитрий:** Doc drift — разработчик создаст неправильную сигнатуру.

**КОНСЕНСУС:** Исправить doc drift ДО мёржа. Dead fields — post-merge.

---

## Quality Gates

- [x] Все 5 экспертов высказались
- [x] Марина нашла контраргументы: проверила что "критические" проблемы — не регрессии
- [x] Каждый эксперт использовал свои antipatterns (Java: N+1, @Transactional; Data: batch performance; Security: OWASP API4)
- [x] Рекомендации конкретные: `rollbackFor`, `@Valid`, `@Size(max=50)`, doc fix
- [x] Риски с mitigation strategies
- [x] Источники с URL: 9 источников
- [x] Разногласия документированы и разрешены

---

## Синтез

### Ключевые выводы
1. Рефакторинг качественный — Template Method применён корректно, SRP достигнут, ~330 строк дублированного кода устранено
2. Одна критическая находка: отсутствие `rollbackFor = Throwable.class` (нарушает конвенцию проекта, риск data integrity)
3. Все остальные проблемы — либо существующий тех.долг (N+1, key uniqueness), либо косметика (dead fields, doc drift)

### Рекомендации (по приоритету)

| # | Рекомендация | Приоритет | Обоснование | Кто поддерживает |
|---|-------------|-----------|-------------|------------------|
| 1 | Добавить `rollbackFor = Throwable.class` на `@Transactional` в `EntityDuplicateService` | Критический | Data integrity, конвенция проекта (80+ мест) | Дмитрий, Игорь, Марина, Олег |
| 2 | Добавить `@Valid` на `@RequestBody` + `@NotNull`/`@Size` на DTO fields | Критический | OWASP API4:2023, input validation | Олег, Дмитрий, Марина |
| 3 | Исправить doc drift: сигнатура `createNewEntity(D)` вместо `createNewEntity(D, E)` | Высокий | Документация не соответствует коду | Игорь, Дмитрий |
| 4 | Создать тикет на batch i18n duplication (~460 queries -> ~6 queries) | Высокий | Production performance | Тимур, Дмитрий |
| 5 | Создать тикет на DB key uniqueness check | Высокий | Race condition, data corruption | Тимур, Олег |
| 6 | Добавить тесты для `TwinStatusDuplicateService` | Средний | Покрытие 2/3 сервисов | Марина |
| 7 | Удалить dead fields (`newTwinClassFieldId`, `newTwinStatusId`, `newTwinClassId` generation) | Низкий | Мёртвый код | Марина, Игорь |
| 8 | Добавить комментарий на self-invoked `@Transactional` | Низкий | Читаемость | Игорь, Марина |

### Риски и открытые вопросы

| Риск | Вероятность | Влияние | Mitigation | Владелец |
|------|------------|---------|------------|----------|
| rollbackFor: полудублированные данные при ServiceException | Средняя | Data corruption | Добавить rollbackFor (п.1) | Backend |
| Batch DoS: неограниченный размер batch | Средняя | DB overload | Добавить @Size(max=50) (п.2) | Backend |
| N+1 в i18n при batch duplicate > 5 элементов | Высокая | Degraded performance | Batch i18n (п.4) | Backend |
| Race condition на key uniqueness | Низкая | Duplicate key violation 500 | DB check (п.5) | Backend |
| Doc drift误导 будущих разработчиков | Средняя | Неправильная реализация | Fix doc (п.3) | Docs |

### Инсайты
1. Дмитрий выявил критический баг, который остальные пропустили — `rollbackFor` с checked exceptions. Это показывает ценность Java-specific экспертизы.
2. Олег корректно оценил что security-проблемы (batch size, @Valid) — не регрессии, но стоит исправить пока код свежий.
3. Тимур подсчитал что i18n N+1 создаёт ~460 queries на один duplicate — это существующий долг, но рефакторинг был моментом его решить.
4. Все эксперты согласны что Template Method применён чисто и архитектура улучшилась.

### Нерешённые разногласия
Нет. Все 5 точек напряжения разрешены через консенсус.

---

## Финальные позиции экспертов

- **Марина «Скальпель»:** Мёржить после исправления rollbackFor и @Valid. Рефакторинг чистый, без изменения поведения. Риск низкий.
- **Игорь «Телескоп»:** Мёржить с 3 фиксами (rollbackFor, @Valid, doc). Архитектура улучшилась, ROI положительный, SRP достигнут.
- **Дмитрий «Бин»:** Мёржить после rollbackFor (критический) и @Valid (рекомендуемый). Это самый важный PR-баг — без rollbackFor возможна потеря данных.
- **Тимур «Индекс»:** Мёржить. N+1 и key uniqueness — существующий долг, не регрессия. Создать тикеты на следующую итерацию.
- **Олег «Замок»:** Мёржить после @Valid и @Size. Authorization не ослаблена. Остальные security-рекомендации — post-merge.

---

## Конкретные исправления (код)

### 1. EntityDuplicateService.java — добавить rollbackFor

```java
@Transactional(rollbackFor = Throwable.class)
public E duplicate(D duplicate) throws ServiceException { ... }

@Transactional(rollbackFor = Throwable.class)
public Collection<E> duplicate(Collection<D> duplicates) throws ServiceException { ... }
```

### 2. DTO — добавить validation

```java
// TwinClassDuplicateDTOv1.java (и аналогично для TwinClassField, TwinStatus)
@NotNull
public UUID originalTwinClassId;
@NotBlank @Size(max = 36)
public String newKey;
```

```java
// TwinClassDuplicateRqDTOv1.java
@NotNull @Size(min = 1, max = 50)
public List<@Valid TwinClassDuplicateDTOv1> duplicates;
```

```java
// Контроллеры — добавить @Valid
public ResponseEntity<?> twinClassDuplicateV1(
        @MapperContextBinding(...) MapperContext mapperContext,
        @Valid @RequestBody TwinClassDuplicateRqDTOv1 request) {
```

### 3. api_duplicate_architecture.md — исправить сигнатуру

```java
// Было (неверно):
protected abstract E createNewEntity(D duplicate, E original) throws ServiceException;

// Стало (верно):
protected abstract E createNewEntity(D duplicate) throws ServiceException;
```

---

## Источники
1. https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html
2. https://www.linkedin.com/pulse/understanding-self-invocation-problem-springs-annotation-souza-wsynf
3. https://www.baeldung.com/spring-transactional-propagation-isolation
4. https://www.baeldung.com/spring-hibernate-n1-problem
5. https://owasp.org/API-Security/
6. https://www.cloudflare.com/learning/security/api/owasp-api-security-top-10/
7. https://refactoring.guru/design-patterns/template-method
8. https://shekhargulati.com/2020/05/11/improving-spring-data-jpa-hibernate-bulk-insert-performance-by-more-than-100-times/
9. https://stackoverflow.com/questions/59103394/using-spring-transaction-proxy-between-abstract-class-and-child-method

---

*Протокол сгенерирован навыком «Экспертная панель»*
