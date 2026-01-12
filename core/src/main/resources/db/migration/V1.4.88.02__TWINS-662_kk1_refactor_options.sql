DROP INDEX IF EXISTS idx_data_list_option_external;

ALTER TABLE data_list_option
ALTER COLUMN external_id TYPE VARCHAR(2048);

CREATE INDEX IF NOT EXISTS idx_data_list_option_external_hash
    ON data_list_option USING HASH (external_id);