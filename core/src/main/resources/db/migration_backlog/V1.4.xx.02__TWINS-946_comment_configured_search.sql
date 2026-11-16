-- Configured search for twin_comment (see docs/configured_search_architecture.md)

INSERT INTO featurer_type (id, name, description)
VALUES (59, 'CommentFinder', 'Find comments')
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;

INSERT INTO featurer_type (id, name, description)
VALUES (60, 'CommentSorter', 'Order comments search')
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;

insert into featurer(id, featurer_type_id, class, name, description, deprecated)
values (5901, 59, '', 'Stub', '', false)
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;

insert into featurer(id, featurer_type_id, class, name, description, deprecated)
values (6001, 60, '', 'Unsorted', '', false)
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;

CREATE TABLE IF NOT EXISTS twin_comment_search (
    id         UUID PRIMARY KEY,
    domain_id  UUID NOT NULL REFERENCES domain (id) ON DELETE CASCADE,  -- null = system-wide
    name       VARCHAR NOT NULL,
    -- sorter block:
    force_sorting              BOOLEAN NOT NULL DEFAULT FALSE,
    comment_sorter_featurer_id INTEGER NOT NULL DEFAULT 6001 REFERENCES featurer (id),
    comment_sorter_params      HSTORE
);

CREATE TABLE IF NOT EXISTS twin_comment_search_predicate (
    id                        UUID PRIMARY KEY,
    twin_comment_search_id    UUID NOT NULL REFERENCES twin_comment_search (id) ON DELETE CASCADE,
    comment_finder_featurer_id INTEGER NOT NULL REFERENCES featurer (id) ON DELETE RESTRICT,
    comment_finder_params     HSTORE DEFAULT ''::hstore
);

CREATE INDEX IF NOT EXISTS idx_twin_comment_search_domain_id
    ON twin_comment_search (domain_id);

CREATE INDEX IF NOT EXISTS idx_twin_comment_search_sorter_featurer_id
    ON twin_comment_search (comment_sorter_featurer_id);

CREATE INDEX IF NOT EXISTS idx_twin_comment_search_predicate_search_id
    ON twin_comment_search_predicate (twin_comment_search_id);

CREATE INDEX IF NOT EXISTS idx_twin_comment_search_predicate_finder_featurer_id
    ON twin_comment_search_predicate (comment_finder_featurer_id);

-- UNLIMITED system row: "no configured search" marker, see SystemIds.TwinCommentSearch.UNLIMITED
INSERT INTO twin_comment_search (id, domain_id, name, force_sorting, comment_sorter_featurer_id)
values ('00000000-0000-0000-0014-000000000006', null, 'Unlimited comment search', false, 6001)
on conflict do nothing;
