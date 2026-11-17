# Configured Search — Architecture

## What it is

A **configured search** is a named, domain-owned, DB-stored search configuration for a given entity type. Instead of sending a full search payload, a caller sends only `searchId` (+ optional runtime params and a "narrow" filter). The server loads the configured search, expands it into a regular domain search object, intersects it with the caller's narrow filter, and runs the standard search pipeline.

Example (twin class field):

```
POST /private/twin_class_fields/search/{searchId}/v1
{
  "params": { "currentTwinClassId": "..." },
  "narrow": { "keyLikeList": ["%name%"] }
}
```

The configured search itself is **not** a stored JSON of search criteria. It is a list of rows in a `_search_predicate` table, where each row points to a **Finder featurer** + its params (hstore). Executing a configured search = running each finder featurer to accumulate criteria into a domain search object. This makes configured searches programmable: a finder can resolve runtime named params (`currentTwinClassId`, `currentTwinId`), look up related data, and then fill the search object.

## Existing implementations

| Entity | Search table | Predicate table | Finder featurer | Sorter featurer | Extras |
|---|---|---|---|---|---|
| twin | `twin_search` | `twin_search_predicate` | TwinFinder (TYPE_27) | `twin_search_sort` table | alias + SearchDetector, permission_id, head_twin_search_id, TwinSearchExecutor (TYPE_57) |
| twin_class | `twin_class_search` | `twin_class_search_predicate` | ClassFinder (TYPE_39) | ClassSorter (TYPE_58) + force_sorting | |
| twin_class_field | `twin_class_field_search` | `twin_class_field_search_predicate` | FieldFinder (TYPE_32) | FieldSorter (TYPE_40) + force_sorting | context params |
| user | `user_search` | `user_search_predicate` | UserFinder (TYPE_43) | UserSorter (TYPE_42) + force_sorting | |
| data_list_option | `data_list_option_search` | `data_list_option_search_predicate` | DataListOptionFinder (TYPE_51) | DataListOptionSorter (TYPE_52) + force_sorting | |
| twin_comment | `twin_comment_search` | `twin_comment_search_predicate` | CommentFinder (TYPE_59) | CommentSorter (TYPE_60) + force_sorting | |

Each table set seeds one system row — the **UNLIMITED** configured search (`domain_id = null`, ids `00000000-0000-0000-0014-000000000001..006`, see `SystemIds`). `UNLIMITED` means "no configured search": the service skips predicate loading and runs the narrow filter directly.

## Data model

### `<entity>_search` (the configured search header)

```sql
create table twin_class_field_search (
    id         uuid primary key,
    domain_id  uuid references domain (id) on delete cascade,  -- null = system-wide
    name       varchar not null,
    -- sorter block:
    force_sorting            boolean not null default false,
    field_sorter_featurer_id integer not null references featurer (id),
    field_sorter_params      hstore
);
```

`twin_search` deviates on purpose (see "Accepted differences"): no `domain_id`, but `permission_id`, `twin_search_alias_id`, `head_twin_search_id`, `twin_search_executor_featurer_id` + params, and a separate `twin_search_sort` table instead of the sorter block.

### `<entity>_search_predicate` (one finder call per row)

```sql
create table twin_class_field_search_predicate (
    id                       uuid primary key,
    twin_class_field_search_id uuid references twin_class_field_search (id) on delete cascade,
    field_finder_featurer_id integer not null references featurer (id) on delete restrict,
    field_finder_params      hstore default ''::hstore
);
```

Predicate rows are always loaded in bulk by `findBy<Search>Id(...)` and executed in a loop (all predicates AND-accumulate into one search object).

### JPA entities

- Header entity (`TwinClassFieldSearchEntity`): flat `@Id` + columns, hstore via `@Type(PostgreSQLHStoreType.class)` → `HashMap<String,String>`. Implements `ConfiguredSearch` (`getSorterFeaturerId`/`getSorterParams` delegate to the entity-specific sorter columns).
- Predicate entity: same + a lazy `@ManyToOne` back-reference to the header marked `@Deprecated // for specification only` (SpecOnly pattern) plus a `@Transient` header field loaded on demand. Implements `ConfiguredSearchPredicate` (`getFinderFeaturerId`/`getFinderParams` delegates).
- Domain search object (`TwinClassFieldSearch`): implements `ConfiguredSearchHolder<<X>SearchEntity>` — Lombok chained accessors satisfy it; the configured search header travels inside the search object as `configuredSearch`, and the `emptySearch` flag lets a finder short-circuit the flow to an empty page.

## Featurer contracts

All configured-search finders and sorters extend two generic base classes (`org.twins.core.featurer.search`):

```java
public abstract class SearchFinder<S> extends FeaturerTwins {
    public void concatSearch(HashMap<String, String> finderParams, S search, Map<String,String> namedParamsMap)       // extracts params, delegates
    protected abstract void concatSearch(Properties properties, S search, Map<String, String> namedParamsMap);      // implement this
}

public abstract class SearchSorter<E> extends FeaturerTwins {
    public Function<Specification<E>, Specification<E>> createSort(HashMap<String, String> sorterParams)              // extracts params, delegates
    public abstract Function<Specification<E>, Specification<E>> createSort(Properties properties);                   // implement this
}
```

Concrete finder/sorter featurer types are thin subclasses pinning the generic parameter and the `@FeaturerType` annotation (e.g. `FieldFinder extends SearchFinder<TwinClassFieldSearch>`, `FieldSorter extends SearchSorter<TwinClassFieldEntity>`). A new configured search only adds two such subclasses — no boilerplate.

`namedParamsMap` carries runtime context from the caller (e.g. `FieldFinder.PARAM_CURRENT_TWIN_CLASS_ID`, `PARAM_CURRENT_TWIN_ID` — faces pass them when rendering).

The sorter is applied when `force_sorting = true` **or** the client did not request a sort. The sort function wraps the filter specification; `pagination.setSort(null)` is then cleared so the two sort sources do not conflict.

If the sorter does not apply (no sorter configured, it returned `null`, and the client did not request a sort), the configured pipeline falls back to the **entity default sort** — the base service's `createDefaultSortSpecification` builds it from the free-search service's `defaultSortField()` (ASC); a `null` default sort field keeps no ORDER BY. This keeps offset pagination deterministic. The sort precedence is: **client sort > configured sorter > entity default**, uniform with the free pipeline.

Not covered by the base classes: `TwinSorter` (TYPE_41) sorts twins by a *field value* (`createSort(params, twinClassFieldEntity, direction)`) — a different contract, not a configured-search sorter.

## Execution flow

The flow is owned by the **config service** (`<X>SearchConfigService`), so the plain `<X>SearchService` stays free-search only and new configured searches never touch it. All five non-twin implementations extend the common base `org.twins.core.service.ConfiguredSearchService<S, P, R, E>`, which implements the flow once as a template method `findConfigured(searchId, namedParamsMap, narrowSearch, pagination)` over small per-entity hooks (twin is the only exception — the same steps live in `TwinSearchService`, see "Accepted differences"):

1. **UNLIMITED short-circuit** — `search(narrowSearch, pagination)` directly.
2. **Load header** via `findEntitySafe(searchId)` on the config service itself — a plain `EntitySecureFindServiceImpl` doing the domain check (`checkDomainAccessDenied(entity.getDomainId(), ...)`; `domain_id = null` passes for everyone).
3. **Load predicates** in one query (`findBy<Search>Id`).
4. **Accumulate**: `mainSearch = entitySearchService().newSearch()`; for each predicate resolve the finder featurer and call `concatSearch(params, mainSearch, namedParamsMap)`.
5. **Narrow**: `narrowSearch(mainSearch, narrowSearch)` in the base — intersects the configured search with the client filter (see below).
6. **Remember the header**: `mainSearch.setConfiguredSearch(searchEntity)` — the header travels inside the domain search so the sort step can reach it later.
7. **Empty-search short-circuit**: if a finder marked `mainSearch.setEmptySearch(true)` (it detected an impossible condition, e.g. `FieldFinderStub`), return an empty page without hitting the DB.
8. **Sort**: if `forceSorting` or client sort is absent → run the sorter featurer (for twin: `twin_search_sort` rows or TwinSearchExecutor).
9. Run the default execution: filter specification + sort wrapper + page fetch via the entity repository (base `runConfiguredSearch`; for twin: TwinSearchExecutor pipeline).

Twin adds one level of indirection: the header selects a `TwinSearchExecutor` featurer (default ID_5701) that performs steps 4–8, enabling alternative execution strategies (e.g. id-only queries, count-only, recursion-guarded subsearches).

## Narrow semantics

Narrowing is a **restriction, never an extension** — the client can only shrink the configured search's result set. The base `ConfiguredSearchService.narrowSearch(mainSearch, narrowSearch)` (null-safe) narrows every field annotated with **`@SearchFilter`** — fields are scanned once per search class (walk the class hierarchy) and cached in a static map inside the base service:

- `Set` fields: `SetUtils.narrowSet` — intersect (empty main = take narrow, i.e. main was "no restriction").
- `Ternary` fields: `TernaryUtils.narrow`.
- `Map<UUID,Boolean>` fields: `MapUtils.narrowMapOfBooleans(..., Boolean.TRUE)`.
- Anything the scan cannot express: override `narrowSearchSpecific(mainSearch, narrowSearch)` in the config service (e.g. `UserSearchConfigService` narrows `childTwinSearches` there).
- twin also narrows `Map<UUID,Set<UUID>>` link filters and can override sorts.

Fields of unsupported types (`IntegerRange`, `LongRange`, `DataTimeRange`, hierarchy objects) are silently skipped by the scan — the annotation still marks them as filter fields, but narrowing is not implemented for them yet (e.g. `TwinClassSearch.twinCounterRange`). Since every filter field is annotated, "forgot to list the field" bugs (e.g. a `freezeIdList` that silently was not narrowed) are impossible by construction.

## REST API pattern

Per entity, in the existing search controller:

```
POST /private/<entity>/search/v1|v2            — free search (full DTO body)
POST /private/<entity>/search/{searchId}/v1    — configured search
```

Configured-search request DTO (`TwinClassFieldSearchConfiguredRqDTOv1` style):

```java
public class XSearchConfiguredRqDTOv1 extends Request {
    public Map<String, String> params;   // named params for finders
    public XSearchDTOv1 narrow;          // client-side narrowing (same DTO as free search)
}
```

Response is identical to free search (paged entity list + relatedObjects). Controllers are `@ProtectedBy` entity permissions; configured-search read access is domain-checked in the config service.

## Consumers

- **Faces** store a FK to a configured search: `face_tc001_option.twin_class_field_search_id`, `face_tc001_option.twin_class_search_id`, `face_tw004.twin_class_field_search_id`, `face_tw007.twin_class_search_id`, `face_wt001.twin_search_id`. Face services call `findConfigured(searchId, contextParams, null, FRIENDLY)` directly (e.g. `FaceTW004Service`).
- **twin_search aliases** (`twin_search_alias` + SearchDetector featurer) let a client reference a configured search by a stable alias and resolve 1..N searches at runtime.
- Permission system: `twin_search.permission_id` gates a configured search itself.

## Lifecycle / CRUD

There is **no CRUD API** for configured searches (planned separately). They are created and maintained by Flyway migrations (including data-migrations that convert legacy per-row finder configs into configured searches, e.g. `V1.3.364.01` for face_tw004). `*SearchConfigService` classes never write to the config tables — they only resolve the header (`findEntitySafe`) and execute the configured search.

## Accepted differences (twin is the heavyweight variant)

1. `twin_search` has no `domain_id`; access is gated by `permission_id` instead. Adding `domain_id` would need a non-trivial backfill — candidate for a separate task.
2. twin sorting lives in a separate `twin_search_sort` table (multi-field sort by twin class fields); the embedded sorter block used by other configured searches was dropped from `twin_search` in `V1.3.431.01`.
3. twin configured searches support aliases, a head-search composition, and a pluggable executor.
4. user: the execution path stays custom (`findUsers` weaves childTwinSearches subqueries, so `runConfiguredSearch` is overridden and the base default-sort fallback does not fire there). `UserSearchService` still extends `EntitySearchService` fully — enum-driven sorting (`UserSortField`: name/email/status, default `name`) and group counting by status (`UserGroupField`) are implemented and available.

## Unified in the 2026-11 consistency pass

- All finders accept `namedParamsMap` and share the `SearchFinder<S>` base class (ClassFinder gained named params; TwinFinder now takes `HashMap<String,String>` params instead of the whole predicate entity, and its `concat` was renamed to `concatSearch`).
- All sorters share the `SearchSorter<E>` base class.
- All non-twin configured searches have the sorter block (`force_sorting` + `<entity>_sorter_featurer_id` + `<entity>_sorter_params`); `twin_class_search` got `ClassSorter` (TYPE_58).
- `force_sorting` is the `Boolean` wrapper everywhere (entity convention).
- All search/predicate entities carry `@FieldNameConstants`.
- Domain search objects carry the configured search header as `configuredSearch`.

## Recipe: add configured search for a new entity

Checklist per entity `<X>` (existing free-search service `<X>SearchService extends EntitySearchService` is a prerequisite):

1. **Migration** (`db/migration_backlog/`):
   - `create table <x>_search (id, domain_id, name, force_sorting, <x>_sorter_featurer_id, <x>_sorter_params)`;
   - `create table <x>_search_predicate (id, <x>_search_id → cascade, <x>_finder_featurer_id → restrict, <x>_finder_params hstore)`;
   - indexes on `<x>_search_id` and both featurer columns (and `domain_id`);
   - insert featurer_type rows (`<X>Finder`, `<X>Sorter`) and stub featurers;
   - insert the UNLIMITED row (`domain_id = null`), register its UUID in `SystemIds`.
2. **DAO**: `<X>SearchEntity` (implements `ConfiguredSearch` — two delegate getters for the sorter columns), `<X>SearchPredicateEntity` (implements `ConfiguredSearchPredicate`; SpecOnly back-reference, `@FieldNameConstants`), two `CrudRepository` interfaces with `findBy<X>SearchId`.
3. **Featurers**: thin subclasses `<X>Finder extends SearchFinder<<X>Search>` and `<X>Sorter extends SearchSorter<<X>Entity>` with the `@FeaturerType` annotation, plus at least one stub implementation each.
4. **Domain search**: add `<X>SearchEntity configuredSearch` and `boolean emptySearch` fields to `<X>Search`, `implements ConfiguredSearchHolder<<X>SearchEntity>`, and annotate every narrowable filter field with `@SearchFilter`.
5. **Config service**: `<X>SearchConfigService extends ConfiguredSearchService<<X>SearchEntity, <X>SearchPredicateEntity, <X>Search, <X>Entity>>` — the whole flow comes from the base (`findConfigured` + default `runConfiguredSearch`); implement the hooks: the 4 `EntitySecureFindServiceImpl` overrides (`isEntityReadDenied` = domain check), `unlimitedSearchId()`, `entitySearchService()` (return the `<X>SearchService` — mandatory; the base takes `newSearch()` and the default sort fallback from it), `loadPredicates()`, `finderClass()`, `sorterClass()`, `createFilterSpecification()` (delegate to `<X>SearchService`), `searchExecutor()` (the entity repository), `runFreeSearch()` (delegate to `<X>SearchService.search`). Override `narrowSearchSpecific()` only for narrowing the generic `@SearchFilter` scan cannot express (see `UserSearchConfigService`), and `runConfiguredSearch()` only for a non-standard execution shape. `authService`/`featurerService` are inherited from the base.
6. **Search service**: untouched — no configured-search code there.
7. **DTO + mapper**: `<X>SearchConfiguredRqDTOv1 { params, narrow }`; reuse the existing free-search DTO for `narrow` and its reverse mapper.
8. **Controller**: `POST /private/<x>/search/{searchId}/v1` in the existing search controller, calling `<x>SearchConfigService.findConfigured(searchId, request.getParams(), narrow, pagination)`, mirroring the free-search endpoint's mapper context and response DTO.
9. **Consumers** (optional): add `<x>_search_id` FK columns to faces/permissions/factories that should reference configured searches.

Recommended baseline to copy: **data_list_option** (newest, cleanest) or **twin_class_field**. Copy twin only when you need aliases/executor/permissions — it is deliberately the heavyweight variant.

### Candidate entities (already have a free-search service)

twin_status, twinflow, twinflow_transition, link, permission, permission_schema, space_role, user_group, data_list, factory, attachment, history, scheduler_log, notification, featurer (admin).
