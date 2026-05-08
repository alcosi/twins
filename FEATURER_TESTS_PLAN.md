---
name: Plan — unit tests for all featurers
description: Step-by-step plan for writing unit tests for every featurer group; updated as groups are completed
type: project
originSessionId: 3054013c-a4b1-4ca4-88fc-0261fe2e4e7a
---
# Unit Tests Plan: All Featurers

Work through these groups one at a time. After each group is written, the user reviews and confirms before moving to the next.

Test root: core/src/test/java/org/twins/core/featurer/

Legend: [ ] = not started · [~] = in progress · [x] = done · ✅ = reviewed   

---

## Groups (ordered small → large)

### Group 1 — fieldrule/conditionevaluator + conditiondescriptor · ~6 classes · [x] ✅
- ConditionEvaluatorParam
- ConditionEvaluatorValue
- ConditionEvaluatorDataListOptionExternalId
- ConditionDescriptorParam
- ConditionDescriptorValue
- ConditionDescriptorDataListOptionExternalId

### Group 2 — fieldrule/fieldoverwriter · 4 classes · [x] ✅
- FieldParamOverwriterStub
- FieldParamOverwriterText
- FieldParamOverwriterNumeric
- FieldParamOverwriterSelect

### Group 3 — headhunter · 2 classes · [x] ✅
- HeadHunterImpl
- HeadHunterByStatus

### Group 4 — linker · 2 classes · [x] ✅
- LinkerImpl
- LinkerByStatus

### Group 5 — datalist · 4 classes · [x] ✅
- DataListOptionFinderSharedInHead
- DataListOptionFinderSharedInHeadExcludeTwin
- DataListOptionFinderStub
- DataListOptionSorterStub

### Group 6 — pointer · 6 classes · [x] ✅
- PointerOnSelf
- PointerOnHead
- PointerOnGivenTwin
- PointerOnLinkedTwin
- PointerOnSingleChild
- PointerOnSingleGrandChild

### Group 7 — statistic · 3 classes · [x] ✅
- StatisterForParentOrChildPercent
- StatisterForParentWithoutSelfPercent
- StatisterFromFieldPercent

### Group 8 — templator + businessaccount + domain · 6 classes · [x] ✅
- TemplatorSimple
- BusinessAccountInitiatorImpl
- DomainInitiatorB2B
- DomainInitiatorBasic
- DomainUserInitiatorB2B
- DomainUserInitiatorBasic

### Group 9 — identityprovider · ~7 classes · [x] ✅
- IdentityProviderAlcosi
- IdentityProviderInternal
- IdentityProviderStub
- connectors & trustors in subpackages

### Group 10 — classfinder · 6 classes · [x] ✅
- ClassFinderExcludeIfHasAnyTwin
- ClassFinderExtendsHierarchyChildrenOf
- ClassFinderGivenSet
- ClassFinderHasSegment
- ClassFinderSegment
- + any abstract base

### Group 11 — user · 3 classes · [x] ✅
- UserFinderBySpaceIdAndRoleId
- UserFinderHeadBySpaceIdAndRoleId
- UserFinderRequested
- UserSorterStub

### Group 12 — usergroup · 7 classes · [x] ✅
- UserGroupManagerImpl
- UserGroupManagerSingleGroup
- SluggerBusinessAccountScopeBusinessAccountManage
- SluggerDomainAndBusinessAccountScopeBusinessAccountManage
- SluggerDomainScopeBusinessAccountManage
- SluggerDomainScopeDomainManage
- SluggerSystemScopeDomainManage

### Group 13 — widget · ~5 classes · [x]
- WidgetAccessorAllowAny
- WidgetAccessorAllowForKeys
- WidgetAccessorAllowForSpace
- WidgetAccessorDenyForKeys
- DataGrabber (base/stub)

### Group 14 — storager · ~8 classes · [x]
- StoragerExternalUri
- StoragerLocalDynamic
- StoragerLocalStatic
- StoragerS3Dynamic
- StoragerS3Static
- StoragerAlcosiFileHandler
- StoragerAlcosiFileHandlerV2

### Group 15 — scheduler · ~22 classes · [x]
- SchedulerAttachmentDeleteTaskCleaner / TaskRunner
- SchedulerDraftCommitTaskRunner
- SchedulerDraftEraseScopeCollectTaskRunner
- SchedulerHistoryNotificationTaskCleaner / TaskRunner
- SchedulerSchedulerLogCleaner
- SchedulerTwinArchiveCleaner
- SchedulerTwinChangeTaskRunner
- SchedulerTwinTriggerTaskRunner
- ConsistencyCheck* (~6 classes in `tasks/`)

### Group 16 — classfield/filter + classfield/sorter · 6 classes · [x]
- FieldFilterDependOnBooleanField
- FieldFilterGiven
- FieldFilterInStatus
- FieldFilterNotInStatus
- FieldSorterByOrderedIds
- FieldSorterStub
- ### Group 17 — classfield/finder · 11 classes · [x]
- FieldFinderAll
- FieldFinderByClassFromParam
- FieldFinderByClassIdGiven
- FieldFinderByIdExcludeHeadMatched
- FieldFinderByIdGiven
- FieldFinderByRelevantProjection
- FieldFinderByRequiredFalse / True
- FieldFinderBySystemFalse / True
- FieldFinderStub

### Group 18 — classfield/projector · 14 classes · [x]
- FieldProjectorFetchDecimalToStringV1
- FieldProjectorFetchIntegerToStringV1
- FieldProjectorFetchStringToStringV1
- FieldProjectorNumericToDataListV1`–`V4
- FieldProjectorNumericToNumericV1
- FieldProjectorStringToDataListV1`–`V4
- FieldProjectorStringToNumericV1 / V2

### Group 19 — twin/detector + twin/sorter · 9 classes · [x]
- SearchDetectorConcat
- SearchDetectorPreferProtected
- TwinSorterBooleanField
- TwinSorterDataListField
- TwinSorterDateField
- TwinSorterNumberField
- TwinSorterStub
- TwinSorterTextField
- TwinSorterTwinField

### Group 20 — twin/finder · 20 classes · [x]
- TwinFinderByAssigneeUserEqualsCurrent
- TwinFinderByAssigneeUserIdGiven / Requested
- TwinFinderByClassIdGiven / Requested
- TwinFinderByCreatedByUserEqualsCurrent
- TwinFinderByCreatedByUserIdGiven / Requested
- TwinFinderByHeadTwinIdGiven / Requested
- TwinFinderByHierarchyTreeContainsGiven / Requested
- TwinFinderByIdEqualsCurrentUser
- TwinFinderByIdGiven / Requested
- TwinFinderByLinkDstTwinGiven / Requested
- TwinFinderByLinkGiven / Requested
- TwinFinderByMarkerDataListOptionIdGiven
- TwinFinderByStatusIdGiven / Requested
- TwinFinderByTagDataListOptionIdGiven
- TwinFinderCurrentUser
- TwinFinderRequested

### Group 21 — twin/validator · 22 classes · [x]
- TwinValidatorApiUserIsMemberOfGroup
- TwinValidatorApiUserIsMemberOfSpace
- TwinValidatorCountOfTwinsSameTwinClassGTEValue
- TwinValidatorNotNull
- TwinValidatorTwinAllChildrenInStatuses
- TwinValidatorTwinAssigneeIsNull
- TwinValidatorTwinAssigneeToCurrentUser
- TwinValidatorTwinChildrenBooleanFieldHasValue
- TwinValidatorTwinChildrenFieldSumInStatusesPositive
- TwinValidatorTwinClassHasFreeze
- TwinValidatorTwinCreatedByCurrentUser
- TwinValidatorTwinFieldBooleanHasValue
- TwinValidatorTwinFieldDateLessThenNow
- TwinValidatorTwinFieldNotNull
- TwinValidatorTwinHasBackwardLink
- TwinValidatorTwinHasChildrenInStatuses
- TwinValidatorTwinHasChildrenOfClasses
- TwinValidatorTwinHasLink
- TwinValidatorTwinHasLinkAndDstTwinHasStatus
- TwinValidatorTwinInStatuses
- TwinValidatorTwinMarkerExist
- TwinValidatorTwinOfTwinClass

### Group 22 — notificator · ~31 classes · [x]
- ContextCollector* (13 classes)    
- RecipientResolver* (6 classes)
- EmailerCachedSender / EmailerInternal
- NotifierAlcosiNotificationManager

### Group 23 — trigger · ~17 classes · [x]
- TwinTriggerCascadeToAncestorsByHead
- TwinTriggerCascadeToDescendants
- TwinTriggerChangeLinkedTwinsChildrenByHead
- TwinTriggerChangeStatusBy* (7 variants)
- TwinTriggerClearAllUsersTouch / CurrentUserTouch
- TwinTriggerDuplicateTwin
- TwinTriggerRabbitMq* (4 classes in `messaging/rabbitmq/`)

### Group 24 — fieldinitializer · 9 classes · [x]
- FieldInitializerBoolean
- FieldInitializerDateCurrent / DateFixed
- FieldInitializerHead
- FieldInitializerList / ListDefaultOrNull
- FieldInitializerNull
- FieldInitializerTextFixed
- FieldInitializerUrlFixed

### Group 25 — fieldtyper/value + fieldtyper/descriptor · ~34 classes · [ ]
- FieldValue* (17 value types)
- FieldDescriptor* (17 descriptor types)

### Group 26 — fieldtyper/storage · 26 classes · [ ]
- TwinFieldStorage* (all storage implementations)
### Group 27 — fieldtyper (core) · ~35 concrete implementations · [ ]
- FieldTyperBoolean / BooleanV1
- FieldTyperDecimal / DecimalBase / DecimalIncrement
- FieldTyperI18n, FieldTyperLink, FieldTyperList
- FieldTyperSelect, FieldTyperSharedSelect*
- FieldTyperSimple / SimpleNonIndexed
- FieldTyperTimestamp, FieldTyperUrl, FieldTyperUser
- FieldTyperCalc* (arithmetic field typers — ~15 classes)
- FieldTyperCount* (count variants)
- Assignee/Status/Marker/Tags/Aliases/etc. base field typers

### Group 28 — factory/multiplier · 14 classes · [ ]
- MultiplierAggregate
- MultiplierIsolated
- MultiplierIsolatedByLink
- MultiplierIsolatedChildrenInStatuses
- MultiplierIsolatedCopy / CopyWithDepth / CopyWithDepthAndClassChange
- MultiplierIsolatedOnContextField / OnContextFieldList
- MultiplierIsolatedOnContextTwinChildClass
- MultiplierIsolatedRelativesByHead
- MultiplierIsolatedShiftHead
- MultiplierIsolatedTwinByLinkedHeadTwin
- MultiplierIsolatedTwinsOfTwinClass

### Group 29 — factory/lookuper · 16 classes · [ ]
- FieldLookuperFrom* (all FieldLookuper implementations)
- FieldLookuperLinkedTwinByField / ByLink
- FieldLookuperNearest

### Group 30 — factory/conditioner · 42 classes · [ ]
Split into two passes:
- 30a — context conditioners: ConditionerContext* (~15)
- 30b — factory item conditioners: ConditionerFactory* (~20) + rest

### Group 31 — factory/filler · 49 classes · [ ]
Split into two passes:
- 31a — FillerBasics* + FillerField* (~30)
- 31b — FillerLinks* + FillerHead* + FillerMarker* + FillerForward* + rest (~20)

---

## Notes
- Start each group by reading the source files, then write tests in the mirrored test package.
- Tests live in the same Java package as the production class (under src/test/java`), not in `unit.* / integration.*. This preserves access to protected and package-private members. Never widen production visibility for testing.
- Follow existing test style: @ExtendWith(MockitoExtension.class), @Nested per scenario, @Spy @InjectMocks for the subject when it has self-calls.
- Mock only collaborators actually stubbed/verified in the test (per project feedback).
- Large groups (27–31) may be split if the user prefers.

## Test design philosophy — derive expectations from intended logic, not from current code

Tests must encode what the method **should do** (its contract, name, semantics, realistic call sites), not a transcription of what the current implementation happens to do. A test that mirrors a buggy implementation passes forever and hides the bug.

For each method under test, before writing assertions:
1. State what the method is supposed to do in one sentence (its contract).
2. List the inputs/branches/edge cases that fall out of that contract.
3. Compare the contract to what the code actually does. If they diverge — that's a bug, not a quirk to encode.

If the intended behavior is unclear — **ask the user** before writing the test or "fixing" prod to match a guess. Don't encode a silent assumption. Surface every divergence between contract and implementation in the chat so the user can confirm fix-vs-encode.
