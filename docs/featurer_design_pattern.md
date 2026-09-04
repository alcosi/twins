# Featurer Design Pattern

How to write and evolve featurers in Twins: the pluggable strategy components behind factories,
conditioners, notificators, field typers, etc. Covers the registration/params machinery from the
cambium framework and the batch-first processing contract used by the notificator family.

## Related documents
* load_method_pattern.md — the `load*` bulk-load methods used by `beforeResolve` / `beforeCollect` preloads
* entity_code_convention.md

## What a featurer is

A featurer is a **Spring singleton bean that implements one configurable strategy**. The code defines
*what* can be configured; the database stores *which* implementation and *which* params a concrete
business configuration uses. At runtime the caller resolves a featurer id + params from config,
materializes the bean and a validated `Properties` object, and invokes it.

```
DB config (featurer id + params)          Code
────────────────────────────────         ─────────────────────────────────────
notification_recipient_collector   ──►   featurerService.getFeaturer(id, RecipientResolver.class)
  recipient_resolver_featurer_id          featurerService.extractProperties(featurer, params)
  recipient_resolver_params               resolver.resolveBatch(batch, properties)
```

On startup `FeaturerService.syncFeaturers()` (`org.cambium.featurer.FeaturerService`) scans every
`Featurer` bean and syncs its metadata (types, ids, params, deprecation) into the `featurer*` tables,
so new featurers become available to admins without manual SQL.

## Anatomy

Two-level class hierarchy, one level per annotation:

```java
// 1. The TYPE — an abstract base class declaring the contract for a family of strategies
@FeaturerType(id = FeaturerTwins.TYPE_47, name = "Recipient resolver", description = "...")
public abstract class RecipientResolver extends FeaturerTwins {
    public abstract void resolveBatch(RecipientResolveBatch batch, Properties properties) throws ServiceException;
}

// 2. The IMPLEMENTATION — a Spring component with a unique numeric id
@Component
@Featurer(id = FeaturerTwins.ID_4704, name = "Recipient Resolver Twin Base", description = "...")
public class RecipientResolverTwinBase extends RecipientResolverAtomic { ... }
```

### Registration rules

* **Type id** — `FeaturerTwins.TYPE_XX` constant on the abstract base via `@FeaturerType`.
* **Implementation id** — `FeaturerTwins.ID_XXNN` constant via `@Featurer`. Convention: the id starts
  with the type number and ends with a sequence (`TYPE_47` → `ID_4701`, `ID_4702`, …). Never reuse an
  id of a deployed featurer.
* Every new constant goes into `org.twins.core.featurer.FeaturerTwins`.
* To retire a featurer, mark the class `@Deprecated` — the flag is synced to the DB and it disappears
  from new-config pickers.
* To change behavior incompatibly, add a **new** featurer (`FieldProjectorNumericToDataListV1` … `V4`
  pattern) instead of editing a deployed one: existing DB configs keep pointing at the old id.

### Params

Params are declared as `public static final` fields of `FeaturerParam<T>` subclasses, annotated with
`@FeaturerParam`. They are read **only** through `extract(properties)` — never raw
`properties.getProperty(...)`:

```java
@FeaturerParam(name = "resolve history actor user", order = 1, optional = true, defaultValue = "false")
public static final FeaturerParamBoolean resolveActor = new FeaturerParamBoolean("resolveActor");

// usage inside the featurer:
if (resolveActor.extract(properties)) { ... }
```

* Prefer `optional = true` + `defaultValue` so existing configs survive new params.
* Types come from cambium (`FeaturerParamBoolean`, `FeaturerParamString`, `FeaturerParamUUIDSet`, …).
  Domain-restricted subtypes live in `org.twins.core.featurer.params` and carry their own
  `@FeaturerParamType` (e.g. `FeaturerParamUUIDSetUserId` — a UUID set that must reference users).
* Cross-param validation goes into `extraParamsValidation(Properties)`; it runs on config save via
  `featurerService.checkValid(...)`.
* Config problems are reported as `ServiceException(ErrorCodeFeaturer.INCORRECT_CONFIGURATION, ...)`.
* `extractProperties` results are cached per `(featurerId, canonical params)` — see
  `FeaturerService.toConfigKey`.

### Bean rules

* Featurers are **stateless singletons**. No mutable instance fields — per-call state lives in local
  variables or in the batch/context object passed by the caller. This is what makes "the preload map
  is a local variable — thread-safe on the singleton bean" true.
* Inject services with `@Lazy @Autowired` (field) or constructor injection; `@Lazy` avoids circular
  dependency failures between featurers and services.

## Batch processing (preferred)

The project rule is: **no N+1 — bulk operations instead of per-item loops**. A featurer is invoked
for a *group* of work items that share the same `(featurerId, params)` config, so its API is
batch-first: the public contract takes a batch object, and a per-item implementation is derived from
it by a template method — not the other way round.

### The flow on the caller side

The caller (e.g. `HistoryNotificationRecipientService.resolveRecipientsBatch`,
`NotificationContextService.collectHistoryContextBatch`) groups work items by
`(featurerId, canonical params)` using `FeaturerGroup.builder(...)`, then runs **one featurer call
per group** over the union of the group's items:

```java
for (var group : resolverGroups.build()) {
    RecipientResolver resolver = featurerService.getFeaturer(group.getFeaturerId(), RecipientResolver.class);
    RecipientResolveBatch resolveBatch = new RecipientResolveBatch(chunkDomainId);
    for (HistoryEntity history : group.getItems()) {
        resolveBatch.add(history);
    }
    resolver.resolveBatch(resolveBatch, group.getParams());
    resolverCache.put(group.getConfigKey(), resolveBatch);   // reused when reassembling per item
}
```

Afterwards per-item results are reassembled from the cached group batches by plain map lookups —
without re-running any featurer.

### The params entry: `HashMap` in, `Properties` down

The featurer base declares **two overloads** of its operation, and the split is part of the contract:

```java
// RecipientResolver
public void resolveBatch(RecipientResolveBatch batch, HashMap<String, String> recipientParams) throws ServiceException {
    if (batch.isEmpty()) {
        return;
    }
    Properties properties = featurerService.extractProperties(this, recipientParams);
    resolveBatch(batch, properties);
}

public abstract void resolveBatch(RecipientResolveBatch batch, Properties properties) throws ServiceException;
```

* The **`HashMap` overload is the public entry**. It takes the params exactly as stored on the
  config entity (e.g. `HistoryNotificationRecipientCollectorEntity.getRecipientResolverParams()`) —
  this is what the caller invokes with `group.getParams()`. It converts them **once** via
  `featurerService.extractProperties(this, params)`, which validates non-optional params, applies
  defaults and `injection@` values, and is cached per `(featurerId, canonical params)`.
* The **abstract `Properties` overload** — and everything below it: the Atomic template, the
  `before*` hooks, the per-item method — receives the already-materialized `Properties`.
  Implementers never call `extractProperties` themselves and never see the raw map: validation and
  defaults cannot be skipped, duplicated, or accidentally run per item instead of per group.
* The single-item convenience entries in the Atomic classes (`resolve(history, ids, HashMap)` /
  `collectData(history, context, HashMap)`) follow the same rule — extract, then delegate to the
  `Properties` variant.

### The Atomic template

`RecipientResolverAtomic` and `ContextCollectorAtomic` turn the batch contract into an item-level
one via a template method. `resolveBatch` / `collectDataBatch` are `final`; the extension points are
only the hooks:

```java
public abstract class RecipientResolverAtomic extends RecipientResolver {

    @Override
    public final void resolveBatch(RecipientResolveBatch batch, Properties properties) throws ServiceException {
        if (batch.isEmpty()) {
            return;
        }
        Map<HistoryEntity, Set<UUID>> recipientIdsByHistory = batch.getRecipientIdsByHistory();
        beforeResolve(batch);                                  // 1. bulk preload, once per group
        for (var entry : recipientIdsByHistory.entrySet()) {
            resolve(entry.getKey(), entry.getValue(), properties);   // 2. per item, in-memory
        }
    }

    /** Override to bulk-load relations needed by {@link #resolve} across the whole batch. */
    protected void beforeResolve(RecipientResolveBatch batch) throws ServiceException {
    }

    protected abstract void resolve(HistoryEntity history, Set<UUID> recipientIds, Properties properties) throws ServiceException;
}
```

`ContextCollectorAtomic` is symmetric: `beforeCollect(ContextCollectorBatch)` + abstract
`collectData(history, context, properties)`. A single-item convenience wrapper
(`resolve(history, ids, HashMap params)` / `collectData(...)`) is kept for tests and non-batch
callers — it extracts `Properties` and delegates.

Making the batch method `final` is deliberate: subclasses cannot silently reintroduce per-item DB
access by overriding the wrong method, and the "preload once, then loop" shape is guaranteed.

### The batch object

`RecipientResolveBatch` / `ContextCollectorBatch` are both:

* **an accumulator owned by the caller** — `recipientIdsByHistory` / `contextByHistory`; the items to
  process are exactly its keySet, and the featurer writes results into the per-item value;
* **a pre-derived view of the group's data** — `getTwins()`, `getTwinIds()`, `getBusinessAccountIds()`
  (and `getTwinClasses()` for collectors), maintained incrementally by the idempotent `add(history)`,
  plus the chunk-wide `getDomainId()`.

Implementers must read these getters instead of re-collecting the same sets from the histories —
re-deriving them in every featurer re-does the caller's work N times per chunk.

### How to write a batch featurer

**Default: extend the Atomic class.**

1. Implement the abstract per-item method. It must run entirely **in-memory** — no DB access.
2. If the item method needs a relation that is not already loaded, override `beforeResolve` /
   `beforeCollect` and bulk-load it for the whole batch. Two preload styles:
   * **Load onto the entities** with a service `load*` method (`twinService.loadHead(batch.getTwins())`,
     `twinService.loadUser(...)`, `resourceService.loadIconResources(batch.getTwinClasses())`) — see
     [load_method_pattern.md](load_method_pattern.md). Preferred when the relation is naturally a
     `@Transient` field of the entity; afterwards the per-item read is a plain getter.
   * **Local map** returned by one bulk query, used inside the overridden batch method only. Safe
     because it is a local variable on a stateless bean.
3. Use the batch getters (`getTwins()`, `getBusinessAccountIds()`, …) as the preload input.

Real example — `RecipientResolverHeadTwinBase`:

```java
@Override
protected void beforeResolve(RecipientResolveBatch batch) throws ServiceException {
    if (!batch.getTwins().isEmpty()) {
        twinService.loadHead(batch.getTwins());     // one query for the whole group
    }
}

@Override
protected void resolve(HistoryEntity history, Set<UUID> userIds, Properties properties) {
    var headTwin = history.getTwin().getHeadTwin(); // in-memory read
    if (resolveHeadTwinCreator.extract(properties)) {
        SetUtils.safeAdd(userIds, headTwin.getCreatedByUserId());
    }
    ...
}
```

**Exception: override the batch method directly** when the result is not a per-item relation at all
but a *query result* keyed by group-level inputs — i.e. one bulk query can produce answers for every
item at once. Then skip the Atomic base, override `resolveBatch` / `collectDataBatch` on the featurer
base directly, and end with a distribution loop over the accumulator:

```java
// RecipientResolverUserGroups — userIds are fixed by params, only business accounts vary per item
Map<UUID, Set<UUID>> userIdsByBusinessAccount =
        userGroupService.getUsersForGroupsIn(batch.getDomainId(), batch.getBusinessAccountIds(), paramUserGroupIds); // ONE query
for (var entry : batch.getRecipientIdsByHistory().entrySet()) {
    Set<UUID> resolved = userIdsByBusinessAccount.get(entry.getKey().getTwin().getOwnerBusinessAccountId());
    if (CollectionUtils.isNotEmpty(resolved)) {
        entry.getValue().addAll(resolved);
    }
}
```

Same shape: `RecipientResolverSpaceRoles`, `RecipientResolverBusinessAccount`,
`RecipientResolverUsersBase`, and `ContextCollectorTwinClass` (direct override because of i18n, see
below). Each of these documents the reason in its class javadoc — do the same.

**Intermediate abstract bases for item-level variants.** When several implementations share a param
set and differ only in *which item* they read, put the shared code in an abstract mid-level class
with one abstract selector method — subclasses stay one-liners:

```java
// ContextCollectorUser (abstract, extends ContextCollectorAtomic)
protected abstract UserEntity getUser(HistoryEntity history, Properties properties);

// ContextCollectorTwinAssigneeUser
@Override
protected UserEntity getUser(HistoryEntity history, Properties properties) {
    return history.getTwin().getAssignerUser();
}
```

Same pattern: `ContextCollectorTwinBase#resolveTwin`, `RecipientResolverUsersBase` vs the
query-result resolvers above.

### i18n in batch collectors

Never translate in place inside a collector: in batch mode the recipients of one chunk may have
different locales, and the thread-local `ApiUser` locale is the wrong one anyway. Collectors call
`ContextCollectorBatch.addI18n(history, key, i18nId)`, which puts a `#i18n=<uuid>` placeholder into
the context and registers the id; the caller (`NotificationContextService.resolveI18n`)
bulk-translates all registered ids once per distinct recipient locale and materializes the
placeholders at notify-event build time.

### The same pattern elsewhere

`NotifierAtomic` mirrors the template for notifiers: a `final` batch method looping over
`notify(...)` per event, plus per-event error isolation — a failing event is collected and returned
instead of aborting the rest of the batch, so the caller can attribute the failure per task.

When adding a new featurer *family*, follow this shape: batch API on the base, `final` template +
`before*` hook + abstract item method in the `...Atomic` intermediate class, direct batch override
for query-result implementations.

## Checklist for a new featurer

1. Add `TYPE_XX` / `ID_XXNN` constants to `FeaturerTwins` (id = type number + sequence).
2. Abstract base with `@FeaturerType` extending `FeaturerTwins`; concrete impl as `@Component` with
   `@Featurer`.
3. Declare params: `public static final FeaturerParam*` + `@FeaturerParam`, prefer
   `optional + defaultValue`; domain subtypes in `org.twins.core.featurer.params`.
4. Pick the base: `...Atomic` subclass (item-level, `before*` preload if needed) vs direct batch
   override (query-result). Document the choice in the class javadoc.
5. Base-class overload shape: the public entry takes the raw `HashMap<String, String>` params from
   the config entity and calls `featurerService.extractProperties(this, params)` once; the abstract
   method and all hooks (`before*`, per-item) receive the materialized `Properties` — implementers
   never call `extractProperties` or touch the raw map.
6. Stateless bean; per-call state in locals / batch object; `@Lazy` service injection.
7. Config errors → `ServiceException(ErrorCodeFeaturer.INCORRECT_CONFIGURATION, ...)`; cross-param
   checks in `extraParamsValidation`.
8. Unit test next to the existing ones (`core/src/test/java/org/twins/core/unit/featurer/...`) using
   the single-item convenience entries where handy.
9. Nothing else to register — metadata syncs to the DB on startup; admins attach the featurer to
   configs with params.
