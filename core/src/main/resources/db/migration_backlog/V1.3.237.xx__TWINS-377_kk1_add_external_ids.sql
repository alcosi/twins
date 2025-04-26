ALTER TABLE twin_class ADD COLUMN IF NOT EXISTS external_id VARCHAR(255);
CREATE INDEX IF NOT EXISTS idx_twin_class_domain_external ON twin_class(domain_id, external_id);

ALTER TABLE twin_class_field ADD COLUMN IF NOT EXISTS external_id VARCHAR(255);
CREATE INDEX IF NOT EXISTS idx_twin_class_field_external ON twin_class_field(external_id);

ALTER TABLE data_list ADD COLUMN IF NOT EXISTS external_id VARCHAR(255);
CREATE INDEX IF NOT EXISTS idx_data_list_domain_external ON data_list(domain_id, external_id);

ALTER TABLE data_list_option ADD COLUMN IF NOT EXISTS external_id VARCHAR(255);
CREATE INDEX IF NOT EXISTS idx_data_list_option_external ON data_list_option(external_id);