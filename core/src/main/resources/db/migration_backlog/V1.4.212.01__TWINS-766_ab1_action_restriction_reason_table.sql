-- Add i18n type for action restriction reasons
INSERT INTO i18n_type (id, name) VALUES ('actionRestriction', 'Action restriction description') ON CONFLICT (id) DO NOTHING;
-- Create i18n entry for RESTRICTED_BY_PERMISSION
INSERT INTO i18n (id, name, "key", i18n_type_id) VALUES ('019d3fc2-4683-7351-b95e-dbcdccd99372', 'Action restricted by permission description', null, 'actionRestriction') ON CONFLICT (id) DO NOTHING;
-- Create i18n entry for RESTRICTED_BY_VALIDATOR
INSERT INTO i18n (id, name, "key", i18n_type_id) VALUES ('019d404b-dc5b-79bc-86d0-e3b5136c4f51', 'Action restricted by validator description', null, 'actionRestriction') ON CONFLICT (id) DO NOTHING;
-- Add translations for RESTRICTED_BY_PERMISSION
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('019d3fc2-4683-7351-b95e-dbcdccd99372', 'en', 'Action restricted by permission', DEFAULT) ON CONFLICT DO NOTHING;
-- Add translations for RESTRICTED_BY_VALIDATOR
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('019d404b-dc5b-79bc-86d0-e3b5136c4f51', 'en', 'Action restricted by validator', DEFAULT) ON CONFLICT DO NOTHING;

-- Action restriction reason table
CREATE TABLE IF NOT EXISTS action_restriction_reason
(
    id                    UUID PRIMARY KEY,
    domain_id             UUID       NULL REFERENCES domain (id) ON UPDATE CASCADE ON DELETE CASCADE,
    type                  VARCHAR    NOT NULL,
    description_i18n_id   UUID       NOT NULL REFERENCES i18n (id) ON UPDATE CASCADE ON DELETE CASCADE
);

-- Insert RESTRICTED_BY_PERMISSION action restriction reason
INSERT INTO action_restriction_reason (id, domain_id, type, description_i18n_id) VALUES ('019d3fc1-2608-7e1c-9c25-e266af990ebe', NULL, 'RESTRICTED_BY_PERMISSION', '019d3fc2-4683-7351-b95e-dbcdccd99372') ON CONFLICT (id) DO NOTHING;
-- Insert RESTRICTED_BY_VALIDATOR action restriction reason
INSERT INTO action_restriction_reason (id, domain_id, type, description_i18n_id) VALUES ('019d4047-a639-7c25-a62d-ee43ee4efdf3', NULL, 'RESTRICTED_BY_VALIDATOR', '019d404b-dc5b-79bc-86d0-e3b5136c4f51') ON CONFLICT (id) DO NOTHING;

-- Add action_restriction_reason_id to twin_action_permission
ALTER TABLE twin_action_permission ADD COLUMN IF NOT EXISTS action_restriction_reason_id UUID REFERENCES action_restriction_reason (id) ON UPDATE CASCADE ON DELETE CASCADE;
UPDATE twin_action_permission SET action_restriction_reason_id = '019d3fc1-2608-7e1c-9c25-e266af990ebe' WHERE action_restriction_reason_id IS NULL;
ALTER TABLE twin_action_permission ALTER COLUMN action_restriction_reason_id SET NOT NULL;

-- Add action_restriction_reason_id to twin_action_validator_rule
ALTER TABLE twin_action_validator_rule ADD COLUMN IF NOT EXISTS action_restriction_reason_id UUID REFERENCES action_restriction_reason (id) ON UPDATE CASCADE ON DELETE CASCADE;
UPDATE twin_action_validator_rule SET action_restriction_reason_id = '019d4047-a639-7c25-a62d-ee43ee4efdf3' WHERE action_restriction_reason_id IS NULL;
ALTER TABLE twin_action_validator_rule ALTER COLUMN action_restriction_reason_id SET NOT NULL;
