ALTER TABLE IF EXISTS face_tw002
    DROP CONSTRAINT IF EXISTS face_tw002_i18n_twin_class_field_id_fk,
    ADD CONSTRAINT face_tw002_i18n_twin_class_field_id_fk FOREIGN KEY (i18n_twin_class_field_id) REFERENCES twin_class_field(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE IF EXISTS face_wt003
    DROP CONSTRAINT IF EXISTS face_wt003_title_substitution_twin_class_field_id_fkey,
    ADD CONSTRAINT face_wt003_title_substitution_twin_class_field_id_fkey FOREIGN KEY (title_substitution_twin_class_field_id) REFERENCES twin_class_field(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE IF EXISTS face_wt003
    DROP CONSTRAINT IF EXISTS face_wt003_message_substitution_twin_class_field_id_fkey,
    ADD CONSTRAINT face_wt003_message_substitution_twin_class_field_id_fkey FOREIGN KEY (message_substitution_twin_class_field_id) REFERENCES twin_class_field(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE IF EXISTS history_type_config_twin_class_field
    DROP CONSTRAINT IF EXISTS history_type_config_twin_class_field_field_id_fk,
    ADD CONSTRAINT history_type_config_twin_class_field_field_id_fk FOREIGN KEY (twin_class_field_id) REFERENCES twin_class_field(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE IF EXISTS projection
    DROP CONSTRAINT IF EXISTS projection_src_twin_class_field_id_fkey,
    ADD CONSTRAINT projection_src_twin_class_field_id_fkey FOREIGN KEY (src_twin_class_field_id) REFERENCES twin_class_field(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE IF EXISTS projection
    DROP CONSTRAINT IF EXISTS projection_dst_twin_class_field_id_fkey,
    ADD CONSTRAINT projection_dst_twin_class_field_id_fkey FOREIGN KEY (dst_twin_class_field_id) REFERENCES twin_class_field(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE IF EXISTS projection_exclusion
    DROP CONSTRAINT IF EXISTS projection_exclusion_twin_class_field_id_fkey,
    ADD CONSTRAINT projection_exclusion_twin_class_field_id_fkey FOREIGN KEY (twin_class_field_id) REFERENCES twin_class_field(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE IF EXISTS twin_attachment
    DROP CONSTRAINT IF EXISTS fk_attachment_fieldclass,
    DROP CONSTRAINT IF EXISTS twin_attachment_twin_class_field_id_fk,
    ADD CONSTRAINT twin_attachment_twin_class_field_id_fk FOREIGN KEY (twin_class_field_id) REFERENCES twin_class_field(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE IF EXISTS twin_class_field_condition
    DROP CONSTRAINT IF EXISTS twin_class_field_condition_base_twin_class_field_id_fkey,
    ADD CONSTRAINT twin_class_field_condition_base_twin_class_field_id_fkey FOREIGN KEY (base_twin_class_field_id) REFERENCES twin_class_field(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE IF EXISTS twin_class_field_rule
    DROP CONSTRAINT IF EXISTS twin_class_field_rule_twin_class_field_id_fkey,
    ADD CONSTRAINT twin_class_field_rule_twin_class_field_id_fkey FOREIGN KEY (twin_class_field_id) REFERENCES twin_class_field(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE IF EXISTS twin_field_boolean
    DROP CONSTRAINT IF EXISTS twin_field_boolean_twin_class_field_id_fkey,
    ADD CONSTRAINT twin_field_boolean_twin_class_field_id_fkey FOREIGN KEY (twin_class_field_id) REFERENCES twin_class_field(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE IF EXISTS twin_field_data_list
    DROP CONSTRAINT IF EXISTS twin_field_data_list_twin_class_field_id_fk,
    ADD CONSTRAINT twin_field_data_list_twin_class_field_id_fk FOREIGN KEY (twin_class_field_id) REFERENCES twin_class_field(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE IF EXISTS twin_field_i18n
    DROP CONSTRAINT IF EXISTS twin_field_i18n_twin_class_field_id_fkey,
    ADD CONSTRAINT twin_field_i18n_twin_class_field_id_fkey FOREIGN KEY (twin_class_field_id) REFERENCES twin_class_field(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE IF EXISTS twin_field_simple_non_indexed
    DROP CONSTRAINT IF EXISTS twin_field_simple_non_indexed_twin_class_field_id_fk,
    ADD CONSTRAINT twin_field_simple_non_indexed_twin_class_field_id_fk FOREIGN KEY (twin_class_field_id) REFERENCES twin_class_field(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE IF EXISTS twin_search_sort
    DROP CONSTRAINT IF EXISTS twin_search_sort_tcf_id_fk,
    ADD CONSTRAINT twin_search_sort_tcf_id_fk FOREIGN KEY (twin_class_field_id) REFERENCES twin_class_field(id) ON UPDATE CASCADE ON DELETE CASCADE;
