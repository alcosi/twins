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

Procedure names must follow the pattern:

```text
{table_or_entity}_{action}
```

The function name should clearly describe:

* the table or entity being processed,
* and the action being performed.

This naming convention ensures:

* predictable structure,
* easier navigation,
* better readability,
* consistent architecture across the project.

## Examples

```text
twin_class_extends_process_tree_update()
twin_class_permissions_autoupdate()
twin_class_i18n_and_translations_delete()
twin_class_direct_children_counters_update()
twin_attachment_storage_counters_update()
domain_business_account_storage_usage_recalculate()
```

---

# Trigger Function Structure

# INSERT Wrapper Example

```sql
CREATE OR REPLACE FUNCTION twin_class_after_insert_wrapper()
    RETURNS trigger AS $$
BEGIN
    -- Update hierarchy
    PERFORM twin_class_extends_process_tree_update(OLD, NEW, TG_OP);

    -- Validate business rules
    PERFORM twin_class_has_segments_check(NEW.head_twin_class_id);

    -- Update counters
    IF NEW.extends_twin_class_id IS NOT NULL THEN
        PERFORM twin_class_direct_children_counters_update(
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

        PERFORM twin_class_extends_process_tree_update(
            OLD,
            NEW,
            TG_OP
        );

        -- Update old parent
        IF OLD.extends_twin_class_id IS NOT NULL THEN
            PERFORM twin_class_direct_children_counters_update(
                OLD.extends_twin_class_id,
                'extends'
            );
        END IF;

        -- Update new parent
        IF NEW.extends_twin_class_id IS NOT NULL THEN
            PERFORM twin_class_direct_children_counters_update(
                NEW.extends_twin_class_id,
                'extends'
            );
        END IF;
    END IF;

    IF NEW.key IS DISTINCT FROM OLD.key THEN
        PERFORM twin_class_permissions_autoupdate(OLD, NEW);
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
    PERFORM twin_class_i18n_and_translations_delete(OLD);

    -- Update counters
    IF OLD.extends_twin_class_id IS NOT NULL THEN
        PERFORM twin_class_direct_children_counters_update(
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
    PERFORM twin_class_permissions_autoupdate(OLD, NEW);
END IF;
```

---

## Update both sides during reassignment

```sql
IF NEW.parent_id IS DISTINCT FROM OLD.parent_id THEN

    IF OLD.parent_id IS NOT NULL THEN
        PERFORM twin_class_counter_decrement(OLD.parent_id);
    END IF;

    IF NEW.parent_id IS NOT NULL THEN
        PERFORM twin_class_counter_increment(NEW.parent_id);
    END IF;
END IF;
```

---

## Group related operations

```sql
IF NEW.marker_data_list_id
    IS DISTINCT FROM OLD.marker_data_list_id THEN
    PERFORM twin_class_inherited_marker_data_list_update(OLD, NEW);
END IF;

IF NEW.tag_data_list_id
    IS DISTINCT FROM OLD.tag_data_list_id THEN
    PERFORM twin_class_inherited_tag_data_list_update(OLD, NEW);
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
* [ ] Ensure function names follow `{table_or_entity}_{action}`
* [ ] Decide whether helper functions should be `IMMUTABLE`
* [ ] Create or update wrapper function using naming convention
* [ ] Add field-change checks before heavy logic
* [ ] Ensure correct `RETURN NEW` / `RETURN OLD`
* [ ] Create corresponding trigger
* [ ] Test all modification scenarios
* [ ] Verify counters and hierarchy updates during reassignment
