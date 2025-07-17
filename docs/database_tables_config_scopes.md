TWINS
DOMAIN
USER
MIXED
AUTO


| TABLE                                       |  SCOPE   |  COMMENT  |  CREATOR  |   DOMAIN   |
|---------------------------------------------|:--------:|:---------:|:---------:|:----------:|
| business_account                            |   USER   |           |     -     |            |
| business_account_user                       |   USER   |           |     -     |            |
| channel                                     |  TWINS   |           |     -     |            |
| cud                                         |  TWINS   |           |     -     |            |
| data_list                                   |  MIXED   |           |  need_s   |     +      |
| data_list_option                            |  MIXED   |           | data_list | data_list  |
| data_list_option_status                     |  MIXED   |           | data_list | data_list  |
| data_list_subset                            |  MIXED   |           | data_list | data_list  |
| data_list_subset_option                     |  MIXED   |           | data_list | data_list  |
| domain                                      |  DOMAIN  |           |     -     |            |
| domain_business_account                     |  USER?   |           |     -     |            |
| domain_locale                               |  DOMAIN  |           |     -     |            |
| domain_status                               |  TWINS   |           |     -     |            |
| domain_type                                 |  TWINS   |           |     -     |            |
| domain_type_twin_class_owner_type           |  TWINS   |           |     -     |            |
| domain_user                                 |   USER   |           |     -     |            |
| draft                                       |   USER   |           |  create-  |     +      |
| draft_history                               |   USER   |           |   actor   |   draft    |
| draft_status                                |  TWINS   |           |     -     |   draft    |
| draft_twin_attachment                       |   USER   |           |     -     |   draft    |
| draft_twin_erase                            |   USER   |           |     -     |   draft    |
| draft_twin_erase_status                     |  TWINS   |           |     -     |            |
| draft_twin_field_boolean                    |   USER   |           |     -     |   draft    |
| draft_twin_field_data_list                  |   USER   |           |     -     |   draft    |
| draft_twin_field_simple                     |   USER   |           |     -     |   draft    |
| draft_twin_field_simple_non_indexed         |   USER   |           |     -     |   draft    |
| draft_twin_field_user                       |   USER   |           |     -     |   draft    |
| draft_twin_link                             |   USER   |           |     -     |   draft    |
| draft_twin_marker                           |   USER   |           |     -     |   draft    |
| draft_twin_persist                          |   USER   |           |     -     |   draft    |
| draft_twin_tag                              |   USER   |           |     -     |   draft    |
| email_sender                                |  DOMAIN  |           |  need_s   |            |
| eraseflow                                   |  DOMAIN  |           |  create+  |            |
| eraseflow_link_cascade                      |  DOMAIN  |           |  create+  |            |
| error                                       |  DOMAIN  |           |  need_s   |            |
| event                                       |  TWINS   |           |     -     |            |
| face                                        |  DOMAIN  |           |  create+  |     +      |
| face_component                              |  TWINS   |           |     -     |            |
| face_component_type                         |  TWINS   |           |     -     |            |
| face_navbar_nb001                           |  DOMAIN  |           |     -     |    face    |
| face_navbar_nb001_menu_item                 |  DOMAIN  |           |     -     |    face    |
| face_navbar_nb001_menu_item_status          | DOMAIN?  |           |     -     |    face    |
| face_pg001                                  |  DOMAIN  |           |     -     |    face    |
| face_pg001_widget                           |  DOMAIN  |           |     -     |    face    |
| face_pg002                                  |  DOMAIN  |           |     -     |    face    |
| face_pg002_layout                           |  DOMAIN  |           |     -     |    face    |
| face_pg002_tab                              |  DOMAIN  |           |     -     |    face    |
| face_pg002_widget                           |  DOMAIN  |           |     -     |    face    |
| face_tc001                                  |  DOMAIN  |           |     -     |    face    |
| face_tc002                                  |  DOMAIN  |           |     -     |    face    |
| face_tc002_option                           |  DOMAIN  |           |     -     |    face    |
| face_tw001                                  |  DOMAIN  |           |     -     |    face    |
| face_tw002                                  |  DOMAIN  |           |     -     |    face    |
| face_tw002_accordion_item                   |  DOMAIN  |           |     -     |    face    |
| face_tw004                                  |  DOMAIN  |           |     -     |    face    |
| face_tw005                                  |  DOMAIN  |           |     -     |    face    |
| face_tw005_button                           |  DOMAIN  |           |     -     |    face    |
| face_wt001                                  |  DOMAIN  |           |     -     |    face    |
| face_wt001_column                           |  DOMAIN  |           |     -     |    face    |
| face_wt002                                  |  DOMAIN  |           |     -     |    face    |
| face_wt002_button                           |  DOMAIN  |           |     -     |    face    |
| face_wt003                                  |  DOMAIN  |           |     -     |    face    |
| featurer                                    |  TWINS   |           |     -     |            |
| featurer_injection                          |  TWINS   |           |     -     |            |
| featurer_param                              |  TWINS   |           |     -     |            |
| featurer_param_type                         |  TWINS   |           |     -     |            |
| featurer_type                               |  TWINS   |           |     -     |            |
| flyway_schema_history                       |   AUTO   |           |     -     |            |
| history                                     |   USER   |           |   actor   |            |
| history_type                                |  TWINS   |           |     -     |            |
| history_type_config_domain                  |  DOMAIN  |           |     -     |     +      |
| history_type_config_twin_class              |  DOMAIN  |           |     -     | twin_class |
| history_type_config_twin_class_field        |  DOMAIN  |           |     -     |   tcf-tc   |
| history_type_domain_template                |  DOMAIN  |           |     -     |     +      |
| history_type_status                         |  TWINS   |           |     -     |            |
| i18n                                        | T/MIXED  |           |  need_s   |     +      |
| i18n_locale                                 |  DOMAIN  |           |     -     |            |
| i18n_translation                            | T/MIXED  |           |     -     |    i18n    |
| i18n_translation_bin                        |  MIXED?  |           |     -     |    i18n    |
| i18n_translation_style                      | DOMAIN?  |           |     -     |    i18n    |
| i18n_type                                   |  TWINS   |           |     -     |            |
| identity_provider                           |  TWINS   |           |     -     |            |
| identity_provider_internal_token            |  DOMAIN  |           |  need_s   |            |
| identity_provider_internal_user             |  DOMAIN  |           |  need_s   |            |
| identity_provider_status                    |  TWINS   |           |     -     |            |
| link                                        |  DOMAIN  |           |  create+  |     +      |
| link_strength                               |  TWINS   |           |     -     |            |
| link_tree                                   |  DOMAIN  |    ???    |  create+  |     +      |
| link_tree_node                              |  DOMAIN  |           |     -     | link, tree |
| link_trigger                                |  DOMAIN  |    ???    |     -     |            |
| link_type                                   |  TWINS   |           |     -     |            |
| link_validator                              |  DOMAIN  |    ???    |     -     |            |
| notification_email                          |  DOMAIN  |           |     -     |     +      |
| notification_mode                           |  TWINS   |           |     -     |            |
| permission                                  | T/MIXED  |           |     -     |            |
| permission_grant_assignee_propagation       |  DOMAIN  |           |     -     |  p_shcema  |
| permission_grant_global                     |  DOMAIN  |           |     -     |  p_shcema  |
| permission_grant_space_role                 |  DOMAIN  |           |     -     |  p_shcema  |
| permission_grant_twin_role                  |  DOMAIN  |           |     -     |  p_shcema  |
| permission_grant_user                       |  DOMAIN  |           |     -     |  p_shcema  |
| permission_grant_user_group                 |  DOMAIN  |           |     -     |  p_shcema  |
| permission_group                            | T/DOMAIN |           |     -     |     +      |
| permission_schema                           |  DOMAIN  |           |     -     |     +      |
| resource                                    |  DOMAIN  |           |  upload   |     +      |
| search                                      |  DOMAIN  |           |     -     |     sa     |
| search_alias                                |  DOMAIN  |           |     -     |     +      |
| search_field                                |  TWINS   |           |     -     |            |
| search_predicate                            |  DOMAIN  |           |     -     |    s-sa    |
| space                                       |   USER   |           |     -     |            |
| space_role                                  |  DOMAIN  |           |     -     |            |
| space_role_user                             |  USER?   |           |  create-  |            |
| space_role_user_group                       |  USER?   |           |  create-  |            |
| storage                                     | T/DOMAIN |           |    -?     |     +      |
| template_generator                          | T/DOMAIN |           |    -?     |     +      |
| tier                                        |  DOMAIN  |           |  need_s   |     +      |
| touch                                       |  TWINS   |           |     -     |            |
| twin                                        |   USER   |           |     -     |            |
| twin_action                                 |  TWINS   |           |     -     |            |
| twin_action_permission                      |  DOMAIN  |           |    -?     |     tc     |
| twin_action_validator_rule                  |  DOMAIN  |           |    -?     |     tc     |
| twin_alias                                  |   USER   |           |     -     |            |
| twin_alias_type                             |  TWINS   |           |     -     |            |
| twin_attachment                             |   USER   |           |     -     |            |
| twin_attachment_action                      |  TWINS   |           |     -     |            |
| twin_attachment_action_alien_permission     |  DOMAIN  |           |     -     |     tc     |
| twin_attachment_action_alien_validator_rule |  DOMAIN  |           |     -     |     tc     |
| twin_attachment_action_self_validator_rule  |  DOMAIN  |           |     -     |     tc     |
| twin_attachment_modification                |   USER   |           |     -     |            |
| twin_attachment_restriction                 |  DOMAIN  |           |     -     |     +      |
| twin_business_account_alias_counter         |   USER   |           |     -     |            |
| twin_class                                  |  MIXED   |           |  create+  |     +      |
| twin_class_field                            |  MIXED   |           |  need_s   |     tc     |
| twin_class_owner_type                       |  TWINS   |           |     -     |            |
| twin_class_schema                           |  DOMAIN  |           |  create+  |     +      |
| twin_class_schema_map                       |  DOMAIN  |           |     -     | schema,tc  |
| twin_comment                                |   USER   |           |     -     |            |
| twin_comment_action                         |  TWINS   |           |     -     |            |
| twin_comment_action_alien_permission        |  DOMAIN  |           |     -     |     tc     |
| twin_comment_action_alien_validator_rule    |  DOMAIN  |           |     -     |     tc     |
| twin_comment_action_self                    |  DOMAIN  |           |     -     |     tc     |
| twin_erase_reason                           |  TWINS   |           |     -     |            |
| twin_factory                                |  DOMAIN  |           |  create+  |     +      |
| twin_factory_branch                         |  DOMAIN  |           |     -     |     tf     |
| twin_factory_condition                      |  DOMAIN  |           |     -     |    tfcs    |
| twin_factory_condition_set                  |  DOMAIN  |           |  create+  |     +      |
| twin_factory_eraser                         |  DOMAIN  |           |     -     |     tf     |
| twin_factory_eraser_action                  |  TWINS   |           |     -     |            |
| twin_factory_multiplier                     |  DOMAIN  |           |     -     |     tf     |
| twin_factory_multiplier_filter              |  DOMAIN  |           |     -     |   tfm-tf   |
| twin_factory_pipeline                       |  DOMAIN  |           |     -     |     tf     |
| twin_factory_pipeline_step                  |  DOMAIN  |           |     -     |   tfp-tf   |
| twin_field_boolean                          |   USER   |           |     -     |            |
| twin_field_data_list                        |   USER   |           |     -     |            |
| twin_field_i18n                             |   USER   |           |     -     |            |
| twin_field_simple                           |   USER   |           |     -     |            |
| twin_field_simple_non_indexed               |   USER   |           |     -     |            |
| twin_field_user                             |   USER   |           |     -     |            |
| twin_link                                   |   USER   |           |  create-  |            |
| twin_marker                                 |   USER   |           |     -     |            |
| twin_pointer                                |  DOMAIN  |     ?     |     -     |     tc     |
| twin_pointer_validator_rule                 |  DOMAIN  |           |     -     |     tp     |
| twin_role                                   |  TWINS   |           |     -     |            |
| twin_status                                 | DOMAIN?M |           |  need_s   |     tc     |
| twin_status_group                           |   ???    |    ???    |     -     |            |
| twin_status_group_map                       |   ???    |    ???    |     -     |            |
| twin_status_transition_trigger              |  DOMAIN  |           |     -     |   ts-tc    |
| twin_status_transition_type                 |  TWINS   | ???iswork |     -     |            |
| twin_tag                                    |   USER   |           |     -     |            |
| twin_touch                                  |  TWINS   |           |     -     |            |
| twin_validator                              |  DOMAIN  |           |    -?     |            |
| twin_validator_set                          |  DOMAIN  |           |    -?     |     +      |
| twin_work                                   |   USER   |    ???    |  author   |            |
| twinflow                                    |  DOMAIN  |           |  create+  | tc,sch_map |
| twinflow_schema                             |  DOMAIN  |           |  create+  |     +      |
| twinflow_schema_map                         |  DOMAIN  |           |     -     |            |
| twinflow_transition                         |  DOMAIN  |           |  create+  | tf, alias  |
| twinflow_transition_alias                   |  DOMAIN  |           |     -     |     +      |
| twinflow_transition_trigger                 |  DOMAIN  |           |     -     |   tt-tf    |
| twinflow_transition_type                    |  TWINS   |  ???358   |     -     |            |
| twinflow_transition_validator_rule          |  DOMAIN  |           |     -     |   tt-tf    |
| user                                        |   USER   |           |     -     |            |
| user_email_verification                     |   USER   |           |     -     |            |
| user_group                                  |  DOMAIN  |           |  added?   |     +      |
| user_group_act_as_user_involve              |  DOMAIN  |           |  added?   |     +      |
| user_group_map_type1                        |  DOMAIN  |           |  added?   |     ug     |
| user_group_map_type2                        |  DOMAIN  |           |  added?   |     ug     |
| user_group_map_type3                        |  DOMAIN  |           |  added?   |    ug,+    |
| user_group_type                             |  TWINS   |           |     -     |            |
| user_status                                 |  TWINS   |           |     -     |            |
