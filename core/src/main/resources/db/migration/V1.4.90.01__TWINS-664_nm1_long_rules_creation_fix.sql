create index if not exists twin_class_field_rule_map_twin_class_field_id_idx
    on twin_class_field_rule_map (twin_class_field_id);

create index if not exists twin_class_field_rule_map_twin_class_field_rule_id_idx
    on twin_class_field_rule_map (twin_class_field_rule_id);

create index twin_class_field_condition_base_twin_class_field_id_index
    on twin_class_field_condition (base_twin_class_field_id);

create index twin_class_field_condition_condition_evaluator_featurer_id_idx
    on twin_class_field_condition (condition_evaluator_featurer_id);

create index twin_class_field_condition_twin_class_field_rule_id_idx
    on twin_class_field_condition (twin_class_field_rule_id, parent_twin_class_field_condition_id);
