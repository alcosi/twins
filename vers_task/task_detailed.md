# Domain Versioning and Migration Task

## Overview

Implement a domain versioning system to facilitate fast and reliable import/export of domain-specific configurations
between environments (e.g., Dev -> Stage -> Prod). The system will track configuration changes, generate version
snapshots, and handle migration logic.

## Goal

Enable "Snapshot" based versioning where a specific state of domain settings can be exported as a file and imported into
another environment, ensuring consistency and handling deletions.

## Key Concepts

### 1. Domain Version (`domain_version`)

A snapshot of the domain's configuration at a specific point in time.

- **Attributes**:
    - `id`: UUID
    - `domain_id`: Link to the domain
    - `version`: Version string (e.g., "1.0", "1.1")
    - `hash`: Checksum of the configuration state (to detect changes)
    - `json_file`: The actual exported configuration (optional storage)
    - `created_at`, `released_at`: Timestamps

### 2. Domain Setting Entities

Any entity that stores configuration data (e.g., `TwinClassEntity`, `TwinStatusEntity`, `PermissionEntity`,
`TwinFlowEntity`) is considered a "Setting".

- **Requirement**: Every setting entity must have a `domain_version_id` field.
- **Logic**: This field indicates which version the specific setting record belongs to.

### 3. Ghost/Draft Mode (`domain_version_ghost`)

Allows tracking of who is working on what configuration changes.

- **Table**: `domain_version_ghost` (domain_id, user_id, table_name).
- **Purpose**: To manage concurrent edits or "draft" states before a version is finalized.

## Workflow

### A. Editing Settings

When a user modifies a setting (e.g., changes a Twin Class):

1. The system identifies the "Current Active Version" (or creates a new Draft Version if none exists).
2. The modified record's `domain_version_id` is updated to this Current/Draft Version ID.
3. This marks the record as "touched" in the current draft.

### B. Export Process

When triggering an export:

1. **Promote**: Update `domain_version_id` for all *currently active* settings to the new Snapshot Version ID.
2. **Snapshot**: Collect all settings (marked with `@DomainSetting`).
3. **Hash**: Calculate a hash of the entire configuration set.
4. **Verify**: If the hash matches the previous version, no new version is needed (no changes).
5. **Output**: Generate a JSON export file containing all settings.
6. **Cycle**: Create a new "Draft" version for future edits.

### C. Import Process

When importing a file:

1. **Backup**: (Optional) Create a backup of current settings.
2. **Apply**: Import settings from JSON.
3. **Cleanup**: Identify settings in the DB that have a `domain_version_id` *older* than the imported version and were
   *not* present in the import file. These represent deleted settings and should be removed.

## Implementation Architecture

### 1. Annotations

- `@DomainSetting`: Class-level annotation for Entities (e.g., `TwinClassEntity`).
- `@DomainSettingField`: Field-level annotation for properties included in the version hash/export.
- `@DomainSettingExclude`: Field-level annotation to explicitly exclude fields (e.g., internal IDs, timestamps,
  unrelated flags).

### 2. DTO Layer

- Create a DTO for each `@DomainSetting` entity (1-to-1 mapping initially).
- **Structure**: `TwinClassDTO`, `PermissionDTO`, etc.

### 3. Service/Mapper Layer

- **Mappers**: Convert Entity <-> DTO.
- **Service**:
    - `SnapshotService`: Handles hashing, DTO collection, and file generation.
    - `ImportService`: Handles parsing, entity updating, and deletion logic.
    - `VersioningService`: Manages `domain_version` table and `domain_version_id` assignments.

### 4. Database Changes

- Create `domain_version` table.
- Create `domain_version_ghost` table.
- Add `domain_version_id` column to all configuration tables:
    - `twin_class`
    - `twin_status`
    - `permission`
    - `twinflow`
    - `datalist`
    - (And others identified in `TWINS-458` analysis)

## Next Steps

1. Finalize list of "Setting" entities.
2. Create DB Migrations (Liquibase/Flyway) for new tables and columns.
3. Implement Annotations and Base DTOs.
4. Implement `VersioningService` for basic version management.
