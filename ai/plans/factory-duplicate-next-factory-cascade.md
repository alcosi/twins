# Factory duplicate — `duplicateNextFactoryCascade` + `duplicateAfterCommitFactory` flags

Add two independent request flags to `FactoryDuplicateService` that duplicate a factory **as deep as possible**:
- `duplicateNextFactoryCascade` — recursively clones every `nextFactory` reached via `nextTwinFactoryId` (branch + pipeline) and remaps the FK to the clone.
- `duplicateAfterCommitFactory` — clones the factory reached via `pipeline.afterCommitTwinFactoryId` and remaps the FK to the clone.

## Current behavior

- `FactoryDuplicateService` is a two-phase (collect → commit) cascade engine built on `EntityDuplicateService`.
- Cascades are declared in `FactoryDuplicateService.childCascades()` and gated per-flag on `FactoryDuplicate` (`duplicateBranches`, `duplicateMultipliers`, `duplicatePipelines`, `duplicateErasers`, `duplicateTriggers`, `duplicateConditionSets`).
- FK remapping already works for **condition sets** via `lookupOrCollect` (policy B): `FactoryBranchDuplicateService.createNewEntity:95`, `FactoryPipelineDuplicateService.createNewEntity:98`.
- **next-factory references are NOT remapped** — they are copied verbatim, so the clone's branch/pipeline points at the **original** next factory:
  - `FactoryBranchDuplicateService.createNewEntity:103` → `.setNextTwinFactoryId(src.getNextTwinFactoryId())`
  - `FactoryPipelineDuplicateService.createNewEntity:107` → `.setNextTwinFactoryId(src.getNextTwinFactoryId())`
  - `FactoryPipelineDuplicateService.createNewEntity:109` → `.setAfterCommitTwinFactoryId(src.getAfterCommitTwinFactoryId())`

### Where `nextFactory`/`afterCommitFactory` is referenced (all top-level `TwinFactoryEntity`)
| Entity | Column | Field | Remapped by flag |
|---|---|---|---|
| `TwinFactoryBranchEntity` | `next_twin_factory_id` | `nextTwinFactoryId` | `duplicateNextFactoryCascade` |
| `TwinFactoryPipelineEntity` | `next_twin_factory_id` | `nextTwinFactoryId` | `duplicateNextFactoryCascade` |
| `TwinFactoryPipelineEntity` | `after_commit_twin_factory_id` | `afterCommitTwinFactoryId` | `duplicateAfterCommitFactory` |

`nextFactory` is a `@Transient` entity on the branch entity (loaded via `FactoryBranchService.loadNextFactory`, already exists). Pipeline service has no next/afterCommit loader yet — add one (bulk) or fetch by id.

## Why the engine already makes this safe

- `EntityDuplicateCollector` dedups on `DuplicateKey(class, originalId, newParentId)`. For a top-level factory `newParentId == null` → **one original factory ⇒ one clone, ever**.
- `lookupOrCollect` is idempotent: if the next-factory is already in the collector it returns the existing clone's id without recursing.
- ⇒ Cycles (`A→B→A`) and self-links (`A→A`) terminate naturally. No visited-set needed. Long linear chains recurse via `collect`; a factory chain thousands deep could stack-overflow, but that is unrealistic for factory graphs.

## Approach (recommended)

Reuse the existing `lookupOrCollect` pattern that already remaps condition sets. The flag lives on `FactoryDuplicate` (top-level) and is read **from the collector registry** inside branch/pipeline `createNewEntity` — no new transport needed, because the source `FactoryDuplicate` is already registered there before the branch/pipeline cascade runs.

Cascaded (auto-created) next-factories must themselves be duplicated **deeply**. Achieve that with one small generic hook: `customizeCollectedDuplicate(D, collector)`, invoked inside `lookupOrCollect` right after `createNewDuplicate()`, so `FactoryDuplicateService` can flip all cascade flags on the auto-created factory.

## Changes

### 1. Flag plumbing (DTO → domain → mapper) — two independent flags
- `dto/rest/factory/FactoryDuplicateDTOv1.java` — add
  - `public boolean duplicateNextFactoryCascade = false;` + `@Schema`
  - `public boolean duplicateAfterCommitFactory = false;` + `@Schema`
- `domain/factory/FactoryDuplicate.java` — add
  - `private boolean duplicateNextFactoryCascade = false;`
  - `private boolean duplicateAfterCommitFactory = false;`
- `mappers/rest/factory/FactoryDuplicateRestDTOReverseMapper.java` —
  - `.setDuplicateNextFactoryCascade(src.isDuplicateNextFactoryCascade())`
  - `.setDuplicateAfterCommitFactory(src.isDuplicateAfterCommitFactory())`

### 2. Generic hook for auto-created duplicates
- `service/EntityDuplicateService.java`:
  - add `protected void customizeCollectedDuplicate(D duplicate, EntityDuplicateCollector collector) {}` (default no-op).
  - in `lookupOrCollect`, after `createNewDuplicate()` + the base `setOriginalEntity/setOriginalEntityId/setNewParentEntityId/setNewParentEntity`, call `customizeCollectedDuplicate(duplicate, duplicateCollector)` **before** `register`/`collect`, so flags land before the new factory's own cascade runs.

### 3. `FactoryDuplicateService`
- Override `customizeCollectedDuplicate`: a cascaded factory (reached via either flag) is duplicated fully and keeps cascading — set all flags on: `duplicateBranches / Multipliers / Pipelines / Erasers / Triggers / ConditionSets / NextFactoryCascade / AfterCommitFactory = true`. (A clone of a factory is always a full clone; this is what "as deep as possible" means. Granular control applies only at the user-requested top level.)
- `createNewEntity`:
  - **copy `factoryProcessorFeaturerId` + `factoryProcessorParams`** from `original` (required — clone is non-functional without processor; fixes pre-existing gap for top-level too);
  - when `duplicate.getNewKey() == null` (cascaded factory), generate one: `original.getKey() + "_" + <8-char UUID suffix>` (UUID.randomUUID is fine outside workflow scripts). Dedup vs the in-flight request keys is already handled by `validateKeyUniqueness`; `TwinFactoryRepository` has no `existsByKey`, so a short random suffix is the collision guard (verify no DB unique-constraint on `twin_factory.key` — if there is, also add an `existsByKey` check or rely on the constraint).

### 4. `FactoryBranchDuplicateService` (covers `branch.nextTwinFactoryId`)
- add `@Lazy private final FactoryDuplicateService factoryDuplicateService;`
- `loadRequiredRelations`: also `factoryBranchService.loadNextFactory(originalEntities)` so the next-factory entity is resident (bulk; avoids N+1). Loading is unconditional and cheap.
- `createNewEntity`: read the flag from the registry —
  ```java
  var srcDup = (FactoryDuplicate) duplicateCollector.getEntry(
        new EntityDuplicateCollector.DuplicateKey(TwinFactoryEntity.class, src.getTwinFactoryId(), null));
  boolean cascadeNext = srcDup != null && srcDup.isDuplicateNextFactoryCascade();
  UUID nextId = src.getNextTwinFactoryId();
  if (cascadeNext && nextId != null && src.getNextFactory() != null) {
      nextId = factoryDuplicateService.lookupOrCollect(src.getNextFactory(), null, duplicateCollector);
  }
  ...
  .setNextTwinFactoryId(nextId)
  ```
  (Reading via `getEntry` works because `FactoryDuplicateService.collect` registers the source `FactoryDuplicate` at line 294, **before** `collectDuplicatesTree` at 296 fires the branch cascade.)

### 5. `FactoryPipelineDuplicateService` (covers `pipeline.nextTwinFactoryId` + `afterCommitTwinFactoryId`)
- add `@Lazy private final FactoryDuplicateService factoryDuplicateService;`
- add bulk-load for next + afterCommit factory entities (mirror `FactoryBranchService.loadNextFactory` in `FactoryPipelineService`, or fall back to `factoryService.findEntitySafe(id)` per non-null id — bulk preferred).
- `createNewEntity`: read the source `FactoryDuplicate` from the registry (same `getEntry` pattern as branch) and remap **independently** per flag:
  - `nextTwinFactoryId` → `lookupOrCollect` only if `srcDup.isDuplicateNextFactoryCascade()`;
  - `afterCommitTwinFactoryId` → `lookupOrCollect` only if `srcDup.isDuplicateAfterCommitFactory()`.
  Each FK is remapped by its own flag; the two are orthogonal.

## Resolved decisions

1. **`afterCommitTwinFactoryId`** — covered by a **separate** `duplicateAfterCommitFactory` flag, independent from `duplicateNextFactoryCascade`. The two FKs (`next`, `afterCommit`) are orthogonal and controlled by their own flags.
2. **Processor copy** — YES, copy `factoryProcessorFeaturerId` + `factoryProcessorParams` in `FactoryDuplicateService.createNewEntity` (fixes the pre-existing gap for top-level too, and is required for cascaded next-factories to be functional).
3. **Key uniqueness** — generate `original.getKey() + "_" + <8-char UUID suffix>` for cascaded factories. `TwinFactoryRepository` has no `existsByKey`; verify during implementation whether `twin_factory.key` carries a DB unique constraint and, if so, confirm the suffix avoids it (or add an `existsByKey` check).

## Response shape

`duplicate(Collection)` returns `collector.getNewEntities(TwinFactoryEntity.class)` = **all** new factories (top-level + every cascaded next-factory). The existing controller already maps the whole list to `FactoryListRsDTOv1`, so cascaded factories surface in the response with no controller change.

## Tests

- Single factory, branch → nextFactory (`duplicateNextFactoryCascade=on`): clone's branch points at the clone of nextFactory (ids differ from originals).
- Chain A→B→C: all three cloned, FKs chain through clones.
- Cycle A→B→A and self-link A→A: terminate; each original cloned exactly once; no exceptions.
- Both flags off: behavior unchanged (FKs point at originals, no extra factories created).
- `duplicateNextFactoryCascade=on`, `duplicateAfterCommitFactory=off`: pipeline.next remapped to clone; pipeline.afterCommit still points at the original.
- `duplicateAfterCommitFactory=on`, `duplicateNextFactoryCascade=off`: pipeline.afterCommit remapped to clone; pipeline.next still points at the original.
- Shared next-factory referenced by two branches → cloned once (dedup).
- Pipeline `next` + `afterCommit` both remapped when both flags on.
- Generated key is non-null and unique within the request.
