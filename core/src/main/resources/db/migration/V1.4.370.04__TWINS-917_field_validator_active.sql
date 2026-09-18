-- TWINS-917: active flag for twin class field validators.
-- Only active validators run during twin create/update field validation
-- (TwinClassFieldValidatorService.collectItems skips inactive ones).
-- Existing rows are backfilled with true — behaviour before the flag existed.

ALTER TABLE twin_class_field_validator ADD COLUMN IF NOT EXISTS active boolean NOT NULL DEFAULT true;
