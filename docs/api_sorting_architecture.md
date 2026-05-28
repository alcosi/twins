# Архитектура: Динамическая сортировка для Search API

**Дата:** 2026-05-28 (обновлено)
**Статус:** Реализовано (пилот на DomainBusinessAccountUserSearch)
**Контекст:** TWINS-831 — отсутствует сортировка на 74+ search API

---

## Контекст

В проекте 74 контроллера поиска с пагинацией (`@SimplePaginationParams`), но без динамической сортировки. `sortField` захардкожен в аннотации на compile-time. Клиент не может изменить поле сортировки. Особенно критично: сортировка по полям RelatedObjects (например, `user.name` вместо `userId`) требует JOIN к связанным таблицам.

## Экспертная панель

**Состав:** Алексей «Навигатор» (модератор), Марина «Скальпель» (критик), Игорь «Телескоп» (аналитик), Дмитрий «Бин» (Java/Spring), Тимур «Индекс» (PostgreSQL), Виктор «Секундомер» (performance).

---

## Решение

### Принцип

Сортировка реализуется через **простой enum per entity** в пакете `enums.sort`. Enum содержит только имена полей — без JPA-логики и без fieldPath. JPA Specification собирается в **SearchService** через switch по enum, который вызывает статический helper `CommonSpecification.toSortSpecification(fieldPath, ascending)`. Sort-поля инлайнятся прямо в **SearchDTO** — Swagger автоматически показывает dropdown со значениями.

### Почему Specification-only, а не Pageable.getSort()

Spring Data JPA баг [#2253](https://github.com/spring-projects/spring-data-jpa/issues/2253): при комбинации Specification + `Pageable.getSort()` по nested path (`user.name`) Spring Data создаёт **дублирующий JOIN** поверх того, что Specification уже присоединила. Это приводит к некорректным result sets и ломает count-запросы.

### Почему enum, а не annotation

- Enum = compile-time type safety + естественный whitelist
- Enum = Swagger автоматически показывает dropdown (Jackson десериализует из JSON)
- Enum = легко тестировать
- Annotation `@SortableFields` на контроллере = смешивание ответственности (controller знает про entity graph)

### Почему enum без fieldPath, а switch в SearchService

- Enum в `enums.sort` не зависит от DAO-слоя — чистый список имён
- SearchService владеет знанием о JPA entity graph — fieldPath логично жить рядом
- Switch даёт полную свободу: для конкретного поля можно добавить кастомную логику (подзапрос, другой JOIN-тип, etc.)
- При добавлении нового sort-поля: добавляем значение в enum + case в switch

### Почему sort inline в SearchDTO, а не отдельный SortDTO

- `SortDTOv1` с `String field` — Swagger показывает текстовое поле, клиент не видит допустимые значения
- Enum-тип прямо в SearchRqDTO — Swagger автоматически генерирует dropdown
- Jackson автоматически десериализует JSON-строку в enum, невалидное значение → 400 Bad Request
- Убирается промежуточный `SortDTOReverseMapper` — маппинг тривиальный
- Sort-поля лежат в `SearchRqDTO` рядом с `search`, а не внутри `SearchDTO` — это позволяет переиспользовать `SearchDTO` в других API (например, группировка) где сортировка не нужна

---

## Компоненты

### 1. Enum в `enums.sort` — простой список имён

```java
package org.twins.core.enums.sort;

public enum DomainBusinessAccountUserSortField {
    createdAt,
    lastActivityAt,
    userName,
    businessAccountName
}
```

Без конструктора, без fieldPath, без SortField<T>. Чистый data carrier.

### 2. Статический helper в `CommonSpecification`

```java
// CommonSpecification.java
public static <T> Specification<T> toSortSpecification(String[] fieldPath, boolean ascending) {
    if (fieldPath == null)
        return (root, query, cb) -> cb.conjunction();
    return (root, query, cb) -> {
        if (query.getResultType().equals(Long.class))
            return cb.conjunction();
        Path<?> sortPath = getFieldPath(root, JoinType.LEFT, fieldPath);
        List<Order> orders = new ArrayList<>(query.getOrderList());
        orders.add(ascending ? cb.asc(sortPath) : cb.desc(sortPath));
        query.orderBy(orders);
        return cb.conjunction();
    };
}
```

Логика: count-query guard → `AbstractSpecification.getFieldPath(root, LEFT, fieldPath)` → `orderBy`.

### 3. SearchRqDTO — sort-поля рядом с search

```java
// DomainBusinessAccountUserSearchRqDTOv1.java
public class DomainBusinessAccountUserSearchRqDTOv1 extends Request {
    public DomainBusinessAccountUserSearchDTOv1 search;

    @Schema(description = "Sort field. Default: createdAt")
    public DomainBusinessAccountUserSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
```

Sort-поля лежат на уровне `SearchRqDTO`, а не внутри `SearchDTO`. Это позволяет переиспользовать `SearchDTO` в API группировки и других endpoint'ах, где сортировка не нужна.
Swagger автоматически показывает dropdown для `sortField`: `createdAt`, `lastActivityAt`, `userName`, `businessAccountName`.

### 4. SearchDTO — чистые параметры поиска

```java
// DomainBusinessAccountUserSearchDTOv1.java — только параметры поиска, без sort
public class DomainBusinessAccountUserSearchDTOv1 {
    public Set<UUID> userIdList;
    public Set<UUID> businessAccountIdList;
    public DataTimeRangeDTOv1 lastActivityAt;
    // ... только критерии поиска
}
```

### 5. Domain Search Object — прямые поля

```java
// DomainBusinessAccountUserSearch.java
public DomainBusinessAccountUserSortField sortField = DomainBusinessAccountUserSortField.createdAt;
public SortDirection sortDirection = SortDirection.ASC;
```

Без `SortOption<S>` — прямые поля enum + SortDirection.

### 6. SearchRqDTOReverseMapper — маппинг search + sort

```java
// DomainBusinessAccountUserSearchRqDTOReverseMapper.convert():
var dst = domainBusinessAccountUserSearchDTOReverseMapper.convert(src.getSearch(), mapperContext);
dst
    .setSortField(src.getSortField())
    .setSortDirection(src.getSortDirection());
return dst;
```

SearchDTOReverseMapper маппит только критерии поиска. Sort-поля маппятся на уровне RqDTO mapper.
Jackson уже десериализовал enum из JSON, маппинг тривиальный. `SortDTOReverseMapper` не нужен.

### 7. SearchService — switch по enum

```java
// DomainBusinessAccountUserSearchService.java
private Specification<DomainBusinessAccountUserEntity> createSortSpecification(DomainBusinessAccountUserSearch search) {
    DomainBusinessAccountUserSortField sortField = search.getSortField();
    if (sortField == null)
        sortField = DomainBusinessAccountUserSortField.createdAt;
    boolean ascending = search.getSortDirection() != SortDirection.DESC;
    String[] fieldPath = switch (sortField) {
        case createdAt -> new String[]{DomainBusinessAccountUserEntity.Fields.createdAt};
        case lastActivityAt -> new String[]{DomainBusinessAccountUserEntity.Fields.lastActivityAt};
        case userName -> new String[]{DomainBusinessAccountUserEntity.Fields.user, UserEntity.Fields.name};
        case businessAccountName -> new String[]{DomainBusinessAccountUserEntity.Fields.businessAccount, BusinessAccountEntity.Fields.name};
    };
    return toSortSpecification(fieldPath, ascending);
}
```

**Как это работает:**
- `createdAt` → `fieldPath = ["createdAt"]` → `getFieldPath` берёт `root.get("createdAt")` — без JOIN
- `userName` → `fieldPath = ["user", "name"]` → `getFieldPath` делает `root.join("user", LEFT)` + `.get("name")`
- `AbstractSpecification.getFieldPath` + `getOrCreateJoin` гарантируют, что если JOIN уже существует (от фильтрации), он будет переиспользован, а не дублирован

### 8. Контроллер — без изменений

```java
@SimplePaginationParams SimplePagination pagination,
@RequestBody DomainBusinessAccountUserSearchRqDTOv1 request
```

`pagination.setSort(null)` в сервисе убирает Sort из Pageable. Sort field приходит из request body через search DTO.

---

## Индексы (обязательно)

```sql
-- Для сортировки по user.name
CREATE INDEX IF NOT EXISTS idx_user_name ON "user"(name);

-- Для сортировки по business_account.name
CREATE INDEX IF NOT EXISTS idx_business_account_name ON business_account(name);

-- Для JOIN lookup (опционально, ускоряет Nested Loop)
CREATE INDEX IF NOT EXISTS idx_domain_business_account_user_user_domain
    ON domain_business_account_user(user_id, domain_id);
CREATE INDEX IF NOT EXISTS idx_domain_business_account_user_ba_domain
    ON domain_business_account_user(business_account_id, domain_id);
```

---

## Performance оценка

| Записей в домене | Sort по created_at (с инд.) | Sort по user.name (БЕЗ инд.) | Sort по user.name (С инд.) |
|---|---|---|---|
| 1,000 | ~2ms | ~5ms | ~2ms |
| 10,000 | ~3ms | ~30ms | ~3ms |
| 100,000 | ~5ms | **~350ms** | ~5ms |
| 1,000,000 | ~10ms | **~4000ms** | ~8ms |

**Вывод:** без индексов сортировка по JOIN-полям деградирует как O(N log N). С правильными индексами — практически константное время благодаря early termination при LIMIT.

---

## Критические правила

1. **`getResultType()` guard** — ОБЯЗАТЕЛЬНО в `CommonSpecification.toSortSpecification()`. Count-запрос не должен содержать ORDER BY.
2. **LEFT JOIN, не INNER JOIN** — чтобы не отсекать записи с NULL-связями.
3. **Whitelist** — enum values = единственные допустимые sort fields. Невалидное значение → 400 от Jackson.
4. **Pageable без Sort** — при Specification-based сортировке Sort в Pageable должен быть `unsorted` (`pagination.setSort(null)`).
5. **Не трогать TwinSorter** — существующий featurer-based механизм сортировки Twin решает другую задачу (динамические поля).

---

## API contract

**Request:**
```json
{
  "search": {
    "userIdList": ["..."]
  },
  "sortField": "userName",
  "sortDirection": "DESC"
}
```

Sort-поля на верхнем уровне `SearchRqDTO`, а не внутри `search`.

Jackson автоматически десериализует `sortField` из JSON-строки в enum. Невалидное значение → 400 Bad Request с понятной ошибкой.

---

## Поэтапный rollout

| Фаза | Что | Статус |
|---|---|---|
| 1 | Enum в `enums.sort` + inline sort в SearchDTO + switch в SearchService для DomainBusinessAccountUserSearch | **Готово** |
| 2 | Обобщение на 3-5 приоритетных API | После валидации на пилоте |
| 3 | Шаблон для новых search API (enum обязателен) | Постоянно |
| 4 | Cursor-based pagination для таблиц > 500K (если потребуется) | По мере роста данных |

---

## Устаревшие компоненты (оставлены для backward compat)

Следующие компоненты больше не используются новым паттерном, но оставлены в коде:

- `SortDTOv1` — заменён на inline sort-поля в SearchDTO (enum-тип)
- `SortDTOReverseMapper` — заменён на прямой маппинг в SearchDTOReverseMapper
- `SortField<T>` интерфейс — заменён на статический helper в `CommonSpecification`
- `SortOption<S>` — заменён на прямые поля `sortField` + `sortDirection`

При масштабировании на все API эти компоненты будут удалены.

---

## Источники

- [Spring Data JPA #2253: Specifications with Sort create additional join](https://github.com/spring-projects/spring-data-jpa/issues/2253)
- [Spring Data JPA #4178: Sort field whitelist validation](https://github.com/spring-projects/spring-data-jpa/issues/4178)
- [CVE-2016-6652: Blind SQL/JPQL Injection via Sort Parameters](https://spring.io/security/cve-2016-6652)
- [PGAnalyze: Postgres Planner Quirks with ORDER BY + LIMIT](https://pganalyze.com/blog/5mins-postgres-planner-order-by-limit)
- [Milan Jovanovic: Cursor Pagination Deep Dive](https://www.milanjovanovic.tech/blog/understanding-cursor-pagination-and-why-its-so-fast-deep-dive)
- [Baeldung: Joining Tables With Specifications](https://www.baeldung.com/spring-jpa-joining-tables)
- [Vlad Mihalcea: JOIN FETCH and Pagination](https://vladmihalcea.com/join-fetch-pagination-spring/)
