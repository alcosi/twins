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

INSERT INTO featurer_type (id, name, description) VALUES (51::integer, 'OptionFinder'::varchar(40), null::varchar(255)) on conflict do nothing;
INSERT INTO featurer_type (id, name, description) VALUES (52::integer, 'OptionSorter'::varchar(40), null::varchar(255)) on conflict do nothing;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (5101::integer, 51::integer, '', '', '', DEFAULT) on conflict do nothing;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (5201::integer, 52::integer, '', '', '', DEFAULT) on conflict do nothing;

INSERT INTO data_list_option_search (id, domain_id, name, data_list_option_sorter_featurer_id, force_sorting) values ('00000000-0000-0000-0014-000000000005', null, 'Unlimited option search', 5201, false) on conflict do nothing ;