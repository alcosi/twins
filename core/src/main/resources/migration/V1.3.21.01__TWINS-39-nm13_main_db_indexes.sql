create index if not exists user_group_domain_id_business_account_id_index
    on user_group (domain_id, business_account_id);

create index if not exists user_group_user_group_type_id_index
    on user_group (user_group_type_id);

create index if not exists domain_business_account_business_account_id_index
    on domain_business_account (business_account_id);

create index if not exists twin_class_extends_twin_class_id_index
    on twin_class (extends_twin_class_id);

create index if not exists twin_class_head_twin_class_id_index
    on twin_class (head_twin_class_id);

create index if not exists twin_class_marker_data_list_id_index
    on twin_class (marker_data_list_id);

create index if not exists twin_class_tag_data_list_id_index
    on twin_class (tag_data_list_id);

create index if not exists twin_class_twin_class_owner_type_id_index
    on twin_class (twin_class_owner_type_id);

create index if not exists twin_class_field_edit_permission_id_index
    on twin_class_field (edit_permission_id);

create index if not exists twin_class_field_view_permission_id_index
    on twin_class_field (view_permission_id);

create index if not exists twin_class_field_field_typer_featurer_id_index
    on twin_class_field (field_typer_featurer_id);

create index if not exists permission_group_twin_class_id_index
    on permission_group (twin_class_id);

create index if not exists twin_twin_class_id_index
    on twin (twin_class_id);

create index if not exists twin_owner_business_account_id_index
    on twin (owner_business_account_id);

create index if not exists twin_assigner_user_id_index
    on twin (assigner_user_id);

create index if not exists twin_created_by_user_id_index
    on twin (created_by_user_id);

create index if not exists twin_head_twin_id_index
    on twin (head_twin_id);

create index if not exists twin_owner_user_id_index
    on twin (owner_user_id);

create index if not exists twin_twin_status_id_index
    on twin (twin_status_id);

create index if not exists twin_status_twins_class_id_index
    on twin_status (twins_class_id);

create index if not exists twin_comment_twin_id_index
    on twin_comment (twin_id);

create index if not exists twin_attachment_twin_id_index
    on twin_attachment (twin_id);

create index if not exists twin_attachment_twinflow_transition_id_index
    on twin_attachment (twinflow_transition_id);

create index if not exists twinflow_transition_dst_twin_status_id_index
    on twinflow_transition (dst_twin_status_id);

create index if not exists twinflow_transition_inbuilt_twin_factory_id_index
    on twinflow_transition (inbuilt_twin_factory_id);

create index if not exists twinflow_transition_permission_id_index
    on twinflow_transition (permission_id);

create index if not exists twinflow_transition_twinflow_id_index
    on twinflow_transition (twinflow_id);

create unique index if not exists  twinflow_transition_twinflow_transition_alias_id_twinflow_id_sr
    on twinflow_transition (twinflow_transition_alias_id, twinflow_id, src_twin_status_id);

create index if not exists twinflow_schema_domain_id_business_account_id_index
    on twinflow_schema (domain_id, business_account_id);

create index if not exists twinflow_schema_map_twinflow_id_index
    on twinflow_schema_map (twinflow_id);

create index if not exists twin_watcher_watcher_user_id_index
    on twin_watcher (watcher_user_id);

create index if not exists twin_work_author_user_id_index
    on twin_work (author_user_id);

create index if not exists twin_work_twin_id_index
    on twin_work (twin_id);

create index if not exists twin_class_schema_domain_id_index
    on twin_class_schema (domain_id);

create index if not exists twin_class_schema_map_twin_class_schema_id_index
    on twin_class_schema_map (twin_class_schema_id);

create index if not exists permission_schema_business_account_id_index
    on permission_schema (business_account_id);

create index if not exists permission_schema_domain_id_business_account_id_index
    on permission_schema (domain_id, business_account_id);

create index if not exists twinflow_transition_validator_transition_validator_featurer_id_
    on twinflow_transition_validator (transition_validator_featurer_id);

create index if not exists twinflow_transition_trigger_transition_trigger_featurer_id_inde
    on twinflow_transition_trigger (transition_trigger_featurer_id);

drop index twin_status_group_map_twin_status_id_twin_status_group_id_uinde;

create unique index if not exists  twin_status_group_map_twin_status_id_twin_status_group_id_uinde
    on twin_status_group_map (twin_status_group_id, twin_status_id);

create index if not exists data_list_option_data_list_id_business_account_id_index
    on data_list_option (data_list_id, business_account_id);

create index if not exists domain_user_user_id_index
    on domain_user (user_id);

create index if not exists twin_domain_alias_twin_id_index
    on twin_domain_alias (twin_id);

create index if not exists user_group_map_user_group_id_index
    on user_group_map (user_group_id);

create index if not exists user_group_map_user_id_business_account_id_index
    on user_group_map (user_id, business_account_id);

create index if not exists user_group_type_slugger_featurer_id_index
    on user_group_type (slugger_featurer_id);

create index if not exists link_domain_id_index
    on link (domain_id);

create index if not exists link_dst_twin_class_id_index
    on link (dst_twin_class_id);

create index if not exists link_src_twin_class_id_index
    on link (src_twin_class_id);

create index if not exists twin_link_dst_twin_id_index
    on twin_link (dst_twin_id);

create index if not exists twin_link_link_id_index
    on twin_link (link_id);

create index if not exists twin_link_src_twin_id_index
    on twin_link (src_twin_id);

create index if not exists twin_status_transition_trigger_transition_trigger_featurer_id_i
    on twin_status_transition_trigger (transition_trigger_featurer_id);

create index if not exists twin_field_data_list_data_list_option_id_index
    on twin_field_data_list (data_list_option_id);

create index if not exists twin_field_data_list_twin_field_id_index
    on twin_field_data_list (twin_field_id);

create index if not exists twin_factory_domain_id_index
    on twin_factory (domain_id);

create index if not exists twin_factory_multiplier_input_twin_class_id_index
    on twin_factory_multiplier (input_twin_class_id);

create index if not exists twin_factory_multiplier_multiplier_featurer_id_index
    on twin_factory_multiplier (multiplier_featurer_id);

create index if not exists twin_factory_multiplier_twin_factory_id_index
    on twin_factory_multiplier (twin_factory_id);

create index if not exists twin_field_user_twin_field_id_index
    on twin_field_user (twin_field_id);

create index if not exists twin_field_user_user_id_index
    on twin_field_user (user_id);

create index if not exists twinflow_transition_alias_domain_id_index
    on twinflow_transition_alias (domain_id);

create index if not exists twin_factory_pipeline_step_filler_featurer_id_index
    on twin_factory_pipeline_step (filler_featurer_id);

create index if not exists twin_factory_pipeline_step_twin_factory_condition_set_id_index
    on twin_factory_pipeline_step (twin_factory_condition_set_id);

create index if not exists twin_factory_condition_conditioner_featurer_id_index
    on twin_factory_condition (conditioner_featurer_id);

create index if not exists twin_factory_condition_twin_factory_condition_set_id_index
    on twin_factory_condition (twin_factory_condition_set_id);

create index if not exists twin_factory_pipeline_input_twin_class_id_index
    on twin_factory_pipeline (input_twin_class_id);

create index if not exists twin_factory_pipeline_next_twin_factory_id_index
    on twin_factory_pipeline (next_twin_factory_id);

create index if not exists twin_factory_pipeline_template_twin_id_index
    on twin_factory_pipeline (template_twin_id);

create index if not exists twin_factory_pipeline_twin_factory_condition_set_id_index
    on twin_factory_pipeline (twin_factory_condition_set_id);

create index if not exists twin_factory_pipeline_twin_factory_id_index
    on twin_factory_pipeline (twin_factory_id);

create index if not exists twin_marker_marker_data_list_option_id_index
    on twin_marker (marker_data_list_option_id);

create index if not exists twin_marker_twin_id_index
    on twin_marker (twin_id);

create index if not exists history_twin_id_index
    on history (twin_id);

