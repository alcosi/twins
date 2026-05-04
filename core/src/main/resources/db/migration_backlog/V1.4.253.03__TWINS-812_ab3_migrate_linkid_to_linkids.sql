-- Migrate field_typer_params from linkId to linkIds for backward compatibility
-- This migration converts existing single linkId values to linkIds format

UPDATE twin_class_field
SET field_typer_params = delete(
    field_typer_params || hstore('linkIds', field_typer_params->'linkId'),
    'linkId'
)
WHERE field_typer_params ? 'linkId';
