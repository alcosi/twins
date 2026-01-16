CREATE TABLE IF NOT EXISTS data_list_option_search (
    id UUID PRIMARY KEY,
    domain_id UUID NOT NULL REFERENCES domain(id),
    name VARCHAR,
    data_list_option_sorter_featurer_id INTEGER NOT NULL REFERENCES featurer(id),
    data_list_option_sorter_params HSTORE,
    force_sorting BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS data_list_option_search_predicate (
    id UUID PRIMARY KEY,
    data_list_option_search_id UUID NOT NULL REFERENCES data_list_option_search(id) ON DELETE CASCADE,
    data_list_option_finder_featurer_id INTEGER NOT NULL REFERENCES featurer(id),
    data_list_option_finder_params HSTORE
);

CREATE INDEX IF NOT EXISTS idx_data_list_option_search_domain_id
    ON data_list_option_search(domain_id);

CREATE INDEX IF NOT EXISTS idx_data_list_option_search_sorter_featurer_id
    ON data_list_option_search(data_list_option_sorter_featurer_id);

CREATE INDEX IF NOT EXISTS idx_data_list_option_predicate_search_predicate_data_list_option_search_id
    ON data_list_option_predicate(data_list_option_search_id);

CREATE INDEX IF NOT EXISTS idx_data_list_option_predicate_finder_featurer_id
    ON data_list_option_predicate(data_list_option_finder_featurer_id);
