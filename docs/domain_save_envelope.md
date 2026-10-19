# Domain Save Envelope

**Status:** Accepted
**Date:** 2026-06-17

A pattern for carrying create/update data between REST mappers and services **without polluting JPA entities with runtime carrier fields**.

---

## 1. Problem

When a REST request needs to create or update an entity that owns i18n FK columns (`name_i18n_id`, `description_i18n_id`, etc.), the request body arrives with embedded i18n payloads (locale → translation). These need to be:

1. Converted to `I18nEntity` + `I18nTranslationEntity` graphs (reverse mapper's job)
2. Persisted via `I18nService.createI18nAndTranslations(...)` (service's job — the service decides the `I18nType`, handles bulk creation, etc.)
3. Resolved into an FK id and set on the target entity

Without a dedicated carrier, mappers do step 1 and then **stuff the resulting `I18nEntity` into a `@Transient` runtime field on the entity**, which the service later reads back:

```java
// ❌ Anti-pattern
@Entity
public class NotificationSchemaEntity {
    @Column(name = "name_i18n_id")
    private UUID nameI18nId;

    @Transient
    private I18nEntity nameI18n;   // ← carrier, not a real JPA field
}
```

This is wrong because:

- The entity mixes **persistence state** (FK id) with **request-scoped carrier data** (`I18nEntity`)
- The carrier field must be excluded from `equals`, `hashCode`, `toString` manually
- It is invisible in the type system that the field exists only for create/update flows
- Lazy devs reuse the field for unrelated runtime concerns, blurring boundaries further
- It makes entity tests awkward — you have to know which fields persist and which don't

---

## 2. Solution

Introduce a **domain envelope class** in `org.twins.core.domain.<module>`. The envelope holds:

- The entity being created/updated (with only its raw columns populated by the mapper)
- Auxiliary `I18nEntity` instances that the service will resolve into FK ids
- Any other non-FK, non-entity helper data the service needs

```java
@Data
@Accessors(chain = true)
public class NotificationSchemaSave {
    public NotificationSchemaEntity notificationSchema;
    public I18nEntity nameI18n;
    public I18nEntity descriptionI18n;
}
```

The entity stays clean — only real columns and JPA-mapped relations. No `@Transient I18nEntity` runtime fields.

---

## 3. Layer responsibilities

| Layer                       | Responsibility                                                                                  |
| --------------------------- | ----------------------------------------------------------------------------------------------- |
| **Controller**              | Receives DTO, calls reverse mapper with `MapperContext`, hands envelope to service              |
| **Reverse mapper (DTO→Save)** | Builds envelope: creates entity with non-i18n columns set, converts i18n DTOs to `I18nEntity` |
| **Service**                 | Calls `i18nService.createI18nAndTranslations(...)` for each auxiliary `I18nEntity`, sets FK id on entity, persists entity |
| **Entity**                  | Pure persistence. No carrier fields.                                                            |

---

## 4. Variants: `XxxCreate` / `XxxUpdate`

When create and update payloads differ (e.g. update requires an `id`), introduce two subclasses:

```
NotificationSchemaSave           (envelope: entity + I18nEntity fields)
├── NotificationSchemaCreate     (empty marker, or create-only fields)
└── NotificationSchemaUpdate     (adds UUID id)
```

Mappers:
- `NotificationSchemaCreateRestDTOReverseMapper` — DTO → `NotificationSchemaCreate`
- `NotificationSchemaUpdateRestDTOReverseMapper` — DTO → `NotificationSchemaUpdate`
- `NotificationSchemaSaveRestDTOReverseMapper` — shared base, used by both

Service:
- `createNotificationSchema(Collection<NotificationSchemaCreate>)`
- `updateNotificationSchema(Collection<NotificationSchemaUpdate>)` — looks up existing entity by id, mutates it, persists

---

## 5. Concrete example — NotificationSchema

### 5.1 Envelope

```java
@Data
@Accessors(chain = true)
public class NotificationSchemaSave {
    public NotificationSchemaEntity notificationSchema;
    public I18nEntity nameI18n;
    public I18nEntity descriptionI18n;
}

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class NotificationSchemaCreate extends NotificationSchemaSave { }

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class NotificationSchemaUpdate extends NotificationSchemaSave {
    private UUID id;
}
```

### 5.2 Reverse mapper (DTO → envelope)

```java
@Component
@RequiredArgsConstructor
public class NotificationSchemaSaveRestDTOReverseMapper
        extends RestSimpleDTOMapper<NotificationSchemaSaveDTOv1, NotificationSchemaSave> {

    private final I18nSaveRestDTOReverseMapper i18nSaveRestDTOReverseMapper;

    @Override
    public void map(NotificationSchemaSaveDTOv1 src, NotificationSchemaSave dst, MapperContext mapperContext) throws Exception {
        I18nEntity nameI18n = i18nSaveRestDTOReverseMapper.convert(src.getNameI18n(), mapperContext);
        I18nEntity descriptionI18n = i18nSaveRestDTOReverseMapper.convert(src.getDescriptionI18n(), mapperContext);
        dst
                .setNameI18n(nameI18n)
                .setDescriptionI18n(descriptionI18n)
                .setNotificationSchema(new NotificationSchemaEntity());
    }
}
```

### 5.3 Service

```java
@Transactional(rollbackFor = Throwable.class)
public List<NotificationSchemaEntity> createNotificationSchema(
        Collection<NotificationSchemaCreate> entities) throws ServiceException {
    if (entities == null || entities.isEmpty()) return Collections.emptyList();

    i18nService.createI18nAndTranslations(I18nType.NOTIFICATION_SCHEMA_NAME,
            entities.stream().map(NotificationSchemaCreate::getNameI18n).filter(Objects::nonNull).toList());
    i18nService.createI18nAndTranslations(I18nType.NOTIFICATION_SCHEMA_DESCRIPTION,
            entities.stream().map(NotificationSchemaCreate::getDescriptionI18n).filter(Objects::nonNull).toList());

    List<NotificationSchemaEntity> entitiesToSave = new ArrayList<>();
    for (NotificationSchemaCreate notificationSchema : entities) {
        NotificationSchemaEntity entity = new NotificationSchemaEntity()
                .setDomainId(authService.getApiUser().getDomainId())
                .setNameI18nId(notificationSchema.getNameI18n() != null
                        ? notificationSchema.getNameI18n().getId() : null)
                .setDescriptionI18nId(notificationSchema.getDescriptionI18n() != null
                        ? notificationSchema.getDescriptionI18n().getId() : null)
                .setCreatedByUserId(authService.getApiUser().getUserId())
                .setCreatedAt(Timestamp.from(Instant.now()));
        entitiesToSave.add(entity);
    }
    return StreamSupport.stream(saveSafe(entitiesToSave).spliterator(), false).toList();
}
```

For update flow, use `i18nService.updateI18nFieldForEntity(...)` — it accepts the envelope's `I18nEntity` and resolves the FK id on the existing entity, recording any change via `ChangesHelper`.

### 5.4 Entity stays clean

```java
@Entity
public class NotificationSchemaEntity {
    @Column(name = "name_i18n_id")
    private UUID nameI18nId;

    // no @Transient I18nEntity nameI18n — the envelope carries it
}
```

---

## 6. When to use this pattern

Required when the create/update flow needs to resolve **any** data that doesn't map directly to a single column:

- `I18nEntity` + `I18nTranslationEntity` graphs → resolve to `xxx_i18n_id`
- `ResourceEntity` (uploaded binary) → resolve to `resource_id`
- Sub-entity graphs that the service must persist separately (e.g. options of a data list)

Not needed when:

- The DTO maps 1:1 to entity columns
- No auxiliary persistence is required (e.g. simple lookup updates)

---

## 7. Naming Convention

| Class                       | Purpose                                              |
| --------------------------- | ---------------------------------------------------- |
| `XxxSave`                   | Base envelope. Fields are `public` for chaining.     |
| `XxxCreate extends XxxSave` | Create flow. Empty marker or create-only fields.     |
| `XxxUpdate extends XxxSave` | Update flow. Adds `UUID id`.                         |
| `XxxSaveRestDTOReverseMapper` | Base reverse mapper, used by Create + Update variants. |

Place envelopes in `org.twins.core.domain.<module>`, mappers in `org.twins.core.mappers.rest.<module>`.

---

## 8. Anti-Patterns

- Declaring `@Transient I18nEntity xxxI18n` on an entity to carry request data
- Using the entity class as a DTO in controllers (entity exposed directly in `@RequestBody`)
- Persisting `I18nEntity` from the mapper (the service must own the `I18nService` call — `I18nType` is a service-layer concern)

---

## 9. Final Rule

| Scenario                                            | Solution                                                  |
| --------------------------------------------------- | --------------------------------------------------------- |
| Create/update flow with auxiliary data (i18n, etc.) | Domain `XxxSave` envelope + reverse mapper + service     |
| Simple 1:1 column mapping                           | Mapper writes directly to entity                          |
| Reading for display                                 | `@Transient` runtime fields loaded by service bulk-load (see `entity_code_convention.md`) |
