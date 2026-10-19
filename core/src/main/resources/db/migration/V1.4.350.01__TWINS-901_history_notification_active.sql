-- add active flag to history_notification
-- inactive (active = false) notifications are skipped during the notification task processing
-- existing rows stay active by default
ALTER TABLE history_notification
    ADD COLUMN IF NOT EXISTS active boolean default true not null;

-- index to support search/filtering by the active flag
CREATE INDEX IF NOT EXISTS idx_history_notification_active ON history_notification(active);
