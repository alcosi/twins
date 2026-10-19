CREATE INDEX IF NOT EXISTS idx_twin_link_created_at
    ON twin_link (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_twin_attachment_created_at
    ON twin_attachment (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_twin_pointer_created_at
    ON twin_pointer (created_at DESC);

