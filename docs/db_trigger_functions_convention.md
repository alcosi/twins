# Guide to Working with Triggers and Trigger Functions in TWINS

# Overview

The TWINS project uses the **Wrapper Functions** pattern for working with triggers.

This approach allows:

* centralized business logic management,
* modular trigger architecture,
* easy extension and maintenance,
* safer database evolution.

Instead of placing business logic directly inside triggers, wrapper functions delegate execution to specialized stored procedures.

---

# Core Concepts

# 1. Wrapper Functions

A **wrapper function** is a trigger function that does not contain business logic directly.

Instead, it:

* orchestrates execution,
* calls specialized procedures,
* validates changes,
* coordinates side effects.

## Principles

* One wrapper function per operation (`INSERT` / `UPDATE` / `DELETE`) per table
* Business logic is delegated to dedicated procedures
* Logic can be added or removed without recreating triggers
* Wrapper functions remain small and readable

---

# 2. When to Use `IMMUTABLE`

`IMMUTABLE` is a PostgreSQL function attribute indicating that a function:

* always returns the same result for the same arguments,
* does not read database tables,
* does not modify database state,
* does not depend on external state.

---

# When `IMMUTABLE` Should Be Used

## ✅ Correct — Pure Data Transformation

```sql
CREATE OR REPLACE FUNCTION safe_cast_to_double(value TEXT)
    RETURNS DOUBLE PRECISION AS $$
BEGIN
    RETURN CASE
        WHEN value IS NULL THEN NULL
        WHEN value ~ '^[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?$'
            THEN CAST(value AS DOUBLE PRECISION)
    END;
END;
$$ LANGUAGE plpgsql IMMUTABLE;
```

---

## ✅ Correct — Pure Validation Logic

```sql
CREATE OR REPLACE FUNCTION check_twin_status_filter(
    twin_status_id UUID,
    status_ids UUID[],
    exclude BOOLEAN
) RETURNS BOOLEAN AS $$
BEGIN
    IF status_ids IS NULL OR array_length(status_ids, 1) = 0 THEN
        RETURN TRUE;
    END IF;

    IF exclude = false THEN
        RETURN twin_status_id = ANY(status_ids);
    ELSE
        RETURN twin_status_id != ALL(status_ids);
    END IF;
END;
$$ LANGUAGE plpgsql IMMUTABLE;
```

---

## ❌ Incorrect — Reads Database Tables

```sql
CREATE OR REPLACE FUNCTION get_class_name(class_id UUID)
    RETURNS VARCHAR AS $$
BEGIN
    SELECT name INTO result
    FROM twin_class
    WHERE id = class_id;

    RETURN result;
END;
$$ LANGUAGE plpgsql;
```

This function **must not** be `IMMUTABLE` because it reads database data.

---

# Benefits of `IMMUTABLE`

* PostgreSQL can cache results
* Query planner can optimize execution better
* Functions can be used in indexes
* Improves overall query performance

---

# Naming Conventions

# Trigger Functions (Wrapper Functions)

Pattern:

```text
{table_name}_{before|after}_{insert|update|delete}_wrapper
```

## Examples

```text
twin_class_after_insert_wrapper()
twin_class_after_update_wrapper()
twin_class_after_delete_wrapper()
twin_class_before_insert_wrapper()
```

---

# Triggers

Pattern:

```text
{table_name}_{before|after}_{insert|update|delete}_wrapper_trigger
```

## Examples

```text
twin_class_after_insert_wrapper_trigger
twin_class_after_update_wrapper_trigger
twin_class_after_delete_wrapper_trigger
```

---

# Stored Procedures Called from Wrappers

Procedure names should clearly describe the performed action.

Pattern:

```text
{action}_{entity}_{details}
```

## Examples

```text
hierarchy_twin_class_extends_process_tree_update()
permissions_autoupdate_on_twin_class_update()
twin_class_on_delete_i18n_and_translations_delete()
update_direct_children_counters()
```

---

# Trigger Function Structure

# INSERT Wrapper Example

```sql
CREATE OR REPLACE FUNCTION twin_class_after_insert_wrapper()
    RETURNS trigger AS $$
BEGIN
    -- Update hierarchy
    PERFORM hierarchy_twin_class_extends_process_tree_update(OLD, NEW, TG_OP);

    -- Validate business rules
    PERFORM twin_class_has_segments_check(NEW.head_twin_class_id);

    -- Update counters
    IF NEW.extends_twin_class_id IS NOT NULL THEN
        PERFORM update_direct_children_counters(
            NEW.extends_twin_class_id,
            'extends'
        );
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

---

# UPDATE Wrapper Example

```sql
CREATE OR REPLACE FUNCTION twin_class_after_update_wrapper()
    RETURNS trigger AS $$
BEGIN
    IF NEW.extends_twin_class_id IS DISTINCT FROM OLD.extends_twin_class_id THEN

        PERFORM hierarchy_twin_class_extends_process_tree_update(
            OLD,
            NEW,
            TG_OP
        );

        -- Update old parent
        IF OLD.extends_twin_class_id IS NOT NULL THEN
            PERFORM update_direct_children_counters(
                OLD.extends_twin_class_id,
                'extends'
            );
        END IF;

        -- Update new parent
        IF NEW.extends_twin_class_id IS NOT NULL THEN
            PERFORM update_direct_children_counters(
                NEW.extends_twin_class_id,
                'extends'
            );
        END IF;
    END IF;

    IF NEW.key IS DISTINCT FROM OLD.key THEN
        PERFORM permissions_autoupdate_on_twin_class_update(OLD, NEW);
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

---

# DELETE Wrapper Example

```sql
CREATE OR REPLACE FUNCTION twin_class_after_delete_wrapper()
    RETURNS trigger AS $$
BEGIN
    -- Cleanup related data
    PERFORM twin_class_on_delete_i18n_and_translations_delete(OLD);

    -- Update counters
    IF OLD.extends_twin_class_id IS NOT NULL THEN
        PERFORM update_direct_children_counters(
            OLD.extends_twin_class_id,
            'extends'
        );
    END IF;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;
```

---

# Creating a Trigger

## Template

```sql
CREATE TRIGGER twin_class_after_insert_wrapper_trigger
    AFTER INSERT ON twin_class
    FOR EACH ROW
    EXECUTE FUNCTION twin_class_after_insert_wrapper();
```

---

# Important Notes

# Trigger Timing

| Timing   | Purpose                                  |
| -------- | ---------------------------------------- |
| `BEFORE` | Validation and modification before write |
| `AFTER`  | Post-processing after successful write   |

---

# Conditional Trigger Execution

## Trigger only for specific field changes

```sql
CREATE TRIGGER trigger_name
    AFTER UPDATE OF extends_twin_class_id, head_twin_class_id
    ON twin_class
    FOR EACH ROW
    EXECUTE FUNCTION function_name();
```

---

## Trigger for every UPDATE

```sql
CREATE TRIGGER trigger_name
    AFTER UPDATE ON twin_class
    FOR EACH ROW
    EXECUTE FUNCTION function_name();
```

---

# Real Project Example — `twin_class`

## Example 1 — Updating Direct Children Counters

Migration:

```text
V1.4.130.01__TWINS-704_kk1_class_count_children.sql
```

---

# Step 1 — Create Counter Update Procedure

```sql
CREATE OR REPLACE FUNCTION update_direct_children_counters(
    p_parent_id UUID,
    p_hierarchy_type TEXT
) RETURNS VOID AS $$
DECLARE
    v_direct_children_count INT;
BEGIN
    IF p_hierarchy_type = 'extends' THEN

        SELECT COUNT(*)
        INTO v_direct_children_count
        FROM twin_class
        WHERE extends_twin_class_id = p_parent_id;

        UPDATE twin_class
        SET extends_hierarchy_counter_direct_children =
            COALESCE(v_direct_children_count, 0)
        WHERE id = p_parent_id;

    ELSIF p_hierarchy_type = 'head' THEN

        SELECT COUNT(*)
        INTO v_direct_children_count
        FROM twin_class
        WHERE head_twin_class_id = p_parent_id;

        UPDATE twin_class
        SET head_hierarchy_counter_direct_children =
            COALESCE(v_direct_children_count, 0)
        WHERE id = p_parent_id;

    END IF;
END;
$$ LANGUAGE plpgsql;
```

---

# Step 2 — Update INSERT Wrapper

```sql
CREATE OR REPLACE FUNCTION twin_class_after_insert_wrapper()
    RETURNS trigger AS $$
BEGIN
    PERFORM hierarchy_twin_class_extends_process_tree_update(
        OLD,
        NEW,
        TG_OP
    );

    PERFORM twin_class_has_segments_check(NEW.head_twin_class_id);

    IF NEW.extends_twin_class_id IS NOT NULL THEN
        PERFORM update_direct_children_counters(
            NEW.extends_twin_class_id,
            'extends'
        );
    END IF;

    IF NEW.head_twin_class_id IS NOT NULL THEN
        PERFORM update_direct_children_counters(
            NEW.head_twin_class_id,
            'head'
        );
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

---

# Step 3 — Update UPDATE Wrapper

```sql
CREATE OR REPLACE FUNCTION twin_class_after_update_wrapper()
    RETURNS trigger AS $$
BEGIN
    IF NEW.extends_twin_class_id IS DISTINCT FROM OLD.extends_twin_class_id THEN

        IF OLD.extends_twin_class_id IS NOT NULL THEN
            PERFORM update_direct_children_counters(
                OLD.extends_twin_class_id,
                'extends'
            );
        END IF;

        IF NEW.extends_twin_class_id IS NOT NULL THEN
            PERFORM update_direct_children_counters(
                NEW.extends_twin_class_id,
                'extends'
            );
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

---

# Example 2 — Attachment Processing (`twin_attachment`)

Migration:

```text
V1.4.33.01__TWINS-598_ec1_attachment_triggers.sql
```

---

# INSERT Wrapper

```sql
CREATE OR REPLACE FUNCTION twin_attachment_after_insert_wrapper()
    RETURNS trigger AS $$
BEGIN
    -- Update BusinessAccount counters
    UPDATE domain_business_account dba
    SET attachments_storage_used_count =
            attachments_storage_used_count + 1,
        attachments_storage_used_size =
            attachments_storage_used_size + NEW.size
    FROM twin te
    WHERE te.id = NEW.twin_id
      AND dba.business_account_id = te.owner_business_account_id;

    -- Update Domain counters
    UPDATE domain d
    SET attachments_storage_used_count =
            attachments_storage_used_count + 1,
        attachments_storage_used_size =
            attachments_storage_used_size + NEW.size
    FROM twin te
    JOIN twin_class tc ON te.twin_class_id = tc.id
    WHERE te.id = NEW.twin_id
      AND d.id = tc.domain_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

---

# DELETE Wrapper

```sql
CREATE OR REPLACE FUNCTION twin_attachment_after_delete_wrapper()
    RETURNS trigger AS $$
BEGIN
    -- Decrease BusinessAccount counters
    UPDATE domain_business_account dba
    SET attachments_storage_used_count =
            attachments_storage_used_count - 1,
        attachments_storage_used_size =
            attachments_storage_used_size - OLD.size
    FROM twin te
    WHERE te.id = OLD.twin_id
      AND dba.business_account_id = te.owner_business_account_id;

    -- Decrease Domain counters
    UPDATE domain d
    SET attachments_storage_used_count =
            attachments_storage_used_count - 1,
        attachments_storage_used_size =
            attachments_storage_used_size - OLD.size
    FROM twin te
    JOIN twin_class tc ON te.twin_class_id = tc.id
    WHERE te.id = OLD.twin_id
      AND d.id = tc.domain_id;

    -- Create storage cleanup task
    INSERT INTO twin_attachment_delete_task(
        id,
        twin_attachment_id,
        storage_id,
        storage_file_key,
        status,
        created_at
    )
    VALUES (
        uuid_generate_v4(),
        OLD.id,
        OLD.storage_id,
        OLD.storage_file_key,
        'NEED_START',
        now()
    );

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;
```

---

# Passing Parameters to Procedures

# Standard Trigger Variables

Wrapper functions have access to:

| Variable | Description            |
| -------- | ---------------------- |
| `OLD`    | Previous row value     |
| `NEW`    | New row value          |
| `TG_OP`  | Trigger operation type |

---

# Procedure Invocation Styles

## Pass entire OLD and NEW

```sql
PERFORM hierarchy_twin_class_extends_process_tree_update(
    OLD,
    NEW,
    TG_OP
);
```

---

## Pass specific fields

```sql
PERFORM update_direct_children_counters(
    NEW.extends_twin_class_id,
    'extends'
);
```

---

# Development Recommendations

# ✅ Best Practices

## Use `IS DISTINCT FROM` for NULL-safe comparison

```sql
IF NEW.extends_twin_class_id
    IS DISTINCT FROM OLD.extends_twin_class_id THEN
    -- update logic
END IF;
```

---

## Avoid heavy operations unless fields changed

```sql
IF NEW.key IS DISTINCT FROM OLD.key THEN
    PERFORM permissions_autoupdate_on_twin_class_update(OLD, NEW);
END IF;
```

---

## Update both sides during reassignment

```sql
IF NEW.parent_id IS DISTINCT FROM OLD.parent_id THEN

    IF OLD.parent_id IS NOT NULL THEN
        PERFORM decrement_counter(OLD.parent_id);
    END IF;

    IF NEW.parent_id IS NOT NULL THEN
        PERFORM increment_counter(NEW.parent_id);
    END IF;
END IF;
```

---

## Group related operations

```sql
IF NEW.marker_data_list_id
    IS DISTINCT FROM OLD.marker_data_list_id THEN
    PERFORM twin_class_update_inherited_marker_data_list(OLD, NEW);
END IF;

IF NEW.tag_data_list_id
    IS DISTINCT FROM OLD.tag_data_list_id THEN
    PERFORM twin_class_update_inherited_tag_data_list(OLD, NEW);
END IF;
```

---

# ❌ Bad Practices

## Incorrect NULL comparison

```sql
-- BAD
IF NEW.parent_id != OLD.parent_id THEN
END IF;

-- GOOD
IF NEW.parent_id IS DISTINCT FROM OLD.parent_id THEN
END IF;
```

---

## Forgetting RETURN

```sql
-- BAD
CREATE OR REPLACE FUNCTION bad_wrapper()
RETURNS trigger AS $$
BEGIN
    PERFORM some_procedure();
END;
$$ LANGUAGE plpgsql;
```

```sql
-- GOOD
CREATE OR REPLACE FUNCTION good_wrapper()
RETURNS trigger AS $$
BEGIN
    PERFORM some_procedure();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

---

## Running heavy logic on every UPDATE

```sql
-- BAD
PERFORM very_heavy_operation();
```

```sql
-- GOOD
IF NEW.some_field IS DISTINCT FROM OLD.some_field THEN
    PERFORM very_heavy_operation();
END IF;
```

---

# Migration Strategy

When changing wrapper functions, migrations should include:

```sql
-- 1. Drop old triggers
DROP TRIGGER IF EXISTS twin_class_after_insert_wrapper_trigger
    ON twin_class;

DROP TRIGGER IF EXISTS twin_class_after_update_wrapper_trigger
    ON twin_class;

DROP TRIGGER IF EXISTS twin_class_after_delete_wrapper_trigger
    ON twin_class;

-- 2. Recreate wrapper functions
CREATE OR REPLACE FUNCTION twin_class_after_insert_wrapper()
RETURNS trigger AS $$
    -- function body
$$ LANGUAGE plpgsql;

-- 3. Recreate triggers
CREATE TRIGGER twin_class_after_insert_wrapper_trigger
    AFTER INSERT ON twin_class
    FOR EACH ROW
    EXECUTE FUNCTION twin_class_after_insert_wrapper();
```

---

# Full Checklist for New Trigger Logic

* [ ] Determine required operations (`INSERT`, `UPDATE`, `DELETE`)
* [ ] Create dedicated stored procedures if needed
* [ ] Decide whether helper functions should be `IMMUTABLE`
* [ ] Create or update wrapper function using naming convention
* [ ] Add field-change checks before heavy logic
* [ ] Ensure correct `RETURN NEW` / `RETURN OLD`
* [ ] Create corresponding trigger
* [ ] Test all modification scenarios
* [ ] Verify counters and hierarchy updates during reassignment
