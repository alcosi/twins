CREATE INDEX IF NOT EXISTS idx_twin_attachment_external_id ON twin_attachment(external_id);
CREATE INDEX IF NOT EXISTS idx_twin_attachment_size ON twin_attachment(size);
CREATE INDEX IF NOT EXISTS idx_twin_attachment_order ON twin_attachment("order");
CREATE INDEX IF NOT EXISTS idx_twin_attachment_created_at ON twin_attachment(created_at);
