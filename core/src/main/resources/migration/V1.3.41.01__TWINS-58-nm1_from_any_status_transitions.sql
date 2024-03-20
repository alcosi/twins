alter table twinflow_transition
    alter column src_twin_status_id drop not null;

-- this is duplicate
drop index if exists uniq_twin_alias_counter_twin_class_id_business_account_id;

-- we have to allow only one null in uniq index
drop index if exists twinflow_transition_twinflow_transition_alias_id_twinflow_id_sr;
ALTER TABLE twinflow_transition
    DROP CONSTRAINT if EXISTS twinflow_transition_uniq;
ALTER TABLE twinflow_transition
    ADD CONSTRAINT twinflow_transition_uniq UNIQUE NULLS NOT DISTINCT (twinflow_id, src_twin_status_id, twinflow_transition_alias_id);
