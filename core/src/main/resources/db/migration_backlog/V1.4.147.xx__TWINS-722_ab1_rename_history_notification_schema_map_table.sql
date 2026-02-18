-- Rename history_notification_schema_map to history_notification
DO $$
    BEGIN
        -- Rename table
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'history_notification_schema_map') THEN
            ALTER TABLE history_notification_schema_map RENAME TO history_notification;
        END IF;

        -- Rename primary key constraint
        IF EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'history_notification_schema_map_pk' AND table_name = 'history_notification') THEN
            ALTER TABLE history_notification RENAME CONSTRAINT history_notification_schema_map_pk TO history_notification_pk;
        END IF;

        -- Rename foreign key constraints
        IF EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'history_notification_schema_map_twin_class_id_id_fk' AND table_name = 'history_notification') THEN
            ALTER TABLE history_notification RENAME CONSTRAINT history_notification_schema_map_twin_class_id_id_fk TO history_notification_twin_class_id_fk;
        END IF;

        IF EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'history_notification_schema_map_twin_class_field_id_fk' AND table_name = 'history_notification') THEN
            ALTER TABLE history_notification RENAME CONSTRAINT history_notification_schema_map_twin_class_field_id_fk TO history_notification_twin_class_field_id_fk;
        END IF;

        IF EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'history_notification_schema_map_history_type_id_fk' AND table_name = 'history_notification') THEN
            ALTER TABLE history_notification RENAME CONSTRAINT history_notification_schema_map_history_type_id_fk TO history_notification_history_type_id_fk;
        END IF;

        IF EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'history_notification_schema_map_notification_schema_id_fk' AND table_name = 'history_notification') THEN
            ALTER TABLE history_notification RENAME CONSTRAINT history_notification_schema_map_notification_schema_id_fk TO history_notification_notification_schema_id_fk;
        END IF;

        IF EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'history_notification_schema_map_recipient_id_fk' AND table_name = 'history_notification') THEN
            ALTER TABLE history_notification RENAME CONSTRAINT history_notification_schema_map_recipient_id_fk TO history_notification_recipient_id_fk;
        END IF;

        IF EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'history_notification_schema_map_channel_event_id_fk' AND table_name = 'history_notification') THEN
            ALTER TABLE history_notification RENAME CONSTRAINT history_notification_schema_map_channel_event_id_fk TO history_notification_notification_channel_event_id_fk;
        END IF;

        IF EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'history_notification_schema_map_twin_validator_set_id_fk' AND table_name = 'history_notification') THEN
            ALTER TABLE history_notification RENAME CONSTRAINT history_notification_schema_map_twin_validator_set_id_fk TO history_notification_twin_validator_set_id_fk;
        END IF;

        -- Rename unique constraint
        IF EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'history_notification_schema_map_uq' AND table_name = 'history_notification') THEN
            ALTER TABLE history_notification RENAME CONSTRAINT history_notification_schema_map_uq TO history_notification_uq;
        END IF;
    END $$;


-- Create indexes for all FK columns
CREATE INDEX IF NOT EXISTS idx_history_notification_twin_class_id ON history_notification(twin_class_id);
CREATE INDEX IF NOT EXISTS idx_history_notification_twin_class_field_id ON history_notification(twin_class_field_id);
CREATE INDEX IF NOT EXISTS idx_history_notification_history_type_id ON history_notification(history_type_id);
CREATE INDEX IF NOT EXISTS idx_history_notification_notification_schema_id ON history_notification(notification_schema_id);
CREATE INDEX IF NOT EXISTS idx_history_notification_history_notification_recipient_id ON history_notification(history_notification_recipient_id);
CREATE INDEX IF NOT EXISTS idx_history_notification_notification_channel_event_id ON history_notification(notification_channel_event_id);
CREATE INDEX IF NOT EXISTS idx_history_notification_twin_validator_set_id ON history_notification(twin_validator_set_id);

-- Add audit fields
ALTER TABLE history_notification ADD COLUMN IF NOT EXISTS created_by_user_id uuid;
ALTER TABLE history_notification ADD COLUMN IF NOT EXISTS created_at timestamp default CURRENT_TIMESTAMP;