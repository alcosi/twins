-- business_account
CREATE INDEX IF NOT EXISTS idx_business_account_owner_user_group_id ON public.business_account(owner_user_group_id);

-- card
CREATE INDEX IF NOT EXISTS idx_card_card_layout_id ON public.card(card_layout_id);
CREATE INDEX IF NOT EXISTS idx_card_name_i18n_id ON public.card(name_i18n_id);

-- card_access
CREATE INDEX IF NOT EXISTS idx_card_access_card_id ON public.card_access(card_id);

-- card_override
CREATE INDEX IF NOT EXISTS idx_card_override_override_card_id ON public.card_override(override_card_id);
CREATE INDEX IF NOT EXISTS idx_card_override_card_layout_id ON public.card_override(card_layout_id);
CREATE INDEX IF NOT EXISTS idx_card_override_override_for_channel_id ON public.card_override(override_for_channel_id);
CREATE INDEX IF NOT EXISTS idx_card_override_name_i18n_id ON public.card_override(name_i18n_id);

-- card_widget
CREATE INDEX IF NOT EXISTS idx_card_widget_widget_id ON public.card_widget(widget_id);

-- card_widget_override
CREATE INDEX IF NOT EXISTS idx_card_widget_override_card_layout_position_id ON public.card_widget_override(card_layout_position_id);
CREATE INDEX IF NOT EXISTS idx_card_widget_override_override_card_widget_id ON public.card_widget_override(override_card_widget_id);
CREATE INDEX IF NOT EXISTS idx_card_widget_override_override_for_channel_id ON public.card_widget_override(override_for_channel_id);

-- data_list
CREATE INDEX IF NOT EXISTS idx_data_list_default_data_list_option_id ON public.data_list(default_data_list_option_id);
CREATE INDEX IF NOT EXISTS idx_data_list_attribute_1_name_i18n_id ON public.data_list(attribute_1_name_i18n_id);
CREATE INDEX IF NOT EXISTS idx_data_list_attribute_2_name_i18n_id ON public.data_list(attribute_2_name_i18n_id);
CREATE INDEX IF NOT EXISTS idx_data_list_attribute_3_name_i18n_id ON public.data_list(attribute_3_name_i18n_id);
CREATE INDEX IF NOT EXISTS idx_data_list_attribute_4_name_i18n_id ON public.data_list(attribute_4_name_i18n_id);
CREATE INDEX IF NOT EXISTS idx_data_list_description_i18n_id ON public.data_list(description_i18n_id);
CREATE INDEX IF NOT EXISTS idx_data_list_name_i18n_id ON public.data_list(name_i18n_id);

-- data_list_option
CREATE INDEX IF NOT EXISTS idx_data_list_option_data_list_option_status_id ON public.data_list_option(data_list_option_status_id);
CREATE INDEX IF NOT EXISTS idx_data_list_option_option_i18n_id ON public.data_list_option(option_i18n_id);
CREATE INDEX IF NOT EXISTS idx_data_list_option_description_i18n_id ON public.data_list_option(description_i18n_id);

-- data_list_option_projection
CREATE INDEX IF NOT EXISTS idx_data_list_option_projection_projection_type_id ON public.data_list_option_projection(projection_type_id);
CREATE INDEX IF NOT EXISTS idx_data_list_option_projection_saved_by_user_id ON public.data_list_option_projection(saved_by_user_id);

-- data_list_subset
CREATE INDEX IF NOT EXISTS idx_data_list_subset_data_list_id ON public.data_list_subset(data_list_id);

-- domain
CREATE INDEX IF NOT EXISTS idx_domain_attachments_storage_id ON public.domain(attachments_storage_id);
CREATE INDEX IF NOT EXISTS idx_domain_business_account_initiator_featurer_id ON public.domain(business_account_initiator_featurer_id);
CREATE INDEX IF NOT EXISTS idx_domain_default_i18n_locale_id ON public.domain(default_i18n_locale_id);
CREATE INDEX IF NOT EXISTS idx_domain_domain_status_id ON public.domain(domain_status_id);
CREATE INDEX IF NOT EXISTS idx_domain_domain_type_id ON public.domain(domain_type_id);
CREATE INDEX IF NOT EXISTS idx_domain_domain_user_initiator_featurer_id ON public.domain(domain_user_initiator_featurer_id);
CREATE INDEX IF NOT EXISTS idx_domain_icon_dark_resource_id ON public.domain(icon_dark_resource_id);
CREATE INDEX IF NOT EXISTS idx_domain_icon_light_resource_id ON public.domain(icon_light_resource_id);
CREATE INDEX IF NOT EXISTS idx_domain_identity_provider_id ON public.domain(identity_provider_id);
CREATE INDEX IF NOT EXISTS idx_domain_navbar_face_id ON public.domain(navbar_face_id);
CREATE INDEX IF NOT EXISTS idx_domain_notification_schema_id ON public.domain(notification_schema_id);
CREATE INDEX IF NOT EXISTS idx_domain_permission_schema_id ON public.domain(permission_schema_id);
CREATE INDEX IF NOT EXISTS idx_domain_resources_storage_id ON public.domain(resources_storage_id);
CREATE INDEX IF NOT EXISTS idx_domain_default_tier_id ON public.domain(default_tier_id);
CREATE INDEX IF NOT EXISTS idx_domain_ancestor_twin_class_id ON public.domain(ancestor_twin_class_id);
CREATE INDEX IF NOT EXISTS idx_domain_twin_class_schema_id ON public.domain(twin_class_schema_id);
CREATE INDEX IF NOT EXISTS idx_domain_business_account_template_twin_id ON public.domain(business_account_template_twin_id);
CREATE INDEX IF NOT EXISTS idx_domain_twinflow_schema_id ON public.domain(twinflow_schema_id);
CREATE INDEX IF NOT EXISTS idx_domain_user_group_manager_featurer_id ON public.domain(user_group_manager_featurer_id);

-- domain_business_account
CREATE INDEX IF NOT EXISTS idx_domain_business_account_notification_schema_id ON public.domain_business_account(notification_schema_id);
CREATE INDEX IF NOT EXISTS idx_domain_business_account_permission_schema_id ON public.domain_business_account(permission_schema_id);
CREATE INDEX IF NOT EXISTS idx_domain_business_account_tier_id ON public.domain_business_account(tier_id);
CREATE INDEX IF NOT EXISTS idx_domain_business_account_twin_class_schema_id ON public.domain_business_account(twin_class_schema_id);
CREATE INDEX IF NOT EXISTS idx_domain_business_account_twinflow_schema_id ON public.domain_business_account(twinflow_schema_id);

-- domain_type
CREATE INDEX IF NOT EXISTS idx_domain_type_default_identity_provider_id ON public.domain_type(default_identity_provider_id);

-- domain_user
CREATE INDEX IF NOT EXISTS idx_domain_user_i18n_locale_id ON public.domain_user(i18n_locale_id);
CREATE INDEX IF NOT EXISTS idx_domain_user_last_active_business_account_id ON public.domain_user(last_active_business_account_id);

-- draft
CREATE INDEX IF NOT EXISTS idx_draft_business_account_id ON public.draft(business_account_id);
CREATE INDEX IF NOT EXISTS idx_draft_domain_id ON public.draft(domain_id);
CREATE INDEX IF NOT EXISTS idx_draft_created_by_user_id ON public.draft(created_by_user_id);
CREATE INDEX IF NOT EXISTS idx_draft_draft_status_id ON public.draft(draft_status_id);

-- draft_history
CREATE INDEX IF NOT EXISTS idx_draft_history_draft_id ON public.draft_history(draft_id);
CREATE INDEX IF NOT EXISTS idx_draft_history_history_type_id ON public.draft_history(history_type_id);
CREATE INDEX IF NOT EXISTS idx_draft_history_twin_class_field_id ON public.draft_history(twin_class_field_id);
CREATE INDEX IF NOT EXISTS idx_draft_history_actor_user_id ON public.draft_history(actor_user_id);

-- draft_twin_attachment
CREATE INDEX IF NOT EXISTS idx_draft_twin_attachment_cud_id ON public.draft_twin_attachment(cud_id);

-- draft_twin_erase
CREATE INDEX IF NOT EXISTS idx_draft_twin_erase_draft_twin_erase_status_id ON public.draft_twin_erase(draft_twin_erase_status_id);
CREATE INDEX IF NOT EXISTS idx_draft_twin_erase_reason_link_id ON public.draft_twin_erase(reason_link_id);
CREATE INDEX IF NOT EXISTS idx_draft_twin_erase_reason_twin_id ON public.draft_twin_erase(reason_twin_id);
CREATE INDEX IF NOT EXISTS idx_draft_twin_erase_twin_erase_reason_id ON public.draft_twin_erase(twin_erase_reason_id);

-- draft_twin_field_boolean
CREATE INDEX IF NOT EXISTS idx_draft_twin_field_boolean_cud_id ON public.draft_twin_field_boolean(cud_id);

-- draft_twin_field_data_list
CREATE INDEX IF NOT EXISTS idx_draft_twin_field_data_list_cud_id ON public.draft_twin_field_data_list(cud_id);

-- draft_twin_field_simple
CREATE INDEX IF NOT EXISTS idx_draft_twin_field_simple_cud_id ON public.draft_twin_field_simple(cud_id);

-- draft_twin_field_simple_non_indexed
CREATE INDEX IF NOT EXISTS idx_draft_twin_field_simple_non_indexed_cud_id ON public.draft_twin_field_simple_non_indexed(cud_id);

-- draft_twin_field_twin_class
CREATE INDEX IF NOT EXISTS idx_draft_twin_field_twin_class_cud_id ON public.draft_twin_field_twin_class(cud_id);
CREATE INDEX IF NOT EXISTS idx_draft_twin_field_twin_class_draft_id ON public.draft_twin_field_twin_class(draft_id);
CREATE INDEX IF NOT EXISTS idx_draft_twin_field_twin_class_twin_class_id ON public.draft_twin_field_twin_class(twin_class_id);

-- draft_twin_field_user
CREATE INDEX IF NOT EXISTS idx_draft_twin_field_user_cud_id ON public.draft_twin_field_user(cud_id);

-- draft_twin_link
CREATE INDEX IF NOT EXISTS idx_draft_twin_link_cud_id ON public.draft_twin_link(cud_id);

-- eraseflow
CREATE INDEX IF NOT EXISTS idx_eraseflow_cascade_deletion_by_head_factory_id ON public.eraseflow(cascade_deletion_by_head_factory_id);
CREATE INDEX IF NOT EXISTS idx_eraseflow_cascade_deletion_by_link_default_factory_id ON public.eraseflow(cascade_deletion_by_link_default_factory_id);
CREATE INDEX IF NOT EXISTS idx_eraseflow_description_i18n_id ON public.eraseflow(description_i18n_id);
CREATE INDEX IF NOT EXISTS idx_eraseflow_name_i18n_id ON public.eraseflow(name_i18n_id);
CREATE INDEX IF NOT EXISTS idx_eraseflow_target_deletion_factory_id ON public.eraseflow(target_deletion_factory_id);
CREATE INDEX IF NOT EXISTS idx_eraseflow_created_by_user_id ON public.eraseflow(created_by_user_id);
CREATE INDEX IF NOT EXISTS idx_eraseflow_twin_class_id ON public.eraseflow(twin_class_id);

-- eraseflow_link_cascade
CREATE INDEX IF NOT EXISTS idx_eraseflow_link_cascade_cascade_deletion_factory_id ON public.eraseflow_link_cascade(cascade_deletion_factory_id);
CREATE INDEX IF NOT EXISTS idx_eraseflow_link_cascade_created_by_user_id ON public.eraseflow_link_cascade(created_by_user_id);

-- error
CREATE INDEXLY IF NOT EXISTS idx_error_client_msg_i18n_id ON public.error(client_msg_i18n_id);

-- face
CREATE INDEX IF NOT EXISTS idx_face_created_by_user_id ON public.face(created_by_user_id);
CREATE INDEX IF NOT EXISTS idx_face_domain_id ON public.face(domain_id);
CREATE INDEX IF NOT EXISTS idx_face_face_component_id ON public.face(face_component_id);

-- face_component
CREATE INDEX IF NOT EXISTS idx_face_component_face_component_type_id ON public.face_component(face_component_type_id);

-- face_navbar_nb001
CREATE INDEX IF NOT EXISTS idx_face_navbar_nb001_admin_area_icon_resource_id ON public.face_navbar_nb001(admin_area_icon_resource_id);
CREATE INDEX IF NOT EXISTS idx_face_navbar_nb001_admin_area_label_i18n_id ON public.face_navbar_nb001(admin_area_label_i18n_id);
CREATE INDEX IF NOT EXISTS idx_face_navbar_nb001_user_area_icon_resource_id ON public.face_navbar_nb001(user_area_icon_resource_id);
CREATE INDEX IF NOT EXISTS idx_face_navbar_nb001_user_area_label_i18n_id ON public.face_navbar_nb001(user_area_label_i18n_id);

-- face_navbar_nb001_menu_item
CREATE INDEX IF NOT EXISTS idx_face_navbar_nb001_menu_items_description_i18n_id ON public.face_navbar_nb001_menu_item(description_i18n_id);
CREATE INDEX IF NOT EXISTS idx_face_navbar_nb001_menu_items_face_id ON public.face_navbar_nb001_menu_item(face_id);
CREATE INDEX IF NOT EXISTS idx_face_navbar_nb001_menu_items_face_navbar_nb001_status_id ON public.face_navbar_nb001_menu_item(face_navbar_nb001_status_id);
CREATE INDEX IF NOT EXISTS idx_face_navbar_nb001_menu_items_icon_resource_id ON public.face_navbar_nb001_menu_item(icon_resource_id);
CREATE INDEX IF NOT EXISTS idx_face_navbar_nb001_menu_items_label_i18n_id ON public.face_navbar_nb001_menu_item(label_i18n_id);
CREATE INDEX IF NOT EXISTS idx_face_navbar_nb001_menu_items_target_page_face_id ON public.face_navbar_nb001_menu_item(target_page_face_id);
CREATE INDEX IF NOT EXISTS idx_face_navbar_nb001_menu_items_target_page_face_id_2 ON public.face_navbar_nb001_menu_item(target_page_face_id);
CREATE INDEX IF NOT EXISTS idx_face_navbar_nb001_menu_item_parent_face_navbar_nb001_menu_item_id ON public.face_navbar_nb001_menu_item(parent_face_navbar_nb001_menu_item_id);
CREATE INDEX IF NOT EXISTS idx_menu_items_permission ON public.face_navbar_nb001_menu_item(permission_id);

-- face_tc001
CREATE INDEX IF NOT EXISTS idx_face_tc001_header_i18n_id ON public.face_tc001(header_i18n_id);
CREATE INDEX IF NOT EXISTS idx_face_tc001_header_icon_resource_id ON public.face_tc001(header_icon_resource_id);
CREATE INDEX IF NOT EXISTS idx_face_tc001_option_select_i18n_id ON public.face_tc001(option_select_i18n_id);
CREATE INDEX IF NOT EXISTS idx_face_tc001_save_button_label_i18n_id ON public.face_tc001(save_button_label_i18n_id);

-- face_tc001_option
CREATE INDEX IF NOT EXISTS idx_face_tc001_option_twin_class_field_search_id ON public.face_tc001_option(twin_class_field_search_id);
CREATE INDEX IF NOT EXISTS idx_face_tc001_option_twin_class_id ON public.face_tc001_option(twin_class_id);
CREATE INDEX IF NOT EXISTS idx_face_tc001_option_twin_class_search_id ON public.face_tc001_option(twin_class_search_id);
CREATE INDEX IF NOT EXISTS idx_face_tc001_option_twin_pointer_validator_rule_id ON public.face_tc001_option(twin_pointer_validator_rule_id);

-- face_tw001
CREATE INDEX IF NOT EXISTS idx_face_tw001_target_twin_pointer_id ON public.face_tw001(target_twin_pointer_id);

-- face_tw002
CREATE INDEX IF NOT EXISTS idx_face_tw002_target_twin_pointer_id ON public.face_tw002(target_twin_pointer_id);

-- face_tw004
CREATE INDEX IF NOT EXISTS idx_face_tw004_target_twin_pointer_id ON public.face_tw004(target_twin_pointer_id);

-- face_tw005
CREATE INDEX IF NOT EXISTS idx_face_tw005_target_twin_pointer_id ON public.face_tw005(target_twin_pointer_id);
CREATE INDEX IF NOT EXISTS idx_face_tw005_twin_pointer_validator_rule_id ON public.face_tw005(twin_pointer_validator_rule_id);

-- face_tw007
CREATE INDEX IF NOT EXISTS idx_face_tw007_face_id ON public.face_tw007(face_id);

-- face_wt001
CREATE INDEX IF NOT EXISTS idx_face_wt001_twin_search_id ON public.face_wt001(twin_search_id);

-- featurer
CREATE INDEX IF NOT EXISTS idx_featurer_featurer_type_id ON public.featurer(featurer_type_id);

-- featurer_injection
CREATE INDEX IF NOT EXISTS idx_featurer_injections_injector_featurer_id ON public.featurer_injection(injector_featurer_id);

-- featurer_param
CREATE INDEX IF NOT EXISTS idx_featurer_param_featurer_param_type_id ON public.featurer_param(featurer_param_type_id);

-- history
CREATE INDEX IF NOT EXISTS idx_history_history_type_id ON public.history(history_type_id);
CREATE INDEX IF NOT EXISTS idx_history_twin_class_field_id ON public.history(twin_class_field_id);
CREATE INDEX IF NOT EXISTS idx_history_actor_user_id ON public.history(actor_user_id);

-- history_notification_recipient
CREATE INDEX IF NOT EXISTS idx_history_notification_recipient_description_i18n_id ON public.history_notification_recipient(description_i18n_id);
CREATE INDEX IF NOT EXISTS idx_history_notification_recipient_domain_id ON public.history_notification_recipient(domain_id);
CREATE INDEX IF NOT EXISTS idx_history_notification_recipient_name_i18n_id ON public.history_notification_recipient(name_i18n_id);

-- history_notification_recipient_collector
CREATE INDEX IF NOT EXISTS idx_history_notification_recipient_collector_recipient_resolver_featurer_id ON public.history_notification_recipient_collector(recipient_resolver_featurer_id);
CREATE INDEX IF NOT EXISTS idx_history_notification_recipient_collector_history_notification_recipient_id ON public.history_notification_recipient_collector(history_notification_recipient_id);

-- history_notification_task
CREATE INDEX IF NOT EXISTS idx_history_notification_task_history_id ON public.history_notification_task(history_id);
CREATE INDEX IF NOT EXISTS idx_history_notification_task_notification_schema_id ON public.history_notification_task(notification_schema_id);
CREATE INDEX IF NOT EXISTS idx_history_notification_task_history_notification_task_status_id ON public.history_notification_task(history_notification_task_status_id);

-- history_type
CREATE INDEX IF NOT EXISTS idx_history_type_history_type_status_id ON public.history_type(history_type_status_id);

-- history_type_config_domain
CREATE INDEX IF NOT EXISTS idx_history_type_config_domain_history_type_id ON public.history_type_config_domain(history_type_id);
CREATE INDEX IF NOT EXISTS idx_history_type_config_domain_message_template_i18n_id ON public.history_type_config_domain(message_template_i18n_id);
CREATE INDEX IF NOT EXISTS idx_history_type_config_domain_domain_id ON public.history_type_config_domain(domain_id);
CREATE INDEX IF NOT EXISTS idx_history_type_config_domain_history_type_status_id ON public.history_type_config_domain(history_type_status_id);

-- history_type_config_twin_class
CREATE INDEX IF NOT EXISTS idx_history_type_config_twin_class_history_type_id ON public.history_type_config_twin_class(history_type_id);
CREATE INDEX IF NOT EXISTS idx_history_type_config_twin_class_message_template_i18n_id ON public.history_type_config_twin_class(message_template_i18n_id);
CREATE INDEX IF NOT EXISTS idx_history_type_config_twin_class_twin_class_id ON public.history_type_config_twin_class(twin_class_id);
CREATE INDEX IF NOT EXISTS idx_history_type_config_twin_class_history_type_status_id ON public.history_type_config_twin_class(history_type_status_id);

-- history_type_config_twin_class_field
CREATE INDEX IF NOT EXISTS idx_history_type_config_twin_class_field_field_id ON public.history_type_config_twin_class_field(twin_class_field_id);
CREATE INDEX IF NOT EXISTS idx_history_type_config_twin_class_field_history_type_id ON public.history_type_config_twin_class_field(history_type_id);
CREATE INDEX IF NOT EXISTS idx_history_type_config_twin_class_field_message_template_i18n_id ON public.history_type_config_twin_class_field(message_template_i18n_id);
CREATE INDEX IF NOT EXISTS idx_history_type_config_twin_class_field_history_type_status_id ON public.history_type_config_twin_class_field(history_type_status_id);

-- history_type_domain_template
CREATE INDEX IF NOT EXISTS idx_history_type_domain_template_domain_id ON public.history_type_domain_template(domain_id);
CREATE INDEX IF NOT EXISTS idx_history_type_domain_template_history_type_id ON public.history_type_domain_template(history_type_id);
CREATE INDEX IF NOT EXISTS idx_history_type_domain_template_history_type_status_id ON public.history_type_domain_template(history_type_status_id);

-- i18n
CREATE INDEX IF NOT EXISTS idx_i18n_domain_id ON public.i18n(domain_id);
CREATE INDEX IF NOT EXISTS idx_i18n_i18n_type_id ON public.i18n(i18n_type_id);

-- i18n_translation_style
CREATE INDEX IF NOT EXISTS idx_i18n_translations_styles_i18n_id ON public.i18n_translation_style(i18n_id);

-- identity_provider
CREATE INDEX IF NOT EXISTS idx_identity_provider_identity_provider_connector_featurer_id ON public.identity_provider(identity_provider_connector_featurer_id);
CREATE INDEX IF NOT EXISTS idx_identity_provider_trustor_featurer_id ON public.identity_provider(trustor_featurer_id);
CREATE INDEX IF NOT EXISTS idx_identity_provider_identity_provider_status_id ON public.identity_provider(identity_provider_status_id);

-- identity_provider_internal_token
CREATE INDEX IF NOT EXISTS idx_identity_provider_internal_token_active_business_account_id ON public.identity_provider_internal_token(active_business_account_id);
CREATE INDEX IF NOT EXISTS idx_identity_provider_internal_token_domain_id ON public.identity_provider_internal_token(domain_id);

-- link
CREATE INDEX IF NOT EXISTS idx_link_backward_name_i18n_id ON public.link(backward_name_i18n_id);
CREATE INDEX IF NOT EXISTS idx_link_linker_featurer_id ON public.link(linker_featurer_id);
CREATE INDEX IF NOT EXISTS idx_link_forward_name_i18n_id ON public.link(forward_name_i18n_id);
CREATE INDEX IF NOT EXISTS idx_link_link_type_id ON public.link(link_type_id);
CREATE INDEX IF NOT EXISTS idx_link_link_strength_id ON public.link(link_strength_id);

-- link_tree
CREATE INDEX IF NOT EXISTS idx_link_tree_domain_id ON public.link_tree(domain_id);
CREATE INDEX IF NOT EXISTS idx_link_tree_root_twin_class_id ON public.link_tree(root_twin_class_id);
CREATE INDEX IF NOT EXISTS idx_link_tree_created_by_user_id ON public.link_tree(created_by_user_id);

-- link_tree_node
CREATE INDEX IF NOT EXISTS idx_link_tree_node_link_id ON public.link_tree_node(link_id);
CREATE INDEX IF NOT EXISTS idx_link_tree_node_link_tree_id ON public.link_tree_node(link_tree_id);

-- link_trigger
CREATE INDEX IF NOT EXISTS idx_link_trigger_link_trigger_featurer_id ON public.link_trigger(link_trigger_featurer_id);

-- link_validator
CREATE INDEX IF NOT EXISTS idx_link_validator_link_validator_featurer_id ON public.link_validator(link_validator_featurer_id);

-- notification_channel
CREATE INDEX IF NOT EXISTS idx_notification_channel_domain_id ON public.notification_channel(domain_id);
CREATE INDEX IF NOT EXISTS idx_notification_channel_notifier_featurer_id ON public.notification_channel(notifier_featurer_id);

-- notification_channel_event
CREATE INDEX IF NOT EXISTS idx_notification_channel_event_notification_channel_id ON public.notification_channel_event(notification_channel_id);
CREATE INDEX IF NOT EXISTS idx_notification_channel_event_notification_context_id ON public.notification_channel_event(notification_context_id);

-- notification_context
CREATE INDEX IF NOT EXISTS idx_notification_context_description_i18n_id ON public.notification_context(description_i18n_id);
CREATE INDEX IF NOT EXISTS idx_notification_context_domain_id ON public.notification_context(domain_id);
CREATE INDEX IF NOT EXISTS idx_notification_context_name_i18n_id ON public.notification_context(name_i18n_id);

-- notification_context_collector
CREATE INDEX IF NOT EXISTS idx_notification_context_collector_context_id ON public.notification_context_collector(notification_context_id);
CREATE INDEX IF NOT EXISTS idx_notification_context_collector_context_collector_featurer_id ON public.notification_context_collector(context_collector_featurer_id);

-- notification_email
CREATE INDEX IF NOT EXISTS idx_notification_email_subject_i18n_id ON public.notification_email(subject_i18n_id);

-- notification_schema
CREATE INDEX IF NOT EXISTS idx_notification_schema_description_i18n_id ON public.notification_schema(description_i18n_id);
CREATE INDEX IF NOT EXISTS idx_notification_schema_domain_id ON public.notification_schema(domain_id);
CREATE INDEX IF NOT EXISTS idx_notification_schema_name_i18n_id ON public.notification_schema(name_i18n_id);

-- permission
CREATE INDEX IF NOT EXISTS idx_permission_description_i18n_id ON public.permission(description_i18n_id);
CREATE INDEX IF NOT EXISTS idx_permission_name_i18n_id ON public.permission(name_i18n_id);

-- permission_grant_assignee_propagation
CREATE INDEX IF NOT EXISTS idx_permission_schema_assignee_propag_granted_by_user_id ON public.permission_grant_assignee_propagation(granted_by_user_id);
CREATE INDEX IF NOT EXISTS idx_permission_schema_assignee_propag_permission_id ON public.permission_grant_assignee_propagation(permission_id);
CREATE INDEX IF NOT EXISTS idx_permission_schema_assignee_propag_permission_schema_id ON public.permission_grant_assignee_propagation(permission_schema_id);
CREATE INDEX IF NOT EXISTS idx_permission_schema_assignee_propag_twin_class_id ON public.permission_grant_assignee_propagation(propagation_by_twin_class_id);
CREATE INDEX IF NOT EXISTS idx_permission_schema_assignee_propag_twin_status_id ON public.permission_grant_assignee_propagation(propagation_by_twin_status_id);

-- permission_grant_global
CREATE INDEX IF NOT EXISTS idx_permission_grant_global_granted_by_user_id ON public.permission_grant_global(granted_by_user_id);

-- permission_grant_space_role
CREATE INDEX IF NOT EXISTS idx_permission_schema_space_roles_granted_by_user_id ON public.permission_grant_space_role(granted_by_user_id);

--permission_grant_twin_role
CREATE INDEX IF NOT EXISTS idx_permission_schema_twin_role_granted_by_user_id ON public.permission_grant_twin_role(granted_by_user_id);
CREATE INDEX IF NOT EXISTS idx_permission_schema_twin_role_permission_id ON public.permission_grant_twin_role(permission_id);
CREATE INDEX IF NOT EXISTS idx_permission_schema_twin_role_permission_schema_id ON public.permission_grant_twin_role(permission_schema_id);
CREATE INDEX IF NOT EXISTS idx_permission_schema_twin_role_twin_class_id ON public.permission_grant_twin_role(twin_class_id);
CREATE INDEX IF NOT EXISTS idx_permission_schema_twin_role_twin_role_id ON public.permission_grant_twin_role(twin_role_id);

-- permission_grant_user
CREATE INDEX IF NOT EXISTS idx_permission_schema_user_granted_by_user_id ON public.permission_grant_user(granted_by_user_id);

-- permission_grant_user_group
CREATE INDEX IF NOT EXISTS idx_permission_schema_user_group_granted_by_user_id ON public.permission_grant_user_group(granted_by_user_id);

-- permission_schema
CREATE INDEX IF NOT EXISTS idx_permission_schema_created_by_user_id ON public.permission_schema(created_by_user_id);

-- projection_type
CREATE INDEX IF NOT EXISTS idx_projection_type_domain_id ON public.projection_type(domain_id);
CREATE INDEX IF NOT EXISTS idx_projection_type_membership_twin_class_id ON public.projection_type(membership_twin_class_id);
CREATE INDEX IF NOT EXISTS idx_projection_type_projection_type_group_id ON public.projection_type(projection_type_group_id);

-- projection_type_group
CREATE INDEX IF NOT EXISTS idx_projection_type_group_domain_id ON public.projection_type_group(domain_id);

-- resource
CREATE INDEX IF NOT EXISTS idx_resource_domain_id ON public.resource(domain_id);
CREATE INDEX IF NOT EXISTS idx_resource_storage_id ON public.resource(storage_id);
CREATE INDEX IF NOT EXISTS idx_resource_uploaded_by_user_id ON public.resource(uploaded_by_user_id);

-- space
CREATE INDEX IF NOT EXISTS idx_space_permission_schema_id ON public.space(permission_schema_id);
CREATE INDEX IF NOT EXISTS idx_space_twin_class_schema_id ON public.space(twin_class_schema_id);
CREATE INDEX IF NOT EXISTS idx_space_twinflow_schema_id ON public.space(twinflow_schema_id);

-- space_role
CREATE INDEX IF NOT EXISTS idx_space_role_business_account_id ON public.space_role(business_account_id);
CREATE INDEX IF NOT EXISTS idx_space_role_name_i18n_id ON public.space_role(name_i18n_id);
CREATE INDEX IF NOT EXISTS idx_space_role_description_i18n_id ON public.space_role(description_i18n_id);

-- space_role_user
CREATE INDEX IF NOT EXISTS idx_space_role_user_created_by_user_id ON public.space_role_user(created_by_user_id);

-- space_role_user_group
CREATE INDEX IF NOT EXISTS idx_space_role_user_group_created_by_user_id ON public.space_role_user_group(created_by_user_id);

-- storage
CREATE INDEX IF NOT EXISTS idx_storage_domain_id ON public.storage(domain_id);
CREATE INDEX IF NOT EXISTS idx_storage_storager_featurer_id ON public.storage(storager_featurer_id);

-- tier
CREATE INDEX IF NOT EXISTS idx_tier_domain_id ON public.tier(domain_id);
CREATE INDEX IF NOT EXISTS idx_tier_notification_schema_id ON public.tier(notification_schema_id);
CREATE INDEX IF NOT EXISTS idx_tier_permission_schema_id ON public.tier(permission_schema_id);
CREATE INDEX IF NOT EXISTS idx_tier_twin_class_schema_id ON public.tier(twin_class_schema_id);
CREATE INDEX IF NOT EXISTS idx_tier_twinflow_schema_id ON public.tier(twinflow_schema_id);

-- twin
CREATE INDEX IF NOT EXISTS idx_twin_alias_space_id ON public.twin(alias_space_id);
CREATE INDEX IF NOT EXISTS idx_twin_permission_schema_space_id ON public.twin(permission_schema_space_id);
CREATE INDEX IF NOT EXISTS idx_twin_twin_class_schema_space_id ON public.twin(twin_class_schema_space_id);
CREATE INDEX IF NOT EXISTS idx_twin_twinflow_schema_space_id ON public.twin(twinflow_schema_space_id);
CREATE INDEX IF NOT EXISTS idx_twin_view_permission_id ON public.twin(view_permission_id);

-- twin_action_permission
CREATE INDEX IF NOT EXISTS idx_twin_action_permission_permission_id ON public.twin_action_permission(permission_id);

-- twin_action_validator_rule
CREATE INDEX IF NOT EXISTS idx_twin_action_validator_twin_validator_set_id ON public.twin_action_validator_rule(twin_validator_set_id);

-- win_alias
CREATE INDEX IF NOT EXISTS idx_twin_alias_twin_id ON public.twin_alias(twin_id);

-- twin_attachment
CREATE INDEX IF NOT EXISTS idx_attachment_fieldclass ON public.twin_attachment(twin_class_field_id);
CREATE INDEX IF NOT EXISTS idx_twin_attachment_view_permission_id ON public.twin_attachment(view_permission_id);
CREATE INDEX IF NOT EXISTS idx_twin_attachment_storage_id ON public.twin_attachment(storage_id);
CREATE INDEX IF NOT EXISTS idx_twin_attachment_twin_comment_id ON public.twin_attachment(twin_comment_id);
CREATE INDEX IF NOT EXISTS idx_twin_attachment_created_by_user_id ON public.twin_attachment(created_by_user_id);

-- twin_attachment_action_alien_permission
CREATE INDEX IF NOT EXISTS idx_twin_attachment_action_alien_permission_permission_id ON public.twin_attachment_action_alien_permission(permission_id);

-- twin_attachment_action_alien_validator_rule
CREATE INDEX IF NOT EXISTS idx_twin_attachment_action_alien_validator_rule_validator_set ON public.twin_attachment_action_alien_validator_rule(twin_validator_set_id);

-- twin_attachment_action_self_validator_rule
CREATE INDEX IF NOT EXISTS idx_twin_attachment_action_self_validator_rule_attach_action_id ON public.twin_attachment_action_self_validator_rule(restrict_twin_attachment_action_id);
CREATE INDEX IF NOT EXISTS idx_twin_attachment_action_self_validator_rule_validator_set_id ON public.twin_attachment_action_self_validator_rule(twin_validator_set_id);

-- twin_change_task
CREATE INDEX IF NOT EXISTS idx_twin_change_task_twin_factory_launcher_id ON public.twin_change_task(twin_factory_launcher_id);

-- twin_class
CREATE INDEX IF NOT EXISTS idx_twin_class_comment_attachment_restriction_id ON public.twin_class(comment_attachment_restriction_id);
CREATE INDEX IF NOT EXISTS idx_twin_class_general_attachment_restriction_id ON public.twin_class(general_attachment_restriction_id);
CREATE INDEX IF NOT EXISTS idx_twin_class_twin_class_freeze_id ON public.twin_class(twin_class_freeze_id);
CREATE INDEX IF NOT EXISTS idx_twinclass_create_permission_id ON public.twin_class(create_permission_id);
CREATE INDEX IF NOT EXISTS idx_twinclass_delete_permission_id ON public.twin_class(delete_permission_id);
CREATE INDEX IF NOT EXISTS idx_twinclass_edit_permission_id ON public.twin_class(edit_permission_id);
CREATE INDEX IF NOT EXISTS idx_twinclass_view_permission_id ON public.twin_class(view_permission_id);
CREATE INDEX IF NOT EXISTS idx_twin_class_description_i18n_id ON public.twin_class(description_i18n_id);
CREATE INDEX IF NOT EXISTS idx_twin_class_head_hunter_featurer_id ON public.twin_class(head_hunter_featurer_id);
CREATE INDEX IF NOT EXISTS idx_twin_class_icon_dark_resource_id ON public.twin_class(icon_dark_resource_id);
CREATE INDEX IF NOT EXISTS idx_twin_class_icon_light_resource_id ON public.twin_class(icon_light_resource_id);
CREATE INDEX IF NOT EXISTS idx_twin_class_name_i18n_id ON public.twin_class(name_i18n_id);
CREATE INDEX IF NOT EXISTS idx_twin_class_created_by_user_id ON public.twin_class(created_by_user_id);

-- twin_class_field
CREATE INDEX IF NOT EXISTS idx_twin_class_field_be_validation_error_i18n_id ON public.twin_class_field(be_validation_error_i18n_id);
CREATE INDEX IF NOT EXISTS idx_twin_class_field_description_i18n_id ON public.twin_class_field(description_i18n_id);
CREATE INDEX IF NOT EXISTS idx_twin_class_field_fe_validation_error_i18n_id ON public.twin_class_field(fe_validation_error_i18n_id);
CREATE INDEX IF NOT EXISTS idx_twin_class_field_name_i18n_id ON public.twin_class_field(name_i18n_id);

-- twin_class_field_attribute
CREATE INDEX IF NOT EXISTS idx_twin_class_field_attribute_create_permission_id ON public.twin_class_field_attribute(create_permission_id);
CREATE INDEX IF NOT EXISTS idx_twin_class_field_attribute_delete_permission_id ON public.twin_class_field_attribute(delete_permission_id);
CREATE INDEX IF NOT EXISTS idx_twin_class_field_attribute_note_msg_i18n_id ON public.twin_class_field_attribute(note_msg_i18n_id);
CREATE INDEX IF NOT EXISTS idx_twin_class_field_attribute_update_permission_id ON public.twin_class_field_attribute(update_permission_id);

-- twin_class_field_condition
CREATE INDEX IF NOT EXISTS idx_twin_class_field_condition_logic_operator_id ON public.twin_class_field_condition(logic_operator_id);

-- twin_class_field_rule
CREATE INDEX IF NOT EXISTS idx_twin_class_field_rule_field_overwriter_featurer_id ON public.twin_class_field_rule(field_overwriter_featurer_id);

-- twin_class_field_search
CREATE INDEX IF NOT EXISTS idx_twin_class_field_search_domain_id ON public.twin_class_field_search(domain_id);

-- twin_class_freeze
CREATE INDEX IF NOT EXISTS idx_twin_class_freeze_description_i18n_id ON public.twin_class_freeze(description_i18n_id);
CREATE INDEX IF NOT EXISTS idx_twin_class_freeze_name_i18n_id ON public.twin_class_freeze(name_i18n_id);
CREATE INDEX IF NOT EXISTS idx_twin_class_freeze_twin_status_id ON public.twin_class_freeze(twin_status_id);

-- twin_class_owner_type
CREATE INDEX IF NOT EXISTS idx_description_i18n ON public.twin_class_owner_type(description_i18n_id);
CREATE INDEX IF NOT EXISTS idx_name_i18n ON public.twin_class_owner_type(name_i18n_id);

-- twin_class_schema
CREATE INDEX IF NOT EXISTS idx_twin_class_schema_created_by_user_id ON public.twin_class_schema(created_by_user_id);

-- twin_class_schema_map
CREATE INDEX IF NOT EXISTS idx_twin_class_schema_map_twin_class_id ON public.twin_class_schema_map(twin_class_id);

-- twin_class_search
CREATE INDEX IF NOT EXISTS idx_twin_class_search_domain_id ON public.twin_class_search(domain_id);

-- twin_comment
CREATE INDEX IF NOT EXISTS idx_twin_comment_created_by_user_id ON public.twin_comment(created_by_user_id);

-- twin_comment_action_alien_permission
CREATE INDEX IF NOT EXISTS idx_twin_comment_action_alien_permission_permission_id ON public.twin_comment_action_alien_permission(permission_id);

--twin_comment_action_alien_validator_rule
CREATE INDEX IF NOT EXISTS idx_comment_action_alien_twin_validator_set_id ON public.twin_comment_action_alien_validator_rule(twin_validator_set_id);

-- twin_comment_action_self
CREATE INDEX IF NOT EXISTS idx_twin_comment_action_self_restrict_twin_comment_action_id ON public.twin_comment_action_self(restrict_twin_comment_action_id);

-- twin_factory
CREATE INDEX IF NOT EXISTS idx_twin_factory_name_i18n_id ON public.twin_factory(name_i18n_id);
CREATE INDEX IF NOT EXISTS idx_twin_factory_description_i18n_id ON public.twin_factory(description_i18n_id);

-- twin_factory_condition_set
CREATE INDEX IF NOT EXISTS idx_twin_factory_condition_set_domain_id ON public.twin_factory_condition_set(domain_id);
CREATE INDEX IF NOT EXISTS idx_twin_factory_condition_set_created_by_user_id ON public.twin_factory_condition_set(created_by_user_id);

-- twin_factory_multiplier_filter
CREATE INDEX IF NOT EXISTS idx_twin_factory_multiplier_filter_input_twin_class_id ON public.twin_factory_multiplier_filter(input_twin_class_id);

-- twin_factory_pipeline
CREATE INDEX IF NOT EXISTS idx_twin_factory_pipeline_output_twin_status_id ON public.twin_factory_pipeline(output_twin_status_id);

-- twin_field_simple_non_indexed
CREATE INDEX IF NOT EXISTS idx_twin_field_simple_non_indexed_twin_class_field_id ON public.twin_field_simple_non_indexed(twin_class_field_id);
CREATE INDEX IF NOT EXISTS idx_twin_field_simple_non_indexed_twin_id ON public.twin_field_simple_non_indexed(twin_id);

-- twin_link
CREATE INDEX IF NOT EXISTS idx_twin_link_created_by_user_id ON public.twin_link(created_by_user_id);

-- twin_pointer_validator_rule
CREATE INDEX IF NOT EXISTS idx_twin_pointer_validator_rule_twin_pointer_id ON public.twin_pointer_validator_rule(twin_pointer_id);
CREATE INDEX IF NOT EXISTS idx_twin_pointer_validator_rule_twin_validator_set_id ON public.twin_pointer_validator_rule(twin_validator_set_id);

-- twin_search
CREATE INDEX IF NOT EXISTS idx_twin_search_head_twin_search_id ON public.twin_search(head_twin_search_id);
CREATE INDEX IF NOT EXISTS idx_twin_search_permission_id ON public.twin_search(permission_id);
CREATE INDEX IF NOT EXISTS idx_twin_search_twin_search_alias_id ON public.twin_search(twin_search_alias_id);

-- twin_search_alias
CREATE INDEX IF NOT EXISTS idx_twin_search_alias_twin_search_detector_featurer_id ON public.twin_search_alias(twin_search_detector_featurer_id);

-- twin_search_predicate
CREATE INDEX IF NOT EXISTS idx_twin_search_predicate_twin_finder_featurer_id ON public.twin_search_predicate(twin_finder_featurer_id);

-- twin_status
CREATE INDEX IF NOT EXISTS idx_twin_status_description_i18n_id ON public.twin_status(description_i18n_id);
CREATE INDEX IF NOT EXISTS idx_twin_status_icon_dark_resource_id ON public.twin_status(icon_dark_resource_id);
CREATE INDEX IF NOT EXISTS idx_twin_status_icon_light_resource_id ON public.twin_status(icon_light_resource_id);
CREATE INDEX IF NOT EXISTS idx_twin_status_name_i18n_id ON public.twin_status(name_i18n_id);
CREATE INDEX IF NOT EXISTS idx_twin_status_twin_status_type ON public.twin_status(twin_status_type);

-- twin_status_transition_trigger
CREATE INDEX IF NOT EXISTS idx_twin_status_transition_type_id ON public.twin_status_transition_trigger(twin_status_transition_type_id);

-- twin_validator
CREATE INDEX IF NOT EXISTS idx_twin_validator_twin_validator_set_id ON public.twin_validator(twin_validator_set_id);

-- twin_validator_set
CREATE INDEX IF NOT EXISTS idx_twin_validator_set_domain_id ON public.twin_validator_set(domain_id);

-- twinflow
CREATE INDEX IF NOT EXISTS idx_twinflow_description_i18n_id ON public.twinflow(description_i18n_id);
CREATE INDEX IF NOT EXISTS idx_twinflow_eraseflow_id ON public.twinflow(eraseflow_id);
CREATE INDEX IF NOT EXISTS idx_twinflow_name_i18n_id ON public.twinflow(name_i18n_id);
CREATE INDEX IF NOT EXISTS idx_twinflow_twin_class_id ON public.twinflow(twin_class_id);
CREATE INDEX IF NOT EXISTS idx_twinflow_initial_twin_status_id ON public.twinflow(initial_twin_status_id);
CREATE INDEX IF NOT EXISTS idx_twinflow_created_by_user_id ON public.twinflow(created_by_user_id);

-- twinflow_schema
CREATE INDEX IF NOT EXISTS idx_twinflow_schema_created_by_user_id ON public.twinflow_schema(created_by_user_id);

-- twinflow_transition
CREATE INDEX IF NOT EXISTS idx_twinflow_transition_created_by_user_id ON public.twinflow_transition(created_by_user_id);
CREATE INDEX IF NOT EXISTS idx_twinflow_transition_description_i18n_id ON public.twinflow_transition(description_i18n_id);
CREATE INDEX IF NOT EXISTS idx_twinflow_transition_name_i18n_id ON public.twinflow_transition(name_i18n_id);
CREATE INDEX IF NOT EXISTS idx_twinflow_transition_drafting_twin_factory_id ON public.twinflow_transition(drafting_twin_factory_id);

-- twinflow_transition_validator_rule
CREATE INDEX IF NOT EXISTS idx_twinflow_transition_validator_twin_validator_set_id ON public.twinflow_transition_validator_rule(twin_validator_set_id);

-- user
CREATE INDEX IF NOT EXISTS idx_user_user_status_id ON public.user(user_status_id);

-- user_email_verification
CREATE INDEX IF NOT EXISTS idx_user_email_verification_identity_provider_id ON public.user_email_verification(identity_provider_id);
CREATE INDEX IF NOT EXISTS idx_user_email_verification_user_id ON public.user_email_verification(user_id);

-- user_group
CREATE INDEX IF NOT EXISTS idx_user_group_description_i18n_id ON public.user_group(description_i18n_id);
CREATE INDEX IF NOT EXISTS idx_user_group_name_i18n_id ON public.user_group(name_i18n_id);

-- user_group_act_as_user_involve
CREATE INDEX IF NOT EXISTS idx_user_group_act_as_user_involve_added_by_user_id ON public.user_group_act_as_user_involve(added_by_user_id);

-- user_group_map_type1
CREATE INDEX IF NOT EXISTS idx_user_group_map_type1_added_by_user_id ON public.user_group_map_type1(added_by_user_id);

-- user_group_map_type2
CREATE INDEX IF NOT EXISTS idx_user_group_map_type2_added_by_user_id ON public.user_group_map_type2(added_by_user_id);

-- user_group_map_type3
CREATE INDEX IF NOT EXISTS idx_user_group_map_type3_added_by_user_id ON public.user_group_map_type3(added_by_user_id);

-- user_search
CREATE INDEX IF NOT EXISTS idx_user_search_domain_id ON public.user_search(domain_id);
CREATE INDEX IF NOT EXISTS idx_user_search_user_sorter_featurer_id ON public.user_search(user_sorter_featurer_id);

-- widget
CREATE INDEX IF NOT EXISTS idx_widget_widget_data_grabber_featurer_id ON public.widget(widget_data_grabber_featurer_id);
CREATE INDEX IF NOT EXISTS idx_widget_widget_accessor_featurer_id ON public.widget(widget_accessor_featurer_id);