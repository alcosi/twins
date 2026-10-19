---
slug: domain
title: Domain
category: multi-tenancy
class: DomainEntity
table: domain
is_system: true
actualized_at: 2026-06-17
see_also:
  - twin-class
  - twinflow
  - permission
---

# Domain

## Summary

The top-level tenant in the platform. Every other entity (TwinClass, Twinflow, Twin, User, BusinessAccount) is scoped to a Domain. Domains carry their own schemas (permission, twinflow, twinclass), identity provider bindings, storage backends, and i18n locale defaults.

## Purpose

Multi-tenancy in Twins is implemented as Domain isolation. A Domain is the outermost boundary: data from one Domain is invisible to another unless explicitly shared. This means each Domain has its own TwinClasses, its own Twinflows, its own permission sets, its own DataLists, its own Users (via DomainUser), and its own BusinessAccounts (via DomainBusinessAccount).

Each Domain carries three top-level schema references — `permissionSchemaId`, `twinflowSchemaId`, `twinClassSchemaId` — which are the defaults that new TwinClasses inherit unless overridden. Domains also configure infrastructure: which IdentityProvider handles authentication (`identityProviderId`), where attachments and resources are stored (`attachmentsStorageId`, `resourcesStorageId`), and what the default i18n locale is (`defaultI18nLocaleId`).

The `domainType` enum (`DomainType`) classifies the kind of Domain — drives featurer selection for domain-level behaviors (initiator featurers for BusinessAccount/User creation, user-group-manager featurer for group resolution, etc.).

## Fields

- `id` — primary key (UUID v7)
- `key` — unique short identifier (e.g., `acme`, `internal`)
- `domainStatusId` — enum `DomainStatus` (`ACTIVE` / `DISABLED`)
- `name` — display name
- `description` — long description
- `permissionSchemaId` — FK to [PermissionSchema](permission-schema.md), default permission schema for new TwinClasses
- `twinflowSchemaId` — FK to TwinflowSchema, default twinflow schema
- `twinClassSchemaId` — FK to TwinClassSchema, default twinclass schema
- `businessAccountTemplateTwinId` — FK to [Twin](twin.md), template for new BusinessAccounts
- `domainUserTemplateTwinId` — FK to [Twin](twin.md), template for new DomainUsers
- `defaultTierId` — FK to Tier, default service tier for the domain
- `defaultI18nLocaleId` — default locale for i18n resolution (stored as `Locale`, converted via `LocaleConverter`)
- `ancestorTwinClassId` — FK to [TwinClass](twin-class.md), root class for inheritance
- `businessAccountInitiatorFeaturerId` + `businessAccountInitiatorParams` — featurer driving BusinessAccount creation (hstore for params)
- `domainUserInitiatorFeaturerId` + `domainUserInitiatorParams` — featurer driving DomainUser creation (hstore for params)
- `userGroupManagerFeaturerId` + `userGroupManagerParams` — featurer for user-group resolution (hstore for params)
- `iconLightResourceId` / `iconDarkResourceId` — FK to Resource, UI icons
- `attachmentsStorageId` — FK to Storage, where attachments live
- `resourcesStorageId` — FK to Storage, where binary resources live
- `navbarFaceId` — FK to Face, default navbar layout
- `identityProviderId` — FK to IdentityProvider, authentication integration
- `notificationSchemaId` — FK to NotificationSchema, notification template set
- `domainType` — enum `DomainType` (`basic` / `b2b`, converted via `DomainTypeConverter`)
- `attachmentsStorageUsedCount` / `attachmentsStorageUsedSize` — materialized usage counters
- `createdAt` — creation timestamp

## Relations

| Target | Cardinality | Kind | Description |
|---|---|---|---|
| [TwinClass](twin-class.md) | one-to-many | own_collection | Classes declared in this domain |
| [Twinflow](twinflow.md) | one-to-many | own_collection | Flows declared in this domain |
| [TwinFactory](twin-factory.md) | one-to-many | own_collection | Factories declared in this domain |
| [Link](link.md) | one-to-many | own_collection | Link types declared in this domain |
| [DataList](data-list.md) | one-to-many | own_collection | DataLists declared in this domain |
| [Permission](permission.md) | one-to-many | own_collection | Permissions declared in this domain |
| DomainBusinessAccount | one-to-many | own_collection | BA-to-domain membership |
| DomainUser | one-to-many | own_collection | User-to-domain membership |
| [PermissionSchema](permission-schema.md) | many-to-one | owning | Default permission schema for new classes |
| TwinflowSchema | many-to-one | owning | Default twinflow schema |
| TwinClassSchema | many-to-one | owning | Default twinclass schema |
| [Twin](twin.md) | many-to-one | owning | BusinessAccount template twin |
| [Twin](twin.md) | many-to-one | owning | DomainUser template twin |
| [TwinClass](twin-class.md) | many-to-one | owning | Ancestor class for inheritance |
| [IdentityProvider](identity-provider.md) | many-to-one | owning | Authentication integration |
| Storage | many-to-one | owning | Attachments backend |
| Storage | many-to-one | owning | Resources backend |
| NotificationSchema | many-to-one | owning | Notification template set |
| Face | many-to-one | owning | Default navbar layout |
| Resource | many-to-one | owning | UI icons (light + dark) |
| DomainType | many-to-one | semantic | Domain classification (drives featurer selection) |
| DomainStatus | many-to-one | semantic | Lifecycle state (active / suspended / archived) |
| DomainLocale | one-to-many | own_collection | Available locales |

## API

Domain CRUD:

- `POST   /private/domain/v1` — create (JSON)
- `POST   /private/domain/v2` — create (multipart)
- `PUT    /private/domain/v1` — update (JSON)
- `PUT    /private/domain/v2` — update (multipart)
- `GET    /private/domain/{domainId}/v1` — view single
- `GET    /private/domain/list/v1` — list domains
- `GET    /private/domain/class_owner_type/list/v1` — list class-owner-type enum values

BusinessAccount membership:

- `POST   /private/domain/{domainId}/business_account/v1` — add BA to domain
- `POST   /private/domain/{domainId}/business_account/{businessAccountId}/v1` — update BA-in-domain
- `DELETE /private/domain/business_account/v1` — remove BA from domain
- `GET    /private/domain/business_account/{businessAccountId}/v1` — view BA-in-domain
- `POST   /private/domain/business_account/search/v1` — search BAs in domain
- `POST   /private/domain/business_account/count/v1` — count
- `GET    /private/domain/list/v1` — list (alternative)

User membership:

- `POST   /private/domain/{domainId}/user/v1` — add user to domain
- `DELETE /private/domain/{domainId}/user/{userId}/v1` — remove user
- `GET    /private/domain/user/v1` — list users in current domain
- `GET    /private/domain/user/{userId}/v1` — view user
- `POST   /private/domain/user/search/v1` — search users in domain

BusinessAccount-User cross-reference:

- `POST   /private/domain/business_account_user/search/v1` — search BA-User mappings
- `POST   /private/domain/business_account_user/count/v1` — count

## Examples

A typical Domain setup for an organisation "Acme":

- `key`: `acme`
- `name`: `Acme Inc.`
- `domainType`: `b2b`
- `defaultI18nLocaleId`: `en`
- `identityProviderId`: acme's OIDC provider
- `attachmentsStorageId`: Acme's MinIO bucket

When an admin creates the first TwinClass in this domain, the class inherits `permissionSchemaId`, `twinflowSchemaId`, and `twinClassSchemaId` from the Domain as defaults.

## Dev notes

- **`domainType` is stored via `DomainTypeConverter`** as a string; resolves to a `DomainType` enum that itself maps to a `DomainTypeEntity` row carrying featurer bindings. Changing the type at runtime has wide effects (initiator featurers, user-group manager).
- **Hstore params** (`businessAccountInitiatorParams`, `domainUserInitiatorParams`, `userGroupManagerParams`) use PostgreSQL `hstore` type — not portable to H2 tests. Mock or skip in unit tests.
- **Materialized storage counters** (`attachmentsStorageUsedCount`, `attachmentsStorageUsedSize`) are updated by background jobs; do not write to them directly.
- **Domain deletion is destructive and not exposed via API** — there is no `DELETE /private/domain/{id}/v1` endpoint. To decommission a Domain, set `domainStatusId` to `DISABLED` and rely on background cleanup jobs.
- **Identity provider binding** (`identityProviderId`) affects authentication — changing it logs out all current users in the Domain.
- **Template Twins** (`businessAccountTemplateTwinId`, `domainUserTemplateTwinId`) drive initial Twin creation when a BA or DomainUser is added. Editing the template affects only new entities, not existing ones.
