-- twin_attachment.storage_file_key holds full external URIs and can exceed 255 chars.
-- twin_attachment_delete_task copied varchar(255) from the original attachment schema,
-- so the after-delete trigger fails when enqueueing file cleanup.

ALTER TABLE twin_attachment
    ALTER COLUMN storage_file_key TYPE varchar;

ALTER TABLE twin_attachment_delete_task
    ALTER COLUMN storage_file_key TYPE varchar;

ALTER TABLE draft_twin_attachment
    ALTER COLUMN storage_file_key TYPE varchar;
