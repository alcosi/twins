---
name: Audit — featurer test gaps & related prod bugs (comprehensive)
description: Full audit of groups 13-24 from FEATURER_TESTS_PLAN.md against "test from intended logic, not from current code" rule. 7 parallel agents audited ~130 classes across widget, storager, fieldinitializer, scheduler, classfield (filter/sorter/finder/projector), twin (detector/sorter/finder/validator), notificator, and trigger.
type: project
---
# Featurer Tests Audit — Groups 13 → 24 (Full Parallel Audit)

**Scope:** Groups marked `[x]` (written) but not yet `✅` (reviewed) in `FEATURER_TESTS_PLAN.md. 7 agents audited in parallel:

- Group 13 — widget (5 classes)
- Group 14 — storager (8 classes)
- Group 15 — scheduler (22 classes)
- Group 16 — classfield/filter + classfield/sorter (6 classes)
- Group 17 — classfield/finder (11 classes)
- Group 18 — classfield/projector (14 classes)
- Group 19 — twin/detector + twin/sorter (9 classes)
- Group 20 — twin/finder (20 classes)
- Group 21 — twin/validator (22 classes)
- Group 22 — notificator (~26 classes including subdirs)
- Group 23 — trigger (17 classes)
- Group 24 — fieldinitializer (9 classes)

**Total:** ~159 prod classes audited. Tests live at `core/src/test/java/org/twins/core/unit/featurer/` (mirrored package structure, `unit/` is folder-only).

**Audit philosophy:** Tests must encode **intended behavior** (contract, sibling-class semantics, side effects implied by method name), not just what current implementation happens to do. Anchor example: `IdentityProviderStub` built token `BA,user` but parsed `user,BA` — test mirrored buggy behavior, passed, hid bug.

---

## Executive Summary

| Category | Count | Severity |
|----------|-------|----------|
| **Production bugs** (real defects) | ~45 | HIGH |
| **Test gaps** (tests pass but don't enforce intent) | ~120+ | MEDIUM |
| **Missing tests** (no `*Test.java` at all) | ~10 | LOW-MEDIUM |

**Most severe production bugs:**
- `FieldFilterInStatus` / `FieldFilterNotInStatus` — swapped semantics (class name opposite to implementation)
- `StoragerAlcosiFileHandler.prepareObjectLink` — overwrites `result` in loop, only last replacement applies (data loss)
- All 14 `FieldProjector*` classes — no-op stubs returning null despite names advertising rich conversion logic
- `TwinValidatorTwinChildrenFieldSumInStatusesPositive` — null-id twins silently overwrite each other in result map (data loss)
- `SchedulerTaskRunner.processTask` — exceptions during `execute()` leave tasks stuck in `IN_PROGRESS` forever
- `TwinTriggerDuplicateTwin.run` — logs that `twinEntity` will be cloned but actually clones a different configured twin
- Multiple NPE risks on legitimately-null inputs (validators, storagers, context collectors)

**Most common test gaps:**
- No `verify(repo).save/delete/saveAll/deleteAll` on persistence operations
- No `verify(service).loadXxx(...)` when prod loads lazy collections before iterating
- Tests stub repo/service to return preset values → never exercises real lookup logic
- No multi-element selectivity tests (e.g. some items pass, some fail)
- No empty/null collection coverage
- V2/V3/V4 tests clone V1 test body — would pass even if implementations diverge incorrectly

---

## Group 13 — widget (5 classes)

### Production bugs
- None identified. All four accessors are pure boolean predicates over `Properties` and `TwinClassEntity`.

### Test gaps
- `WidgetAccessorAllowForSpaceTest.isAvailableForClass_allSpaceFlagsNull_returnsFalse` (line 86-96): name claims "Null" but test sets all flags to `false`, identical to `_allSpaceFlagsFalse_returnsFalse`. Doesn't exercise null/primitive-default behavior.
- `WidgetAccessorAllowForKeysTest` / `DenyForKeysTest`: no test for `Properties` missing the `twinClassIdList` key entirely (`extract` returning null → NPE on `.contains()`).
- `DataGrabberTest` (entire file): only asserts `assertInstanceOf` on stub subclass. `DataGrabber` is empty abstract — test exists only to bump coverage.

### Clean
- `WidgetAccessor` (abstract base)
- `WidgetAccessorAllowAny`
- `WidgetAccessorAllowForKeys` / `DenyForKeys` (aside from missing-key gap)
- `WidgetAccessorAllowForSpace`

### Missing tests
- None (all prod classes have corresponding tests)

---

## Group 14 — storager (8 classes)

### Production bugs

**`StoragerAlcosiFileHandler.prepareObjectLink` (line 271-277) — data loss bug**
```java
for (var entry : replaceMap.entrySet()) {
    result = objectLink.replace(entry.getKey(), entry.getValue());
}
```
Overwrites `result` each iteration — only the last `replaceMap` entry applies. Also iterates `Map` (undefined order) → non-deterministic when multiple entries.

**Same bug in `StoragerAlcosiFileHandlerV2.prepareObjectLink` (line 243-249).**

**`StoragerAbstractChecked.addFile(UUID, byte[], HashMap)` (line 92-98) — NPE on null limit**
`if (file.length > fileSizeLimit)` unboxes `Integer fileSizeLimit`, NPE when `fileSizeLimit` returns null. When set to `-1`, `byte[].length > -1` is always true, so the size check effectively rejects every byte array.

**`StoragerAlcosiFileHandler.deleteFile` / `addFileInternal` / `getFileSize` (both V1 & V2) — exception swallowing**
Broad `catch (Throwable)` wraps inner `ServiceException` and re-wraps with generic message, losing specific error codes.

**`Storager.getInputStreamHttpResponse` (line 223-229) — race + timeout bug**
`httpClient` static field initialized with timeout from FIRST call. Subsequent callers get wrong timeout silently. Unsynchronized lazy init → race on first call.

**`StoragerS3Static.generateFileKey` (line 178-179) — inconsistency with local impl**
Does NOT call `addSlashAtTheEndIfNeeded` for `domainId` / `businessAccount` placeholders before substituting (compare to `StoragerLocalStatic`). After replacement and `removeDoubleSlashes`, drops the slash that local impl preserves.

**`StoragerS3Static.addFileInternal` (line 109-112) — NPE on null limit**
`CountedLimitedSizeInputStream(fileStream, fileSizeLimit, 0)` passes boxed Integer — NPE on null limit.

### Test gaps
- `StoragerAlcosiFileHandlerTest` & `V2Test`: no test for `prepareObjectLink` at all — replace-bug unverified.
- `StoragerAlcosiFileHandlerTest.deleteFile_*`: asserts return-value equivalents but never `verify(restTemplate).exchange(...)` with captor on URL/method/body — wouldn't catch swapped args, wrong endpoint.
- `StoragerLocalDynamicTest.getFileControllerUri_*` / `S3StaticTest` / `S3DynamicTest`: assert results like `"//public/static-resource/{id}/v1"` with double leading slash — tests freeze buggy output instead of asserting desired single-slash form.
- `StoragerS3StaticTest`: no test for `addFileInternal` (largest method), no test for `generateFileKey`.
- `StoragerLocalStaticTest`: only covers `deleteFile_nonExistentFile_doesNotThrow` and `getFileAsStream_nonExistentFile_throwsServiceException`. No coverage of `getFileControllerUri`, `generateFileKey`, `addFileInternal`.
- `StoragerExternalUriTest.getFileAsStream_throwsServiceExceptionForInvalidKey`: test passes `"not-a-valid-url"` and expects `ServiceException` — but prod `getInputStreamHttpResponse` will retry 3× with `Thread.sleep` before failing. Test runs slowly and indirectly.
- `StoragerExternalUriTest`: no test for `addExternalUrlFile`/`checkAndAddUriInternal` — entire URI-validation flow unverified.

### Missing tests
- `Storager` (abstract base — includes shared `getInputStreamHttpResponse` retry and `getFileUri` placeholder substitution — neither tested)
- `StoragerAbstractChecked` (abstract — addFile-byte[] limit check, checkFileMimeType, checkAndAddFileInternal, getFileInputStreamWithMetaInfo — none tested)

---

## Group 15 — scheduler (22 classes)

### Production bugs

**`SchedulerCleaner.deleteRecordsAfterInterval` (line 60) — misleading variable name**
Local variable named `createdAfter` is actually the "before" cutoff (`now - interval`) and is passed as `createdBefore` to the DAO. Semantics correct, but naming is a footgun.

**`SchedulerCleaner.processTask` (line 26-49) — exception swallowing inside transaction**
`@Transactional` wraps a `try { ... } catch (Exception e) { return "..." }`. Swallowing exception inside transaction means tx commits (possibly partial) silently. Same pattern in `SchedulerTaskRunner` and `SchedulerConsistencyCheck`.

**`SchedulerTaskRunner.processTask` (line 38-70) — NOT @Transactional + stuck tasks**
Class comment explicitly notes the race between `collectTasks` and `setStatusAndSave`. Two scheduler invocations can re-pick the same `NEED_START` rows. Exceptions inside `execute()` loop are caught and logged, but entity already moved to `IN_PROGRESS` and saved — tasks stuck forever.

**`SchedulerTwinTriggerTaskRunner.collectAll` (line 47) — inconsistency**
Wraps with `List.copyOf(...)` while siblings do not. If repo returns null, NPE here vs. downstream — minor inconsistency.

### Test gaps
- **All runner tests** (`SchedulerAttachmentDeleteTaskRunnerTest`, `SchedulerHistoryNotificationTaskRunnerTest`, `SchedulerDraftCommitTaskRunnerTest`, `SchedulerDraftEraseScopeCollectTaskRunnerTest`, `SchedulerTwinChangeTaskRunnerTest`, `SchedulerTwinTriggerTaskRunnerTest`): **no `processTask` test**. Batching extraction, `setStatusAndSave` chaining, executor.execute, applicationContext.getBean completely uncovered.
- `SchedulerAttachmentDeleteTaskCleanerTest.processTask_withRecordsAndInterval_deletesOlderRecords`: stubs `countAllByCreatedAtBefore` returning 4, but doesn't verify the `Timestamp` argument equals `now - interval`. ArgumentCaptor imported but unused.
- Same timestamp gap in `SchedulerSchedulerLogCleanerTest`, `SchedulerHistoryNotificationTaskCleanerTest`, `SchedulerTwinArchiveCleanerTest`.
- All `Scheduler*CleanerTest.processTask_*`: none assert the exception path — `try/catch (Exception)` returning string is untested.
- `SchedulerConsistencyCheck*Test`: assert only return string contents. They do NOT verify `alertLog.error` is invoked when `size > 0` — tests would pass if alert-log call were removed.
- `SchedulerAttachmentDeleteTaskRunnerTest.SetStatusAndSave.setStatusAndSave_setsInProgressAndSaves`: mutates input list in place but `verify(repo).saveAll(entities)` only verifies reference equality. If `saveAll` moved before setStatus loop in prod, test would still pass.

### Missing tests
- `Scheduler` (base — no test for `getRunnableForScheduling` flow / scheduler-log save branch)
- `SchedulerCleaner` (abstract — `processTask` logic only via subclasses; no exception coverage)
- `SchedulerTaskRunner` (abstract — `processTask` flow never tested)
- `SchedulerConsistencyCheck` (abstract — exception branch + alert-log branch untested)
- `AttachmentDeleteTask`, `DraftCommitTask`, `DraftEraseScopeCollectTask`, `HistoryNotificationTask`, `TwinChangeTask`, `TwinTriggerTask` (all in `tasks/` subdir, no tests)

---

## Group 16 — classfield/filter + classfield/sorter (6 classes)

### Production bugs

**`FieldFilterInStatus.filterFields` (line 30) — swapped semantics**
Name implies "include fields when status IS in set", but emits fields only when status is NOT in set. Logic is inverted vs name.

**`FieldFilterNotInStatus.filterFields` (line 30) — swapped semantics**
Name implies "include when status NOT in set", but emits fields only when status IS in set. Mirror-image swap of `FieldFilterInStatus`.

**`FieldFilterDependOnBooleanField.filterFields` (line 41-43) — silent null-as-false**
When boolean field is absent from kit, code silently treats it as `false`. Spec-wise, missing boolean field should arguably be a distinct case.

**`FieldFilterGiven` (id 3601, name "Filter given field by id") — name/param mismatch**
Parameter is called `filteredFieldIds` but implementation treats those as **excluded** ids (adds the rest). Name suggests "include the given ids".

### Test gaps
- `FieldFilterInStatusTest` / `FieldFilterNotInStatusTest`: tests literally encode the inverted semantics — every assertion mirrors the buggy direction. Will pass even if names/behaviour get fixed to match.
- `FieldFilterDependOnBooleanFieldTest`: no test for "boolean field missing AND `excludeOnTrue=false`".
- `FieldFilterGivenTest.filterFields_emptyFilterSet_keepsAllFields`: passes `""` which yields empty set; never tests `null`/missing param.
- `FieldSorterByOrderedIdsTest.createSort_*` and `SortFunction`: only assert `assertNotNull` — never verifies ordering, never executes Function against real CriteriaBuilder, never confirms CASE WHEN sequence matches input order.
- `FieldSorterByOrderedIdsTest`: no test for the `query.getResultType().equals(Long.class)` branch (count queries).

### Clean
- `FieldSorterStub` (true no-op; test asserts null which is correct intent)

---

## Group 17 — classfield/finder (11 classes)

### Production bugs

**`FieldFinderByClassFromParam.concatSearch` (line 33-35) — mislabeled error**
Non-UUID input throws `ServiceException` with `TWIN_SEARCH_PARAM_MISSED` instead of "not UUID" code — minor mislabeling.

**`FieldFinderByIdExcludeHeadMatched.getMatchCode` (line 63) — hash collision risk**
Concatenates `key + featurerId + params.hashCode()` without separators. Different field combinations can collide (e.g., `key="a1", featurerId=2` vs `key="a", featurerId=12`).

**`FieldFinderByRelevantProjection.concatSearch` (line 50-52) — unbounded result**
Builds `BasicSearch` with only `twinClassIdList` and calls `findTwins` — for systems with many twins per class this potentially loads every twin row. No pagination/limit.

### Test gaps
- `FieldFinderByIdExcludeHeadMatchedTest`: no test covers the `getMatchCode` collision case.
- `FieldFinderByRelevantProjectionTest`: no test covers `relevantProjectionIds` empty path that still invokes `setFieldProjectionSearch` with empty list.
- `FieldFinderByRequiredFalse/True`, `FieldFinderBySystemFalse/True`, `FieldFinderStub`: each has single one-liner assertion of Ternary value/flag set. Doesn't confirm "Required field finder returns required twins".
- `FieldFinderAllTest`: no test for unrecognized property values, no test for both flags simultaneously.

### Clean
- `FieldFinderByIdGiven` and its test (covers include + exclude + appending paths)
- `FieldFinderByClassIdGiven` and its test (covers single/multiple/exclude/extends combinations)

---

## Group 18 — classfield/projector (14 classes)

### Production bugs

**ALL 14 projector classes are no-op stubs**
Every `project(...)` returns `null` regardless of inputs. Their names (`FetchDecimalToStringV1`, `NumericToDataListV1..V4`, `StringToDataListV1..V4`, `StringToNumericV1/V2`, etc.) advertise rich conversion logic that does not exist. This is either the worst-case "name vs. behavior" mismatch in the audit, or all projectors are placeholders awaiting implementation. Either way, every consumer that calls `.project(...)` gets `null` instead of any projected `FieldValue`.

**`FieldProjectorNumericToDataListV2/V3/V4` and `FieldProjectorStringToDataListV2/V3/V4` are byte-for-byte identical to V1** except for `@Featurer(id=...)`/class name. Versioned API surface but zero behavioral divergence.

### Test gaps
- All 14 projector tests only assert `assertNull(result)` — they literally enshrine stub behavior. If anyone implemented real projection logic, every test would fail without indicating behavioral regression.
- V2/V3/V4 tests are exact clones of V1 tests (same body, only class name differs). Will pass even if V2-V4 diverge incorrectly.
- No projector test exercises `field`/`entity` content (null fields, populated fields, empty datalists, source value missing).
- No projector test covers `featurerService.extractProperties` throwing.

### Clean
- None — all 14 prod classes are stubs with mirror-tests

---

## Group 19 — twin/detector + twin/sorter (9 classes)

### Production bugs
- None significant identified. Detectors are simple predicates; sorters delegate to `Comparator.comparing` with field access.

### Test gaps
- Sorter tests sort already-sorted input → can't tell if sort is actually applied.
- Sorter tests use only 2 elements → off-by-one comparator bugs invisible.
- No tests verify `loadXxx` for collections that prod loads before iterating (if any).
- Test asserts result count but not result content/order.

### Clean
- `SearchDetectorConcat`, `SearchDetectorPreferProtected`
- All `TwinSorter*` classes (aside from test gaps above)

### Missing tests
- None (all prod classes have tests)

---

## Group 20 — twin/finder (20 classes)

### Production bugs
- None significant. Most `Given` vs `Requested` finders correctly distinguish properties-source vs authService-source.

### Test gaps
- Finder tests stub repo and only assert `result == stub.value` — tautological.
- Tests for "Given" finders don't verify properties are actually parsed.
- Tests for "Requested" finders don't verify `authService` is consulted.
- `EqualsCurrent` finders: no explicit verify that `authService.getApiUser().getUserId()` is used.
- Tests rely on stubbed mocks returning preset truth — never exercises real lookup logic.

### Clean
- All `TwinFinder*` implementations (aside from test gaps above)

### Missing tests
- None (all prod classes have tests)

---

## Group 21 — twin/validator (22 classes)

### Production bugs

**`TwinValidatorTwinChildrenFieldSumInStatusesPositive.isValid` (line 45) — dead code overload**
Legacy single-twin overload not `@Override`-annotated and not dispatched by base class — dead code that diverges from collection overload (different empty-statuses semantics).

**`TwinValidatorTwinChildrenFieldSumInStatusesPositive.isValid` (line 105-106) — null-key data loss**
Collection overload only filters null ids out of `dstTwinIds` query but still inserts them into result map by `null` key, so multiple null-id twins overwrite each other in `getTwinsResults()` (silent twin loss).

**`TwinValidatorTwinHasBackwardLink.isValid` (line 42-43) — minor perf**
Re-extracts `linkId.extract(properties)` twice inside per-twin loop instead of using already-extracted `linkIdUUID`.

**`TwinValidatorTwinAllChildrenInStatuses.isValid` (line 53) — semantic question**
`count == 0` returns valid even when twin has zero children at all — "all children in status" passes vacuously.

**`TwinValidatorTwinHasChildrenInStatuses.isValid` / `TwinValidatorTwinHasChildrenOfClasses.isValid` — unbounded search**
`BasicSearch` lacks `addHeadTwinId(twinEntityCollection ids)` — globally counts every twin in those statuses then groups by headTwinId. Twins outside collection's children leak through grouping (correctness ok by virtue of grouping, but search loads unbounded result set).

**`TwinValidator.isValid` base (line 50) — invert always false**
Subclass invocation always passes `invert=false`, then base re-applies invert via `buildResult`. Subclasses that compute non-trivial messages branching on `invert` never see `invert=true` through normal flow — inverted-message branches are effectively unreachable in production.

### Test gaps

**Cross-cutting: NO tests verify `loadXxx` calls**
- `TwinValidatorApiUserIsMemberOfGroupTest`: no `verify(userGroupService).loadGroupsForCurrentUser()`
- `TwinValidatorTwinHasLinkTest`: no `verify(twinLinkService).loadTwinLinks(...)`
- `TwinValidatorTwinHasLinkAndDstTwinHasStatusTest`: no `verify(twinLinkService).loadTwinLinks(...)`
- `TwinValidatorTwinMarkerExistTest`: no `verify(twinMarkerService).loadMarkers(...)` — anchor of the audit's "loadXxx missing" pattern
- `TwinValidatorTwinFieldDateLessThenNowTest`: no verify `loadFieldsValues`
- `TwinValidatorTwinFieldBooleanHasValueTest`: no verify `loadFieldsValues`

**Cross-cutting: NO multi-twin selectivity tests**
Almost every validator test uses only a single twin. Tests never assert that some twins pass and some fail correctly.

**Cross-cutting: NO inverted-returnsValid cases**
Most tests have only one inverted direction or none.

**Specific gaps:**
- `TwinValidatorCountOfTwinsSameTwinClassGTEValueTest`: no test with `count == val - 1` boundary just below threshold.
- `TwinValidatorTwinChildrenFieldSumInStatusesPositiveTest.nullTwinId_returnsInvalid`: freezes existing buggy behavior — collection result keyed by `null` is asserted as expected.
- `TwinValidatorTwinChildrenFieldSumInStatusesPositiveTest`: no test for empty `statusIds` (the `isEmpty` branch).
- All tests invoke protected `isValid(Properties, Collection, boolean)` directly, bypassing public caching layer entirely. The cache-key/cache-hit/cache-miss code in `TwinValidator` has zero unit-test coverage.

### Clean
- None — every validator's test has at least one significant gap

### Missing tests
- None (all prod classes have tests)

---

## Group 22 — notificator (~26 classes)

### Production bugs

**Context collectors:**
- `ContextCollectorHistoryComment.collectData` (line 27-28): unchecked `(HistoryContextComment)` cast + chained `comment.getComment().getText()` — ClassCastException/NPE if context wrong type or comment/text null.
- `ContextCollectorHistoryTwinStatus.collectData` (line 61): unchecked `(HistoryContextStatusChange)` cast — ClassCastException risk; no null guards on `getFromStatus()`/`getToStatus()` before `.getName()/.getBackgroundColor()/.getFontColor()`.
- `ContextCollectorHistoryTwinStatus` (line 32): `@FeaturerParam(name = "Collect src status name")` annotation is on the *dst* status key field — copy-paste bug in metadata.
- `ContextCollectorTwinClass.collectData` (line 54): no null check for `twin.getTwinClass()` — NPE if twin has no class.
- `ContextCollectorTwinAssigneeUser.getUser` / `ContextCollectorTwinCreatorUser.getUser` / `ContextCollectorHistoryActorUser.getUser`: return `null` when user not loaded; `ContextCollectorUser.collectData` line 45 will NPE on `user.getId()`. No `loadXxx` call to guarantee population.
- `ContextCollectorHeadTwin.resolveTwin` (line 28-31): if `getHeadTwinId()` is null and `getHeadTwin()` is null, calls `findEntitySafe(null)` — surprising result instead of treating as no-head.
- `ContextCollectorTwinBase.collectData` (line 54): `twin.getOwnerBusinessAccountId().toString()` NPE when business account id is null and `collectBusinessAccount=true`.

**Recipient resolvers:**
- `RecipientResolverTwinBase.resolve` (line 53): `history.getHistoryType().equals(...)` NPE if historyType null; line 54 unchecked `(HistoryContextUserChange)` cast; line 56/62 `historyContext.getFromUser()/.getToUser()` not null-guarded.
- `RecipientResolverSpaceRoles` (line 26): `spaceRoleIds` declared as `FeaturerParamUUIDSetUserId` (user-id subtype) — wrong param type for space role ids.
- `RecipientResolverUserGroups` (line 26): same — `userGroupIds` declared as `FeaturerParamUUIDSetUserId`, wrong param subtype.
- `RecipientResolverHeadTwinBase.resolve` (line 38): no twin id null guard before `findHeadTwin` call.

**Emailer:**
- `EmailerCachedSender.getSender` (line 40-44): known check-then-act race on `senderCache` (already noted).
- `EmailerCachedSender.sendMail` / `Emailer.sendMail`: abstract signature is `(srcEmail, dstEmail, …)` in `Emailer` but `EmailerCachedSender` redeclares as `(dstEmail, srcEmail, …)` and swaps args when delegating. Functional only because subclass impl flips the parameter names back; one missed flip and `from`/`to` silently swap.

**Notifier:**
- `NotifierAlcosiNotificationManager.notify` (line 48): `putFilters(businessAccountKey, context.get(businessAccountKey))` — protobuf `putFilters` rejects null value with NPE if context lacks that key.
- `NotifierAlcosiNotificationManager.getOrCreateStub` (line 84): wraps `Exception` as `RuntimeException` while parent contract is `throws ServiceException` — runtime leakage.

### Test gaps

**Context collector tests:**
- `ContextCollectorTwinAssigneeUserTest.setUp`: only sets `assignerUser` — never sets `createdByUser` or `actorUser` simultaneously. Collector reaching for wrong field would still pass.
- `ContextCollectorTwinCreatorUserTest`: same — only `createdByUser` set.
- `ContextCollectorHistoryActorUserTest`: only sets `history.actorUser`, never sets `twin.assignerUser` or `twin.createdByUser`.
- `ContextCollectorTwinAssigneeUserTest`: no null tests for `name/email/avatar`.
- `ContextCollectorHistoryCommentTest`: no test for `null context` / `wrong-type context` / `null comment` / `null text` — all ClassCast/NPE paths uncovered.
- `ContextCollectorHistoryTwinStatusTest`: no wrong-type / null context / null fromStatus / null toStatus tests.
- `ContextCollectorTwinTest`: no null `ownerBusinessAccountId` case for `collectBusinessAccount=true`.
- `ContextCollectorTwinClassTest`: no test for `twin.getTwinClass()==null`.
- `ContextCollectorHeadTwinAttachmentTest.collectData_headTwinHasAttachment_putsUrl`: pre-sets `twin.setHeadTwin(headTwin)` before calling — never validates that `loadHeadForTwin` actually populated headTwin.
- `ContextCollectorHeadTwinTest`: no test where `headTwinId==null`.

**Recipient resolver tests:**
- `RecipientResolverBusinessAccountTest`: doesn't verify `findUserIdsByBusinessAccountId` called with correct `businessAccountId`; doesn't test selectivity vs domain users.
- `RecipientResolverTwinBaseTest`: no test where `historyType==null`; no test where `getContext()` is wrong type; no test where `fromUser/toUser` is null; no `assigneeChanged` selectivity test.
- `RecipientResolverTwinBaseTest.resolve_assigneeChanged_withOldOnly_addsOldOnly`: asserts size==1 but doesn't assert `creatorUserId/assignerUserId` are NOT in set when flags false.
- `RecipientResolverHeadTwinBaseTest`: no test that asserts `findHeadTwin` is called with correct `twin.getId()`.
- `RecipientResolverUsersBaseTest`: no test that `filterUsersByBusinessAccountAndDomain` is invoked with correct args.
- `RecipientResolverSpaceRolesTest.resolve_addsUsersFromSpaceRoles`: only one role and stubs return; never tests selectivity.
- `RecipientResolverUserGroupsTest`: no test for arg-order swap (`domainId` vs `businessAccountId`).

**Emailer tests:**
- `EmailerCachedSenderTest.GetSender`: no test that `createSender` is called *only once* per mailerId across N concurrent calls — would catch known race.
- `EmailerInternalTest`: no negative test for failed send.

**Notifier tests:**
- `NotifierAlcosiNotificationManagerTest.notify_putsEventCodeInContext`: catches generic `Exception.class` — does not assert it's a `ServiceException`. Hides RuntimeException-leak prod bug.
- `NotifierAlcosiNotificationManagerTest.notify`: no test where context lacks `businessAccountKey` → would expose protobuf `putFilters` NPE.
- `NotifierAlcosiNotificationManagerTest.GetOrCreateStub`: no test that failing `ManagedChannelBuilder.forTarget` doesn't cache entry.

### Clean
- `ContextCollector` (abstract base — only delegation)
- `RecipientResolver` (abstract base — only delegation)
- `Emailer` (abstract base; signature reordering quirk noted but not runtime defect)

### Missing tests
- `ContextCollectorTwinBase` (no direct unit test — only exercised indirectly)
- `Emailer` (no test; abstract — but `sendMail` public/internal arg-flip wiring uncovered)

---

## Group 23 — trigger (17 classes)

### Production bugs

**`TwinTriggerDuplicateTwin.run` (line 39) — wrong target**
Name implies "duplicate the trigger's twin" but extracts a fixed configured `twinId` from properties and clones *that*; ignores `twinEntity`. Log says `twinEntity.logNormal() + " will be cloned"` while a different entity is actually cloned.

**`TwinTriggerChangeStatusByLink.run` (line 50) — unused bind param**
Passes `twinEntity.getHierarchyTree()` to `updateTwinStatusByTwinClassIdAndLinkId`; the SQL filters link's `dst.hierarchy_tree <@ :hierarchyTree`, but `:twinId` bind parameter is unused (dead arg). Trigger updates *all* link sources whose linked dst is anywhere under current twin's subtree.

**`TwinTriggerChangeStatusByLinkForward.run` (line 45) — no class filter**
Hard-codes `twinClassId=null` (no class filter param exposed), so it status-updates **every** dst twin regardless of class. Sibling `ByLinkReverse` is similarly unfiltered.

**`TwinTriggerRabbitMqConnection.connect` (line 40) — connection leak**
Cache evictions (entryCapacity=20, 1h TTL) drop `CachingConnectionFactory` references without `destroy()`/`resetConnection()` → leaks RabbitMQ TCP connections + channels on eviction.

**`TwinTriggerRabbitMqSendI18NFieldValueWithLocale` (line 45) — DI asymmetry**
Mixes constructor injection (`@RequiredArgsConstructor`) with `@Autowired` field injection on `twinClassFieldService` — circular-dep workaround leaking through.

**`TwinTriggerCascadeToDescendants.run` (line 43) — NPE on null depth**
Unlike `CascadeToAncestorsByHead`, does NOT normalize null/negative depth — `depth.extract(properties)` may return null, causing NPE when JPA binds to native int param.

### Test gaps

**Cross-cutting:**
- All trigger tests: `null jobTwinId`, `null srcTwinStatus`, `null dstTwinStatus` are passed as constants; no test verifies they are (or aren't) propagated.
- No trigger test exercises invalid UUID format, missing required key.

**Specific:**
- `TwinTriggerDuplicateTwinTest.run_withFoundTwin_duplicatesTwin`: only verifies `findEntity` called with configured `srcTwinId`. Mirrors current behavior where trigger's `twinEntity` is ignored. Doesn't verify deep-copy semantics.
- `TwinTriggerCascadeToDescendantsTest`: no test for `null` / negative `depth` — current code would NPE.
- `TwinTriggerChangeStatusByLinkTest.run_callsRepositoryWithCorrectParameters`: uses non-`null` `hierarchyTree` but doesn't assert subtree-vs-id semantics.
- `TwinTriggerChangeStatusByTwoLinksTest.run_callsRepositoryWithCorrectParameters`: doesn't exercise both link traversals or assert traversal direction. Swap of `firstLinkId`/`secondLinkId` would silently pass.
- `TwinTriggerChangeStatusByTwoLinksForwardTest`: same — single test, link-id-symmetry not exercised.
- `TwinTriggerChangeStatusByHeadThenLinkTest` / `LinkThenHeadTest`: no test that order of operations matches the name. Both tests just verify repo-method-name dispatch; name/repo cross-swap wouldn't be caught.
- `TwinTriggerChangeStatusByLinkReverseTest`: doesn't differentiate Forward vs Reverse semantics beyond repo method name.
- `TwinTriggerClearAllUsersTouchTest` / `ClearCurrentUserTouchTest`: only verifies correct method called; no test that trigger doesn't accidentally clear the *other* touch.
- `TwinTriggerRabbitMqSendTwinChildrenInStatusesTest.send_withChildren_sendsMessageForEachChild`: doesn't verify `BasicSearch` content (head twin id, status filter, exclude flag, class id).
- `TwinTriggerRabbitMqSendTwinTest.connect_createsNewConnectionFactoryForNewUrl`: asserts cache populated but not idempotency / second-call behavior. Cache state leaks across tests (static `rabbitConnectionCache` shared).
- `TwinTriggerRabbitMqSendI18NFieldValueWithLocaleTest.send_sendsTranslationPayloadWithCorrectFields`: doesn't test inactive locale filtering, doesn't test multiple fields, doesn't test case where `src_locale` is absent from active locales.

### Clean
- `TwinTriggerCascadeToAncestorsByHead` (depth normalization correct)
- `TwinTriggerRabbitMqSendTwin`, `TwinTriggerRabbitMqSendFields` (payload ctor order matches)
- `TwinTriggerClearCurrentUserTouch`, `TwinTriggerClearAllUsersTouch` (simple delegation)

### Missing tests
- `TwinTriggerRabbitMqConnection` (no dedicated test for cache eviction/leak; only indirect coverage via subclasses)

---

## Group 24 — fieldinitializer (9 classes)

### Production bugs

**`FieldInitializer.tryToOverrideValue` (line 62-73) — NPE before ServiceException**
If `value == null`, `valueType.isInstance(value)` returns false → throws `ServiceException` dereferencing `value.getTwinClassField().logNormal()` → NPE before `ServiceException` is constructed. Caller may legitimately produce null.

**`FieldInitializerHead.initValue` (line 32-34) — NPE on missing head**
Calls `twinService.loadHeadForTwin(twin)` then `twinService.loadFieldsValues(twin.getHeadTwin())` — does not check whether `loadHeadForTwin` actually populated `headTwin`. Subsequent `twin.getHeadTwin().getFieldValuesKit()` will NPE.

**`FieldInitializerListDefaultOrNull.initValue` (line 41-56) — NPE on null featurer id**
`featurerService.extractProperties(twinClassField.getFieldTyperFeaturerId(), ...)` — if `getFieldTyperFeaturerId()` is null, unboxing-or-NPE in featurer lookup. No null guard.

### Test gaps
- `FieldInitializerHeadTest.initValue_*`: uses `doAnswer` to manually `twin.setHeadTwin(headTwin)` — bypasses real `loadHeadForTwin` semantics. No `verify(twinService).loadHeadForTwin(twin)` or `loadFieldsValues(headTwin)`.
- `FieldInitializerListTest.initValue_setsSelectedOption`: no `verify(dataListOptionService).findEntitySafe(optionId)`.
- `FieldInitializerListDefaultOrNullTest.initValue_withDefaultOption_setsOption`: stubs `extractProperties` with unrelated properties object; test never asserts what featurerId/params were passed.
- `FieldInitializerBooleanTest`, `TextFixedTest`, `UrlFixedTest`, `DateCurrentTest`, `DateFixedTest`: only call `initValue` directly on each value-setting branch. None exercise `tryToInitializeValue` / `tryToOverrideValue` (public entry points), so override gate logic entirely untested.
- `FieldInitializerNullTest.initValue_callsUndefine`: asserts `value.isUndefined()` after — fine, but `FieldInitializer.tryToInitializeValue` calls `createFieldValue` first; when `createFieldValue` returns null (actual prod path for `FieldInitializerNull`), `initValue` is never called by framework. Test exercises path production never reaches.
- All tests: no `verify` on `featurerService.extractProperties` for any initializer; every test calls `initValue` directly with hand-built `Properties`, sidestepping property extraction.

### Missing tests
- `FieldInitializer` (base abstract — tryToInitializeValue, tryToOverrideValue, valueType/descriptorType reflection in constructor, override gate logic — none tested)
- `FieldInitializerThrowIfNull` (interface only, no logic)

---

## Summary by Category

### Critical production bugs (fix immediately)

| Bug | Location | Impact |
|-----|----------|--------|
| `FieldFilterInStatus` / `FieldFilterNotInStatus` swapped semantics | Both line 30 | Filters include opposite of what names advertise |
| `StoragerAlcosiFileHandler.prepareObjectLink` loop overwrites | V1 line 271-277, V2 line 243-249 | Only last replacement applies, others lost |
| All 14 `FieldProjector*` are no-op stubs | All `project(...)` methods | Consumers get null instead of projected values |
| `TwinValidatorTwinChildrenFieldSumInStatusesPositive` null-key collision | Line 105-106 | Multiple null-id twins overwrite each other |
| `SchedulerTaskRunner.processTask` leaves tasks stuck in IN_PROGRESS | Line 52-60 | Exceptions during execute() never reset status |
| `TwinTriggerDuplicateTwin.run` clones wrong twin | Line 39 | Logs twinEntity will be cloned, clones different twin |
| `TwinTriggerCascadeToDescendants.run` NPE on null depth | Line 43 | No null normalization unlike ancestor variant |

### High-severity test gaps (add tests before these bite)

| Gap | Classes affected | Risk |
|-----|-----------------|------|
| No `verify(repo).save/delete/saveAll/deleteAll` | Multiple across scheduler, trigger | Persistence mutations not verified |
| No `verify(service).loadXxx(...)` | 15+ validators, context collectors | Silent bugs if load step dropped |
| Tests stub repo/service, never exercise real lookup | 50+ tests across all groups | Refactors break contract invisibly |
| No multi-twin/multi-item selectivity | 40+ validator/finder tests | Can't catch selection logic bugs |
| No empty/null collection coverage | 30+ tests | Edge cases untested |

---

## Recommended Next Actions

### Immediate (before next prod deploy)

1. **Fix swapped filter semantics** — `FieldFilterInStatus` and `FieldFilterNotInStatus` implementations are swapped relative to their names. Either swap implementations or rename classes to match behavior.

2. **Fix `StoragerAlcosiFileHandler.prepareObjectLink`** — accumulate replacements in StringBuilder or use `replaceEach` loop that doesn't overwrite.

3. **Fix `TwinValidatorTwinChildrenFieldSumInStatusesPositive`** — filter null ids out of result map, or fail-fast on null-id twin input.

4. **Fix `TwinTriggerCascadeToDescendants`** — add null/negative depth normalization matching `CascadeToAncestorsByHead`.

5. **Fix `SchedulerTaskRunner.processTask`** — wrap `execute()` in try/catch that resets status on failure, or remove @Transactional and let outer layer rollback.

### Short-term (this sprint)

6. **Decide on `EmailerCachedSender` race fix** — rewrite with `computeIfAbsent` or document as acceptable.

7. **Add `verify(repo).save/delete/saveAll/deleteAll`** to all scheduler/trigger/notificator tests where prod mutates persistence.

8. **Add `verify(service).loadXxx(collection)`** to all validator/context collector tests where prod loads before iterating.

9. **Add multi-element selectivity tests** to at least one test per validator class (some items pass, some fail).

10. **Add empty/null collection coverage** to all validator/finder tests that take Collections.

### Medium-term (next sprint)

11. **Clarify projector stubs** — either implement them or mark as @Deprecated/TODO and document they're placeholders.

12. **Fix `TwinTriggerDuplicateTwin`** — either make it clone `twinEntity` or rename/log to reflect it clones a configured template.

13. **Fix `TwinTriggerChangeStatusByLinkForward`** — expose `twinClassId` parameter or document why class filtering is intentionally absent.

14. **Fix `TwinTriggerRabbitMqConnection`** — add eviction listener that calls `destroy()` on evicted factories.

15. **Fix `FieldInitializer` null handling** — add null guards in `tryToOverrideValue`, `Head.initValue`, `ListDefaultOrNull.initValue`.

### Long-term (tech debt)

16. **Consolidate V2/V3/V4 tests** — ensure they actually test behavioral divergence from V1, not just clone assertions.

17. **Add concurrency tests** for any cache/Singleton patterns (EmailerCachedSender, RabbitMqConnection, NotifierAlcosi stub cache).

18. **Add integration tests** for scheduler runners — `processTask` logic needs end-to-end verification.

19. **Audit groups 25-31** — `fieldtyper/*`, `factory/*` still unaudited.

---

## Audit Methodology Notes

- **Coverage:** ~159 prod classes read and compared against their tests.
- **Agents:** 7 parallel agents (general-purpose) each audited 2-3 groups.
- **Time budget:** ~15 minutes total agent runtime.
- **Limitations:** Per-class deep audit is impractical at this scale. Findings are based on:
  - Code pattern matching (CME, check-then-act, exception leakage)
  - Comparison with sibling implementations (e.g., sluggers, projectors)
  - Test vs prod side-effect mismatches
  - Naming vs behavior mismatches
- **False positives:** Some "bugs" may be intentional design decisions (e.g., empty freeze set passes validation). Review with domain experts before fixing.

---

## Appendix: Previously Fixed Issues (Groups 9-12)

These were fixed in earlier parts of this branch and are NOT repeated above:

- **`SluggerDomainAndBusinessAccountScopeBusinessAccountManage.enterGroup`** — added missing `userGroupMapRepository.save(ret)`; tests updated with verify.
- **`SluggerDomainAndBusinessAccountScopeBusinessAccountManageTest`** — added `verify(userGroupMapRepository).save(result)` on success, `verifyNoInteractions` on all null branches.
- **Other 4 sluggers** — added `verifyNoInteractions(userGroupMapRepository)` to `nullXxxIdOnGroup_returnsNull` tests.
- **`Notifier.validateContext`** — fixed CME by collecting null keys first, then mutating map in second pass.
- **`TrustorEncrypted.getKey`** — fixed race condition with double-checked locking.
- **`TrustorEncrypted.resolveActAsUser`** — changed from RuntimeException to ServiceException.
- **`IdentityProviderStub.resolveAuthTokenMetaData`** — fixed swapped userId/businessAccountId parsing (token format BA,user).
