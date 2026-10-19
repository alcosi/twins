# Twins Glossary

Reference dictionary of the main domain entities in the Twins platform. For each entity: a short definition, the JPA class file path, the key fields, and the relationships to other entities.

Use this document alongside:

- `docs/api_starter.md` — DTO / controller / service conventions for settings APIs
- `docs/dto_code_convention.md` — DTO hierarchy and naming
- `docs/entity_many_to_one_relations.md` — `*SpecOnly` / runtime field split
- `docs/load_method_pattern.md` — batch-loading `@Transient` fields

---

## 1. Core Domain

### Twin

The central business object — a single instance of a Twin Class. Twinks can be linked, tagged, commented on, attached to, and put through state transitions (twinflows).

- **Class:** `org.twins.core.dao.twin.TwinEntity`
- **Table:** `twin`
- **Key fields:** `id`, `twinClassId`, `headTwinId`, `hierarchyTree` (ltree path), `externalId`, `ownerBusinessAccountId`, `ownerUserId`, `viewPermissionId`, `viewPermissionCustom`, `createdAt`
- **Relationships:**
  - belongs to `TwinClass` (via `twinClassId`)
  - has one `TwinStatus` (current state in its twinflow)
  - has many `TwinLink` (outgoing), `TwinTag`, `TwinMarker`, `TwinAttachment`, `TwinComment`, `TwinAlias`, `TwinField*` (typed field value tables)
  - optional parent via `headTwinId` (hierarchy via `hierarchyTree` ltree)
- **Notes:** Hierarchy uses PostgreSQL `ltree` (`hierarchy_tree` column with `LtreeUserType`). Owner is split into `ownerBusinessAccountId` + `ownerUserId` to support both org-owned and user-owned twins.

### TwinClass

The type (schema) of a Twin — defines which fields, statuses, twinflows, links, and factories apply. a "class" in OOP for twins.

- **Class:** `org.twins.core.dao.twinclass.TwinClassEntity`
- **Table:** `twin_class`
- **Key fields:** `id`, `domainId`, `key` (unique within domain), `twinClassFreezeId`, `permissionSchemaSpace`, `twinflowSchemaSpace`, `twinClassSchemaSpace`, `aliasSpace`, `viewPermissionId`, `createPermissionId`, `abstractt` (abstract class flag)
- **Relationships:**
  - belongs to `Domain`
  - has many `TwinClassField` (field definitions), `TwinStatus`, `Twinflow`, `Link`, `TwinFactory`
  - has schema refs: `TwinClassSchema`, `TwinflowSchema`, `PermissionSchema` (via `*SchemaSpace` flags)
- **Notes:** `id` is deterministic via `UUID.nameUUIDFromBytes((key + domainId).getBytes())` in `@PrePersist` — same key in same domain always produces the same id. The `*Space` boolean flags indicate whether the class uses space-scoped (per-instance) schema versions or the domain-wide one.

### TwinClassField

A single field definition on a TwinClass — its type, validation rules, storage backend. Stored separately from values because the schema is class-level, values are twin-level.

- **Class:** `org.twins.core.dao.twinclass.TwinClassFieldEntity`
- **Table:** `twin_class_field`
- **Key fields:** `id`, `twinClassId`, `key`, `inheritable`, `nameI18nId`, `descriptionI18nId`, plus featurer-driven `fieldStorage` / `fieldType` / validation rules
- **Relationships:** belongs to `TwinClass`; field values live in `TwinField*` tables (one per type: `TwinFieldSimpleEntity`, `TwinFieldBooleanEntity`, `TwinFieldDecimalEntity`, `TwinFieldTimestampEntity`, `TwinFieldUserEntity`, `TwinFieldTwinClassEntity`, `TwinFieldDataListEntity`, `TwinFieldI18nEntity`).
- **Notes:** `id` is deterministic — `UUID.nameUUIDFromBytes((key + twinClassId).getBytes())`. The `FeaturerEntity` reference (`org.cambium.featurer.dao.FeaturerEntity`) drives pluggable field types and storage backends — see the `featurer/fieldtyper/` package.

### TwinStatus

A status a Twin can be in within its Twinflow (e.g., DRAFT, ACTIVE, ARCHIVED). One TwinClass has many defined statuses; one Twin holds a current status.

- **Class:** `org.twins.core.dao.twin.TwinStatusEntity`
- **Table:** `twin_status`
- **Key fields:** `id`, `twinClassId`, `key`, `inheritable`, `nameI18nId`, `descriptionI18nId`, `iconLightResourceId`, `iconDarkResourceId`, `backgroundColor`, `fontColor`
- **Relationships:** belongs to `TwinClass`; referenced by `Twinflow.initialTwinStatusId` and `TwinflowTransition.srcTwinStatusId` / `dstTwinStatusId`.

### TwinAlias

A short, human-readable identifier for a Twin (e.g., `PROJECT-123`). Different alias types have different uniqueness scopes.

- **Class:** `org.twins.core.dao.twin.TwinAliasEntity`
- **Table:** `twin_alias`
- **Key fields:** `id`, `alias` (the value), `aliasTypeId` (enum `TwinAliasType`), `twinId`, `userId`, `domainId`, `businessAccountId`
- **Alias types (`org.twins.core.enums.twin.TwinAliasType`):**
  - `D` — Domain-wide unique
  - `C` — Twin-class unique within domain
  - `B` — BA-unique within domain
  - `S` — Space alias, owner = domain
  - `T` — Space alias, owner = domain-business-account
  - `K` — Space alias, owner = domain-user

### TwinLink / Link

`TwinLink` is an instance of a typed relationship between two twins. `Link` is the type definition (declared at the TwinClass level).

- **Classes:**
  - `org.twins.core.dao.twin.TwinLinkEntity` (instance; table `twin_link`)
  - `org.twins.core.dao.link.LinkEntity` (type definition; table `link`)
- **TwinLink fields:** `id`, `srcTwinId`, `dstTwinId`, `linkId`, `createdByUserId`, `createdAt`
- **Link fields:** `id`, `domainId`, `key`, type/strength enums, source and destination TwinClass refs
- **Notes:** Links are typed and directional (`src_twin_id` → `dst_twin_id`). Triggers (`LinkTriggerEntity`) and validators (`LinkValidatorEntity`) can be attached at the type level.

### TwinTag / TwinMarker

Lightweight classification labels attached to a Twin. Both reference a `DataListOption` (an option from a `DataList`).

- **Classes:**
  - `org.twins.core.dao.twin.TwinTagEntity` (table `twin_tag`)
  - `org.twins.core.dao.twin.TwinMarkerEntity` (table `twin_marker`)
- **Both have:** `id`, `twinId`, `{tag,marker}DataListOptionId`, `createdAt`, unique constraint `(twin_id, *_data_list_option_id)`
- **Difference:** tags are typically user-assigned; markers are typically system/computed. TwinClass defines which DataLists are valid for tags vs markers.

---

## 2. Workflow (Twinflow & Factory)

### Twinflow

A state machine attached to a TwinClass — defines which statuses a Twin can be in and the legal transitions between them. One TwinClass can have at most one active Twinflow.

- **Class:** `org.twins.core.dao.twinflow.TwinflowEntity`
- **Table:** `twinflow`
- **Key fields:** `id`, `twinClassId`, `inheritable`, `nameI18nId`, `descriptionI18nId`, `createdByUserId`, `initialTwinStatusId`, `eraseflowId`, `createdAt`
- **Relationships:**
  - belongs to `TwinClass`
  - has many `TwinflowTransition`, `TwinflowFactory` (factories triggered by the flow)
  - references `Eraseflow` for cascade-delete policy

### TwinflowTransition

A directed edge in a Twinflow — moves a Twin from `srcTwinStatusId` to `dstTwinStatusId`. Has permission gate, validators and optional triggers.

- **Class:** `org.twins.core.dao.twinflow.TwinflowTransitionEntity`
- **Table:** `twinflow_transition`
- **Key fields:** `id`, `twinflowId`, `srcTwinStatusId`, `dstTwinStatusId`, `nameI18nId`, `descriptionI18nId`, `screenId`, `permissionId`, `twinflowTransitionTypeId` (enum)
- **Transition types (`TwinflowTransitionType`):** see `org.twins.core.dao.twinflow.TwinflowTransitionTypeEntity` (enum stored as string in DB).
- **Relationships:**
  - belongs to `Twinflow`
  - has `TwinflowTransitionTriggerEntity` (side effects on transition)
  - has validators and aliases

### TwinflowTransitionType

Enumeration of transition kinds. Stored as a lookup table so the enum is DB-driven.

- **Class:** `org.twins.core.dao.twinflow.TwinflowTransitionTypeEntity`
- **Table:** `twinflow_transition_type`
- **Key fields:** `id` (the enum value, stored as `EnumType.STRING`), `description`

### Eraseflow

A cascade-delete policy attached to a TwinClass — defines what happens to related twins / links / attachments when a twin is deleted.

- **Class:** `org.twins.core.dao.eraseflow.EraseflowEntity`
- **Table:** `eraseflow`
- **Key fields:** `id`, `twinClassId`, `nameI18nId`, `descriptionI18nId`
- **Related:** `EraseflowLinkCascadeEntity` (per-link-type cascade behavior)
- **Used by:** `Twinflow.eraseflowId` — the erase policy attached to a flow.

### TwinFactory

A reusable "recipe" for creating or transforming Twins. Composed of pipelines, multipliers, branches, erasers, conditions, and triggers. Launched by twinflows (via `TwinflowFactory`), transitions or directly.

- **Class:** `org.twins.core.dao.factory.TwinFactoryEntity`
- **Table:** `twin_factory`
- **Key fields:** `id`, `key`, `domainId`, `nameI18nId`, `descriptionI18nId`, `createdByUserId`, `createdAt`
- **Sub-components (all under `dao/factory/`):**
  - `TwinFactoryPipelineEntity` — ordered steps; references `inputTwinClassId` and `TwinFactoryConditionSetEntity`
  - `TwinFactoryPipelineStepEntity` — single step in a pipeline
  - `TwinFactoryMultiplierEntity` — multiplies outputs (e.g., create N copies); has `TwinFactoryMultiplierFilterEntity`
  - `TwinFactoryBranchEntity` — conditional branching
  - `TwinFactoryEraserEntity` — deletes/cleans during the pipeline
  - `TwinFactoryTriggerEntity` — side-effect triggers
  - `TwinFactoryConditionEntity` / `TwinFactoryConditionSetEntity` — condition trees

### TwinflowFactory

A binding that attaches a `TwinFactory` to a `Twinflow` with a launcher type — declares when (on which lifecycle event) the factory should fire.

- **Class:** `org.twins.core.dao.twinflow.TwinflowFactoryEntity`
- **Table:** `twinflow_factory`
- **Key fields:** `id`, `twinflowId`, `twinFactoryId`, `twinFactoryLauncher` (enum `FactoryLauncher`)
- **Notes:** `FactoryLauncher` values (e.g., onTwinCreate, onTwinUpdate, afterTwinUpdate) are defined in `org.twins.core.enums.factory.FactoryLauncher`.

---

## 3. Multi-Tenancy & Organization

### Domain

The top-level tenant. Every other entity is scoped to a Domain. Provides the default schemas (permission, twinflow, twinclass) and the locale set.

- **Class:** `org.twins.core.dao.domain.DomainEntity`
- **Table:** `domain`
- **Key fields:** `id`, `key`, `domainStatusId` (enum `DomainStatus`), `name`, `description`, `permissionSchemaId`, plus default schema refs and identity provider config
- **Relationships:**
  - has many `TwinClass`, `Twinflow`, `TwinFactory`, `Link`, `DataList`, `Permission`
  - has many `DomainBusinessAccount`, `DomainUser`, `UserGroup`, `Space`
  - has `DomainType`, `DomainStatus`, `DomainLocale` (lookup tables)

### DomainType

A "kind" of Domain — drives featurer selection for initiator, user-group manager, etc.

- **Class:** `org.twins.core.dao.domain.DomainTypeEntity`
- **Table:** `domain_type`
- **Key fields:** `id` (string key, not UUID), `name`, `description`
- **Notes:** 
  - basic - Domains are a thing in itself and can’t have Business Accounts - they are a single entity in this branch.
  - b2b - Domains can have Business Accounts under their umbrella and can operate with them

### DomainBusinessAccount

A BusinessAccount registered in a Domain — the join entity that gives a BA access to a domain (b2b only) with its own permission schema override.

- **Class:** `org.twins.core.dao.domain.DomainBusinessAccountEntity`
- **Table:** `domain_business_account`
- **Key fields:** `id`, `domainId`, `businessAccountId`, `permissionSchemaId`
- **Relationships:** many-to-one to `Domain` and `BusinessAccount`.

### DomainUser

A User registered in a Domain — the join entity for domain membership.

- **Class:** `org.twins.core.dao.domain.DomainUserEntity`
- **Table:** `domain_user`
- **Key fields:** `id`, `domainId`, `userId`

### BusinessAccount

An organizational unit (tenant sub-cluster). Twins can be owned by a BA, users belong to BAs via `BusinessAccountUser`.

- **Class:** `org.twins.core.dao.businessaccount.BusinessAccountEntity`
- **Table:** `business_account`
- **Key fields:** `id`, `name`, `ownerUserGroupId`, `createdAt`

### BusinessAccountUser

Join entity: user membership in a BusinessAccount.

- **Class:** `org.twins.core.dao.businessaccount.BusinessAccountUserEntity`
- **Table:** `business_account_user`

### User

An authenticated user account. Email is normalized to lowercase in `@PrePersist`. Status driven by `UserStatus` enum.

- **Class:** `org.twins.core.dao.user.UserEntity`
- **Table:** `user`
- **Key fields:** `id`, `name`, `email` (lowercased), `avatar`, `createdAt`, `userStatusId` (`UserStatus` enum)
- **Relationships:**
  - has many `DomainUser`, `BusinessAccountUser`, `UserGroupMap` (group memberships)
  - `userGroupMapsSpecOnly` is the deprecated spec-only relation (see `entity_many_to_one_relations.md`)

### UserGroup

A named group of users within a Domain/BusinessAccount — used for permission grants and notifications.

- **Class:** `org.twins.core.dao.user.UserGroupEntity`
- **Table:** `user_group`
- **Key fields:** `id`, `domainId`, `businessAccountId`, `type` (`UserGroupType` enum)
- **Membership:** via `UserGroupMapEntity` (in `dao/usergroup/`)

### Space

An isolated context inside a Domain — allows per-instance schema overrides. A Space is itself backed by a Twin (the `twinId` field references a Twin of class "space").

- **Class:** `org.twins.core.dao.space.SpaceEntity`
- **Table:** `space`
- **Key fields:** `id` (note: PK is `twin_id`, not its own UUID), `twinId`, `key`, `permissionSchemaId`, `twinflowSchemaId`, `twinClassSchemaId`, `domainAliasCounter`, `businessAccountAliasCounter`
- **Relationships:** has many `SpaceRole`, which in turn have `SpaceRoleUser` and `SpaceRoleUserGroup` for membership.

### SpaceRole

A role defined inside a Space — grants permissions to users/user-groups scoped to that space.

- **Class:** `org.twins.core.dao.space.SpaceRoleEntity`
- **Table:** `space_role`
- **Key fields:** `id`, `twinClassId`, `businessAccountId`, `key`, `nameI18nId`, `descriptionI18nId`
- **Membership:** `SpaceRoleUserEntity`, `SpaceRoleUserGroupEntity` (in `dao/space/`)

---

## 4. Permissions

### Permission

An atomic permission (e.g., `TWIN_CREATE`, `TWINCLASS_UPDATE`). Centralized enum-like registry; grouped via `PermissionGroup`.

- **Class:** `org.twins.core.dao.permission.PermissionEntity`
- **Table:** `permission`
- **Key fields:** `id`, `key`, `permissionGroupId`, `nameI18nId`, `descriptionI18nId`

### PermissionGroup

A grouping of related permissions (e.g., all Twin-class permissions).

- **Class:** `org.twins.core.dao.permission.PermissionGroupEntity`
- **Table:** `permission_group`

### PermissionSchema

A named set of permission grants — attached to a Domain, BusinessAccount, Space, or TwinClass.

- **Class:** `org.twins.core.dao.permission.PermissionSchemaEntity`
- **Table:** `permission_schema`
- **Key fields:** `id`, `domainId`, `businessAccountId`, `name`, `description`

### Permission grants

Concrete grant entities — assign a permission to a subject (user, user-group, space role, twin role) within a scope. All in `org.twins.core.dao.permission`:

- `PermissionGrantGlobalEntity` — domain-level grant
- `PermissionGrantUserEntity` — per-user grant
- `PermissionGrantUserGroupEntity` — per-group grant
- `PermissionGrantSpaceRoleEntity` — per-space-role grant
- `PermissionGrantTwinRoleEntity` — per-twin-role grant

---

## 5. Content (Comments / Attachments / Lists)

### TwinComment

A user comment on a Twin. Supports nested actions (edits, replies) via `TwinCommentActionSelfEntity` and alien-permission-controlled actions via `TwinCommentActionAlienPermissionEntity`.

- **Class:** `org.twins.core.dao.comment.TwinCommentEntity`
- **Table:** `twin_comment`
- **Key fields:** `id`, `twinId`, `text`, `createdByUserId`, `createdAt`
- **Notes:** Uses `@Getter @Setter` (NOT `@Data`) — explicit Lombok discipline for entities with `@Transient` kits.

### TwinAttachment

A file attached to a Twin (or to a TwinflowTransition). Stored via `StorageEntity` 

- **Class:** `org.twins.core.dao.attachment.TwinAttachmentEntity`
- **Table:** `twin_attachment`
- **Key fields:** `id`, `twinId`, `twinflowTransitionId`, plus file metadata via `StorageEntity` reference
- **Notes:** Has its own action restriction entities (`TwinAttachmentRestrictionEntity`, `TwinAttachmentModificationEntity`) and alien-permission controls (`TwinAttachmentActionAlienPermissionEntity`). Deletion is async via `AttachmentDeleteTaskEntity`.

### DataList

A named list of options — used as the source of valid values for tags, markers, and `TwinFieldDataList` fields.

- **Class:** `org.twins.core.dao.datalist.DataListEntity`
- **Table:** `data_list`
- **Key fields:** `id`, `key`, `domainId`, `nameI18nId`, `descriptionI18nId`
- **Related:** `DataListOptionEntity` (individual options), `DataListSubsetEntity` (named subset of options), `DataListSubsetOptionEntity` (subset membership)

---

## 6. Cross-Cutting

### I18n

The translation-key entity — multi-language strings are stored as UUID references to `i18n` rows, with translations in `i18n_translation`. NEVER store raw translatable strings on entities.

- **Package:** `org.twins.core.dao.i18n`
- **Usage pattern:** `entity.getNameI18nId()` returns the UUID; `I18nService` resolves translations at runtime.
- **See also:** `I18nService.collectI18nIds()` for batch collection; `I18nExportService` for SQL export ordering.

### Featurer

The pluggable feature system (from Cambium). Field types, storage backends, twinflow transition types, multipliers, conditions, triggers, validators — all are featurers.

- **Class:** `org.cambium.featurer.dao.FeaturerEntity`
- **Lookup table:** `featurer` in DB
- **Project extension point:** `org.twins.core.featurer.FeaturerTwins`
- **Notes:** Never instantiate featurers directly — resolve via the featurer registry.

### Schemas (TwinClassSchema / TwinflowSchema / PermissionSchema)

Inheritance / template entities — when a TwinClass is `inheritable`, child TwinClasses can reference the parent's schema for fields, statuses, twinflows, and permissions.

- **`TwinClassSchemaEntity`** (`dao/twinclass/`) — table `twin_class_schema`, scoped to `domainId`
- **`TwinflowSchemaEntity`** (`dao/twinflow/`) — table `twinflow_schema`, scoped to `domainId` + optional `businessAccountId`
- **`PermissionSchemaEntity`** (`dao/permission/`) — table `permission_schema`, scoped to `domainId` + optional `businessAccountId`
- **Notes:** The `*SchemaSpace` flag on `TwinClassEntity` controls whether instances of the class use the domain-level schema or a per-instance (space-scoped) one.

---

## 7. Field Value Tables

Each typed field value is stored in a dedicated table to allow type-safe queries and indexes:

| Field Type | Entity | Table |
|---|---|---|
| Simple (string/int) | `TwinFieldSimpleEntity` | `twin_field_simple` |
| Simple non-indexed | `TwinFieldSimpleNonIndexedEntity` | `twin_field_simple_non_indexed` |
| Boolean | `TwinFieldBooleanEntity` | `twin_field_boolean` |
| Decimal | `TwinFieldDecimalEntity` | `twin_field_decimal` |
| Timestamp | `TwinFieldTimestampEntity` | `twin_field_timestamp` |
| User reference | `TwinFieldUserEntity` | `twin_field_user` |
| TwinClass reference | `TwinFieldTwinClassEntity` | `twin_field_twin_class` |
| DataList option | `TwinFieldDataListEntity` | `twin_field_data_list` |
| I18n text | `TwinFieldI18nEntity` | `twin_field_i18n` |

All entities live in `org.twins.core.dao.twin`. Each references `twinId` + `twinClassFieldId` + the typed value column.

---

## 8. Field Rules & Validation

### TwinClassFieldRule

Cross-field rule on a TwinClass — drives validation when a twin is created/updated.

- **Class:** `org.twins.core.dao.twinclass.TwinClassFieldRuleEntity`
- **Table:** `twin_class_field_rule`
- **Related:** `TwinClassFieldRuleMapEntity` (rule-to-field mapping), `TwinClassFieldConditionEntity` (conditions), `TwinClassFieldAttributeEntity` (per-field attributes), `TwinClassFieldSearchEntity` / `TwinClassFieldSearchPredicateEntity` (search indexing).

### Validators

Action-level validators — gates that must pass before an action (twin action, attachment action, comment action, transition) executes.

- **Package:** `org.twins.core.dao.validator`
- **Examples:** `TwinActionValidatorRuleEntity`, `TwinflowTransitionValidatorRuleEntity`, `TwinAttachmentActionAlienValidatorRuleEntity`, `TwinCommentActionAlienValidatorRuleEntity`

---

## 9. Other Supporting Entities (overview)

| Entity | Package | Purpose |
|---|---|---|
| `ActionRestrictionReasonEntity` | `dao/action` | Why an action was denied (for diagnostics) |
| `TwinActionPermissionEntity` | `dao/action` | Per-action permission policy on TwinClass |
| `NotificationSchemaEntity` | `dao/notification` | Notification template set scoped to a domain/BA |
| `History*Entity` | `dao/history`, `dao/history/context` | Audit trail of changes (with snapshots) |
| `ResourceEntity`, `StorageEntity` | `dao/resource` | Binary resources (attachments, icons, avatars) — MinIO-backed |
| `IdentityProviderEntity` | `dao/idp` | External IdP integration (OIDC / SAML) |
| `Scheduler*Entity` | `dao/scheduler` | Background job definitions |
| `TwinChangeTaskEntity`, `TwinWorkEntity`, `TwinTouchEntity` | `dao/twin` | Async change tracking, work items, last-touch bookkeeping |
| `Template*Entity` | `dao/template` | TwinClass / field templates with generators |
| `FaceEntity` | `dao/face` | UI "face" (view layout) for a TwinClass |
| `ProjectionEntity` | `dao/projection` | Pre-computed projections (counts, aggregations) |
| `Statistic*Entity` | `dao/statistic` | Long-term statistics tables |

---

## Conventions

- **`id` generation:** Most entities use `org.cambium.common.util.UuidUtils.ifNullGenerate(id)` in `@PrePersist` — UUID-v7 time-ordered, good for index locality.
- **Deterministic ids:** `TwinClassEntity`, `TwinClassFieldEntity`, `DataListEntity` use `UUID.nameUUIDFromBytes((key + scopeId).getBytes())` — same key in same scope always produces the same id (stable across environments).
- **i18n fields:** Always store `UUID` reference (`nameI18nId`, `descriptionI18nId`); never store raw strings.
- **`inheritable` flag:** Common on schema-defining entities — when true, child TwinClasses inherit the definition.
- **Spec vs runtime relations:** Many entities carry `*SpecOnly` deprecated relations for JPA Criteria queries and `@Transient` runtime fields for business logic — see `docs/entity_many_to_one_relations.md`.
