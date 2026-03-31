-- V1.4.172.03__TWINS-734_set_twin_permission_schema_id.sql
UPDATE twin
SET permission_schema_id = permission_schema_detect(permission_schema_space_id, owner_business_account_id, twin_class_id);
