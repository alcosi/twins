-- data_list_projection: mapping between source and destination data lists
CREATE TABLE IF NOT EXISTS data_list_projection (
    id uuid PRIMARY KEY,
    src_data_list_id uuid NOT NULL REFERENCES data_list(id) ON UPDATE CASCADE ON DELETE CASCADE,
    dst_data_list_id uuid NOT NULL REFERENCES data_list(id) ON UPDATE CASCADE ON DELETE CASCADE,
    name varchar(40),
    saved_by_user_id uuid NOT NULL REFERENCES "user"(id) ON UPDATE CASCADE ON DELETE CASCADE,
    changed_at timestamp default CURRENT_TIMESTAMP,
    CONSTRAINT data_list_projection_src_dst_unique UNIQUE (src_data_list_id, dst_data_list_id)
);

-- Helpful indexes
CREATE INDEX IF NOT EXISTS data_list_projection_src_idx ON data_list_projection (src_data_list_id);
CREATE INDEX IF NOT EXISTS data_list_projection_dst_idx ON data_list_projection (dst_data_list_id);


-- data_list_option_projection: mapping between options inside a specific data_list_projection
CREATE TABLE IF NOT EXISTS data_list_option_projection (
    id uuid PRIMARY KEY,
    data_list_projection_id uuid NOT NULL REFERENCES data_list_projection(id) ON UPDATE CASCADE ON DELETE CASCADE,
    src_data_list_option_id uuid NOT NULL REFERENCES data_list_option(id) ON UPDATE CASCADE ON DELETE CASCADE,
    dst_data_list_option_id uuid NOT NULL REFERENCES data_list_option(id) ON UPDATE CASCADE ON DELETE CASCADE,
    saved_by_user_id uuid NOT NULL REFERENCES "user"(id) ON UPDATE CASCADE ON DELETE CASCADE,
    changed_at timestamp default CURRENT_TIMESTAMP,
    CONSTRAINT data_list_option_projection_triple_unique UNIQUE (data_list_projection_id, src_data_list_option_id, dst_data_list_option_id)
);

-- Helpful indexes
CREATE INDEX IF NOT EXISTS data_list_option_projection_proj_idx ON data_list_option_projection (data_list_projection_id);
CREATE INDEX IF NOT EXISTS data_list_option_projection_src_opt_idx ON data_list_option_projection (src_data_list_option_id);
CREATE INDEX IF NOT EXISTS data_list_option_projection_dst_opt_idx ON data_list_option_projection (dst_data_list_option_id);
CREATE UNIQUE INDEX IF NOT EXISTS data_list_option_projection_proj_src_unique_idx ON data_list_option_projection (data_list_projection_id, src_data_list_option_id);
