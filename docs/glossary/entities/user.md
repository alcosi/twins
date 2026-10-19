---
slug: user
title: User
category: multi-tenancy
class: UserEntity
table: user
is_system: true
actualized_at: 2026-06-18
see_also:
  - domain
  - permission
  - business-account
---

# User

## Summary

An authenticated user account. Identified by email (lowercased), has a lifecycle status, belongs to one or more Domains via DomainUser join entities, and gains permissions through group memberships and direct grants.

## Purpose

A User is the human-side counterpart of every action in the platform: every Twin has a creator, every comment has an author, every permission grant has a subject. Users authenticate via the IdentityProvider bound to their Domain (OIDC, SAML, etc.) and the platform resolves their email to a User row on each login.

Users do not directly hold permissions. Instead, permissions flow through three layers: (a) DomainUser membership gives them a baseline role within a Domain; (b) UserGroup memberships (mapped via `UserGroupMapEntity`) inherit group-level grants; (c) direct `PermissionGrantUserEntity` grants override or extend. At request time, the service materialises the user's effective permission set into the `permissions` transient field for fast lookup.

User lifecycle is driven by the `userStatusId` enum (`UserStatus`): `ACTIVE` for normal operation, `BLOCKED` for suspended access, `EMAIL_VERIFICATION_REQUIRED` for unverified registrations, `DELETED` for soft-deleted accounts. Authentication flows check this status and route accordingly (e.g., reject login when BLOCKED, resend verification email when EMAIL_VERIFICATION_REQUIRED).

## Fields

- `id` — primary key (UUID v7)
- `name` — display name
- `email` — unique email address, **normalised to lowercase** in `@PrePersist` / `@PreUpdate`
- `avatar` — avatar URL or blob reference
- `userStatusId` — enum `UserStatus` (`ACTIVE` / `DELETED` / `EMAIL_VERIFICATION_REQUIRED` / `BLOCKED`)
- `createdAt` — creation timestamp

## Relations

| Target | Cardinality | Kind | Description |
|---|---|---|---|
| [Domain](domain.md) | many-to-many | link | Domains the user belongs to (via `DomainUser` join) |
| [BusinessAccount](business-account.md) | many-to-many | link | BusinessAccount memberships (via `BusinessAccountUser` join) |
| UserGroup | many-to-many | link | Group memberships (via `UserGroupMap` join, spec-only `userGroupMapsSpecOnly` on entity) |
| SpaceRoleUser | one-to-many | link | Space-role assignments (spec-only `spaceRoleUsersSpecOnly` on entity) |
| [Twin](twin.md) | one-to-many | link | Twins where this user is creator / assigner / owner |
| [Permission](permission.md) | many-to-many | transient_runtime | Effective permission set (materialised into `permissions` transient field) |
| PermissionSchema | many-to-one | transient_runtime | Detected schema for the user (materialised into `detectedPermissionSchemaId`) |

## API

- `POST   /private/user/v1` — create user (JSON)
- `POST   /private/user/v2` — create (extended)
- `PUT    /private/user/{userId}/v1` — update single user
- `PUT    /private/user/v1` — update current user (self-service)
- `DELETE /private/user/{userId}/v1` — soft-delete user
- `POST   /private/user/search/v1` — search users
- `POST   /private/user/search/{searchId}/v1` — execute saved search
- `GET    /private/user/permission/v1` — list current user's effective permissions
- `GET    /private/user/{userId}/permission/v1` — list a user's effective permissions (admin)
- `GET    /private/user/locale/v1` — get current user's locale preference
- `PUT    /private/user/locale/{localeName}/v1` — set current user's locale preference
- `PUT    /private/user_group/v1` — update user-group membership

## Examples

A typical user record:

```json
{
  "id": "0192a7c4-...",
  "name": "Jane Doe",
  "email": "jane.doe@acme.com",
  "avatar": "https://cdn.acme.com/avatars/jane.png",
  "userStatusId": "ACTIVE",
  "createdAt": "2026-06-01T10:00:00Z"
}
```

## Dev notes

- **Email is normalised to lowercase** in `@PrePersist` and `@PreUpdate` (`normalizeEmail()`). Always query with lowercased email; mixed-case lookups will miss rows.
- **PII masking in logs** — `easyLog()` calls `maskEmail(email)` instead of printing the raw address. Follow this pattern in any new log statement that touches email.
- **`userGroupMapsSpecOnly` / `spaceRoleUsersSpecOnly`** are `@Deprecated` spec-only relations with no getter — never access in business code.
- **`permissions` is `@Transient`** — populated by `AuthService` at request start. Do not assume it's set outside a request scope.
- **Soft-delete via `DELETED` status** — queries must filter out DELETED users explicitly; the platform does not auto-exclude them.
- **User creation is tied to IdentityProvider** — the typical flow is "user logs in via IdP → platform auto-creates User row if missing". Direct creation via `/private/user/v1` is reserved for admin / seeding scenarios.
