# Mapper Code Convention

**Status:** Accepted
**Date:** 2026-11-25

This document defines how REST DTO mappers (`org.twins.core.mappers.rest`, `org.twins.face.mappers.rest`) are written in Twins: class shape, loading of transient entity fields, mode handling, and related-objects (`postpone`) usage.

Related documents: [`entity_code_convention.md`](entity_code_convention.md) (entities, SpecOnly/runtime field split), [`load_method_pattern.md`](load_method_pattern.md) (service-side bulk loading), [`dto_code_convention.md`](dto_code_convention.md) (DTO naming).

---

## 1. Core Principle

> A mapper **orchestrates** conversion. It never **loads** data itself.

All data access goes through services, exposed as **one-line calls**. If the call you need does not exist, add a load method to the service that owns the entity holding the `@Transient` field — do not inline loading machinery in the mapper.

---

## 2. Class Shape

```java
@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FacePG001Modes.FacePG001WidgetCollectionMode.class})
public class FacePG001RestDTOMapper extends RestSimpleDTOMapper<FacePG001Entity, FacePG001DTOv1> {

    protected final FaceRestDTOMapper faceRestDTOMapper;                 // nested mapper
    protected final FacePG001Service facePG001Service;                   // service owning the SRC entity
    protected final FacePG001WidgetService facePG001WidgetService;       // service owning child entities

    @MapperModePointerBinding(modes = FacePG001Modes.FacePG001Widget2FaceMode.class)
    protected final FacePG001WidgetRestDTOMapper facePG001WidgetRestDTOMapper;

    @Override
    public void map(FacePG001Entity src, FacePG001DTOv1 dst, MapperContext mapperContext) throws Exception { ... }

    @Override
    public void beforeCollectionConversion(Collection<FacePG001Entity> srcCollection, MapperContext mapperContext) throws Exception { ... }
}
```

- `@Component` + `@RequiredArgsConstructor` — mappers are Spring beans, dependencies via `final` fields.
- Extends `RestSimpleDTOMapper<SRC, DST>` — never hand-rolled converter classes.
- `@MapperModeBinding` — for collection/property modes this mapper reacts to (`hasModeButNot`).
- `@MapperModePointerBinding` — for modes of **postponed** relations (related objects); placed on the nested mapper field used for the postpone.
- Inject the service that **owns the source entity** (and services owning its child collections). Never inject repositories.

---

## 3. Loading `@Transient` Fields — the One-Line Rule

Entities carry runtime relations as `@Transient` fields, bulk-loaded by services (see `entity_code_convention.md` §2). In a mapper every such load is a **single call to the parent entity's own service**:

```java
facePG001Service.loadFace(src);                    // loads src.face
faceTC001Service.loadIconResources(srcCollection); // loads iconResource of every element
faceService.loadFace(pointedFace);                 // loads pointedFace.config.face
faceService.loadTwin(pointedFace);                 // loads pointedFace.targetTwin
```

### 3.1 Forbidden — inline load machinery

Never write the load delegation itself in a mapper:

```java
// FORBIDDEN in mappers:
faceService.load(Collections.singletonList(src.getConfig()),
        FacePointedEntity::getFaceId,
        FacePointedEntity::getFace,
        FacePointedEntity::setFace);
```

If no suitable one-line method exists, add it to the **service that owns the entity holding the `@Transient` field** (see `load_method_pattern.md` — Variant A delegates to `EntitySecureFindServiceImpl.load`, exactly like the snippet above, but *inside the service*). For generic wrappers (`PointedFace<E>`) the method lives next to its peers — e.g. `FaceService.loadFace(PointedFace)` beside `FaceService.loadTwin(PointedFace)`.

### 3.2 Load placement checklist

| Who holds the `@Transient` field | Who exposes the load method | Mapper calls |
| -------------------------------- | --------------------------- | ------------ |
| The mapped entity itself (`src.face`) | Its own service (`FacePG001Service`) | `facePG001Service.loadFace(src)` |
| A child collection element (`FaceBC001ItemEntity.iconResource`) | The parent's service (`FaceBC001Service`) | `faceBC001Service.loadIconResources(items)` |
| A generic wrapper (`PointedFace.config.face`, `PointedFace.targetTwin`) | The wrapper's service (`FaceService`) | `faceService.loadFace(src)` |

### 3.3 Null-FK safety

The base `EntitySecureFindServiceImpl.load` skips elements whose FK id is null and whose field is already set — so load calls are unconditional; do not wrap them in `if (src.getXxxId() != null)`.

---

## 4. `map()` vs `beforeCollectionConversion()` — Load in BOTH

`RestSimpleDTOMapper` has two conversion paths:

- `convert(src)` — **single** object: calls only `map()`. `beforeCollectionConversion()` is **NOT** invoked.
- `convertCollection(srcList)` — **bulk**: calls `beforeCollectionConversion()` once, then `map()` per element.

Therefore every load must happen **twice** — singleton form in `map()`, bulk form in `beforeCollectionConversion()`:

```java
@Override
public void map(FacePG001Entity src, FacePG001DTOv1 dst, MapperContext mapperContext) throws Exception {
    facePG001Service.loadFace(src);                       // single path
    faceRestDTOMapper.map(src.getFace(), dst, mapperContext);
    ...
}

@Override
public void beforeCollectionConversion(Collection<FacePG001Entity> srcCollection, MapperContext mapperContext) throws Exception {
    super.beforeCollectionConversion(srcCollection, mapperContext);   // always first
    facePG001Service.loadFace(srcCollection);                         // bulk path
    if (mapperContext.hasModeButNot(FacePG001Modes.FacePG001WidgetCollectionMode.HIDE)) {
        facePG001WidgetService.loadWidgets(srcCollection);
    }
}
```

Rules:

- Always call `super.beforeCollectionConversion(...)` **first**.
- In `beforeCollectionConversion` prefer **bulk** loads over per-element loops — one service call per relation kind for the whole collection.
- Mode-gated loads must use the same condition in both paths.
- The service `load(...)` skips already-loaded fields, so the per-element call in `map()` is a no-op on the bulk path — keep it anyway for the single path.

### 4.1 Delegating to nested mappers

If `map()` delegates to another mapper's `map()` (e.g. a widget mapper delegating to the generic `FaceWidgetRestDTOMapper`), the bulk path must delegate likewise:

```java
@Override
public void beforeCollectionConversion(Collection<PointedFace<FaceWT001Entity>> srcCollection, MapperContext mapperContext) throws Exception {
    super.beforeCollectionConversion(srcCollection, mapperContext);
    faceWidgetRestDTOMapper.beforeCollectionConversion(srcCollection, mapperContext); // faces + twins in bulk
    ...
}
```

---

## 5. Mode Handling

Two complementary mechanisms:

```java
switch (mapperContext.getModeOrUse(FaceMode.SHORT)) {   // primary detail switch
    case SHORT -> dst.setTitle(I18nCacheHolder.addId(src.getTitleI18nId()));
    case DETAILED -> dst
            .setStyleClasses(StringUtils.splitToSet(src.getStyleClasses(), " "))
            .setTitle(I18nCacheHolder.addId(src.getTitleI18nId()));
}

if (mapperContext.hasModeButNot(FacePG001Modes.FacePG001WidgetCollectionMode.HIDE)) {  // optional parts
    facePG001WidgetService.loadWidgets(src);
    dst.setWidgets(facePG001WidgetRestDTOMapper.convertCollection(facePG001WidgetService.filterVariants(src.getWidgets()), mapperContext));
}
```

- `getModeOrUse(Default)` — exactly one mode active; drives the SHORT/DETAILED switch.
- `hasModeButNot(X.HIDE)` — optional relation/collection; gates both the load and the DTO assignment.
- Never check modes with string comparisons or raw `contains`.

---

## 6. Related Objects — `postpone()`

Related entities are **not** mapped inline. They are postponed into `MapperContext` related objects with a pointed fork:

```java
if (mapperContext.hasModeButNot(FaceMode.ModalFace2FaceMode.HIDE)) {
    faceWT001Service.loadModalFaces(config);              // load BEFORE postpone
    faceRestDTOMapper.postpone(config.getModalFace(), mapperContext.forkOnPoint(FaceMode.ModalFace2FaceMode.SHORT));
    dst.setModalFaceId(config.getModalFaceId());          // DTO carries the raw FK id
}
```

Rules:

- **Load before postpone** — the postponed object must be fully loaded (its own `@Transient` fields are resolved later by its own mapper, but the reference itself must exist).
- Always `forkOnPoint(SomeMode.SHORT)` — the related object is converted later with its own mode fork; never pass the current `mapperContext` directly.
- The DTO gets the **raw FK id** (`setModalFaceId`), never the nested DTO inline.
- Postponed entities MUST implement `Identifiable<ID>` — `MapperContext.addRelatedObject` silently drops non-`Identifiable` objects ("can not be stored in mapperContext"). See `entity_code_convention.md` §15.

---

## 7. Child Collections

The kit is loaded by the parent's service, filtered, and converted in one flow:

```java
facePG001WidgetService.loadWidgets(src);
dst.setWidgets(facePG001WidgetRestDTOMapper.convertCollection(facePG001WidgetService.filterVariants(src.getWidgets()), mapperContext));
```

- `loadXxx(src)` — kit load (Variant D of `load_method_pattern.md`).
- `filterVariants(...)` — strips variants not applicable to the current context (optionally with a sort key extractor).
- `convertCollection(...)` — never loop `convert(...)` manually.

---

## 8. Common Value Mappings

```java
// i18n: DTO carries the raw i18n id; translation is resolved by the cache holder
.setTitle(I18nCacheHolder.addId(src.getTitleI18nId()))

// with entity fallback (label overrides the field's own name i18n)
.setLabel(I18nCacheHolder.addId(src.getLabelI18nId() != null ? src.getLabelI18nId() : src.getTwinClassField().getNameI18nId()))

// resources: URI from the loaded ResourceEntity (load via service first)
.setIcon(resourceService.getResourceUri(src.getIconResource()))

// style classes: space-separated string -> Set<String>
.setStyleClasses(StringUtils.splitToSet(src.getStyleClasses(), " "))
```

Entities never carry `@ManyToOne` i18n relations (`entity_code_convention.md` §6.5) — mappers always work with the raw `xxxI18nId`. If a fallback dereferences a transient relation (`src.getTwinClassField().getNameI18nId()`), the relation must be loaded unconditionally in both conversion paths.

---

## 9. What a Mapper Must NOT Do

- Access repositories or build queries.
- Contain business logic, permission checks, or validation (service layer).
- Touch `*SpecOnly` relation fields (`entity_code_convention.md` §9.2).
- Traverse a `@Transient` relation that was not loaded on this path (NPE / lazy-load risk).
- Inline load machinery — see §3.1.
- Map related entities inline into the DTO — use `postpone()` (§6).
- Loop `convert(...)` for collections — use `convertCollection(...)` (§7).
- Load anything in controllers — loads happen here, in the mapper layer (controller stays thin).

---

## 10. Checklist for a New Mapper

1. `@Component @RequiredArgsConstructor`, extends `RestSimpleDTOMapper<SRC, DST>`.
2. Mode annotations: `@MapperModeBinding` on the class, `@MapperModePointerBinding` on nested mapper fields used for postpone.
3. Every `@Transient` field you dereference is loaded via a **one-line** service call — in `map()` **and** in `beforeCollectionConversion()`.
4. `super.beforeCollectionConversion(...)` is the first statement; nested mappers' `beforeCollectionConversion` delegated.
5. Optional parts gated by `hasModeButNot(...)` identically in both paths.
6. Related entities postponed with `forkOnPoint(...)`; DTO gets the raw FK id; postponed entity implements `Identifiable`.
7. Child collections: `loadXxx` → `filterVariants` → `convertCollection`.
8. i18n via `I18nCacheHolder.addId(...)`; resources via `resourceService.getResourceUri(...)`.
9. No repositories, no SpecOnly access, no business logic.
