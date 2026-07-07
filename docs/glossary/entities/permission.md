---
slug: permission
title: Permission
category: permissions
class: PermissionEntity
table: permission
is_system: true
actualized_at: 2026-06-18
see_also:
  - user
  - user-group
  - permission-schema
  - twin-class
---

# Permission

## Summary

An atomic permission — a single named capability (e.g., `TWIN_CREATE`, `TWINCLASS_UPDATE`) that can be granted to subjects (users, user-groups, space-roles, twin-roles) within a scope (global, space, twin). Permissions are grouped into PermissionGroups for UI organisation.

## Purpose

Every protected operation in the platform — creating a Twin, updating a TwinClass, performing a TwinflowTransition, exporting SQL — checks one or more permissions before executing. Permissions are the atomic unit of authorisation: they cannot be subdivided, only granted or denied.

Permissions themselves are just metadata (a key, a group, i18n-translated name/description). The actual grants live in five separate join tables, one per subject type:

- `PermissionGrantGlobalEntity` — domain-wide grants
- `PermissionGrantUserEntity` — per-user grants
- `PermissionGrantUserGroupEntity` — per-group grants
- `PermissionGrantSpaceRoleEntity` — per-space-role grants
- `PermissionGrantTwinRoleEntity` — per-twin-role grants

At request time, `AuthService` walks these grant tables for the current user and materialises the effective permission-set into `UserEntity.permissions` (transient). Controllers and services gate operations by checking membership of that set.

Permission keys are stable identifiers used in `@ProtectedBy(Permissions.X)` annotations on controllers and in `validate*` checks in services. Adding a new permission requires both a DB row (for grant management) and a Java enum constant (for compile-time references).

## Fields

- `id` — primary key (UUID v7)
- `key` — unique permission key (e.g., `TWIN_CREATE`, `TWINCLASS_UPDATE`)
- `permissionGroupId` — FK to [PermissionGroup](permission-group.md), grouping for UI
- `nameI18nId` — FK to [I18n](i18n.md), display name translations
- `descriptionI18nId` — FK to [I18n](i18n.md), description translations

## Relations

| Target | Cardinality | Kind | Description |
|---|---|---|---|
| [PermissionGroup](permission-group.md) | many-to-one | owning | UI grouping |
| PermissionGrantGlobal | one-to-many | link | Domain-wide grants of this permission |
| PermissionGrantUser | one-to-many | link | Per-user grants |
| PermissionGrantUserGroup | one-to-many | link | Per-group grants |
| PermissionGrantSpaceRole | one-to-many | link | Per-space-role grants |
| PermissionGrantTwinRole | one-to-many | link | Per-twin-role grants |
| [I18n](i18n.md) | many-to-one | owning | Display name translations |
| [I18n](i18n.md) | many-to-one | owning | Description translations |
| [TwinClass](twin-class.md) | one-to-many | link | Classes using this as default view permission |
| [TwinClass](twin-class.md) | one-to-many | link | Classes using this as create permission |
| [TwinflowTransition](twinflow-transition.md) | one-to-many | link | Transitions gated by this permission |
| [User](user.md) | many-to-many | transient_runtime | Users holding this permission (via effective-permissions materialisation) |

## API

Permission CRUD:

- `POST   /private/permission/v1` — create permission
- `POST   /private/permission/{permissionId}/v1` — update permission (note: POST on update path)
- `GET    /private/permission/{permissionId}/v1` — view single
- `GET    /private/permission_by_key/{permissionKey}/v1` — lookup by key
- `POST   /private/permission/search/v1` — search permissions

PermissionGroup:

- `GET    /private/permission_group/{groupId}/v1` — view single group
- `GET    /private/permission_group_by_key/{groupKey}/v1` — lookup group by key
- `POST   /private/permission_group/search/v1` — search groups

PermissionSchema:

- `GET    /private/permission_schema/{schemaId}/v1` — view single schema
- `POST   /private/permission_schema/search/v1` — search schemas

Per-user grants:

- `POST   /private/permission_grant/user/v1` — create grant
- `POST   /private/permission_grant/user/{grantId}/v1` — update grant
- `DELETE /private/permission_grant/user/{permissionGrantUserId}/v1` — delete grant
- `GET    /private/permission_grant/space_role/{grantId}/v1` — view grant (space_role variant)
- `POST   /private/permission_grant/user/search/v1` — search grants

Per-user-group grants:

- `POST   /private/permission_grant/user_group/v1` — create
- `PUT    /private/permission_grant/user_group/{permissionGrantUserGroupId}/v1` — update
- `DELETE /private/permission_grant/user_group/{permissionGrantUserGroupId}/v1` — delete
- `POST   /private/permission_grant/user_group/search/v1` — search

Per-space-role grants:

- `POST   /private/permission_grant/space_role/v1` — create
- `PUT    /private/permission_grant/space_role/{permissionGrantSpaceRoleId}/v1` — update
- `DELETE /private/permission_grant/space_role/{permissionGrantSpaceRoleId}/v1` — delete
- `POST   /private/permission_grant/space_role/search/v1` — search

Per-twin-role grants:

- `POST   /private/permission_grant/twin_role/v1` — create
- `PUT    /private/permission_grant/twin_role/{permissionGrantTwinRoleId}/v1` — update
- `DELETE /private/permission_grant/twin_role/{permissionGrantTwinRoleId}/v1` — delete
- `POST   /private/permission_grant/twin_role/search/v1` — search

User-facing:

- `GET    /private/user/permission/v1` — list current user's effective permissions

## Examples

A typical permission definition:

```json
{
  "id": "...",
  "key": "TWIN_CREATE",
  "permissionGroupId": "...",
  "nameI18nId": "...",
  "descriptionI18nId": "..."
}
```

When a controller method carries `@ProtectedBy(Permissions.TWIN_CREATE)`, the platform resolves the current user's `permissions` set and verifies `TWIN_CREATE` is present.

## Dev notes

- **Adding a new permission requires both DB and Java changes**: insert a row in the `permission` table (typically via Flyway migration) AND add a constant to the `Permissions` enum used by `@ProtectedBy`. The two must stay in sync.
- **Five grant tables mean five places to check** when auditing user capabilities — `PermissionGrantGlobalEntity`, `PermissionGrantUserEntity`, `PermissionGrantUserGroupEntity`, `PermissionGrantSpaceRoleEntity`, `PermissionGrantTwinRoleEntity`. `AuthService` handles this; never query grants directly in business code.
- **`nameI18nSpecOnly` / `descriptionI18nSpecOnly`** are deprecated spec-only fields with `@Getter(AccessLevel.NONE)`. Use runtime-resolved I18n values.
- **Permission keys are stable** — renaming a key breaks every `@ProtectedBy` annotation referencing it. Add new permissions; do not rename existing ones.
- **Permission update uses POST, not PUT** — `POST /private/permission/{permissionId}/v1` is the update path. This is a known API inconsistency; do not "fix" it without coordinating with clients.
