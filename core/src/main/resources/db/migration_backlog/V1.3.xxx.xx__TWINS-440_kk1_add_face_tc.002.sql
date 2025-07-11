INSERT INTO public.face_component (id, face_component_type_id, name, description)
VALUES ('TC002', 'TWIN_CREATE', 'Create twin sketch modal widget', null)
    ON CONFLICT (id) DO NOTHING;

CREATE TABLE IF NOT EXISTS face_tc002 (
    id UUID PRIMARY KEY NOT NULL,
    face_id UUID NOT NULL REFERENCES face(id),
    twin_pointer_validator_rule_id UUID REFERENCES twin_pointer_validator_rule,
    key VARCHAR NOT NULL,
    save_button_label_i18n_id UUID REFERENCES i18n ON UPDATE CASCADE ON DELETE RESTRICT,
    header_i18n_id UUID REFERENCES i18n ON UPDATE CASCADE ON DELETE RESTRICT,
    header_icon_resource_id UUID REFERENCES resource ON UPDATE CASCADE ON DELETE RESTRICT,
    style_classes VARCHAR
    );

CREATE TABLE IF NOT EXISTS face_tc002_option (
    id UUID PRIMARY KEY NOT NULL,
    face_tc002_id UUID NOT NULL REFERENCES face_tc002(id) ON UPDATE CASCADE ON DELETE CASCADE,
    twin_pointer_validator_rule_id UUID REFERENCES twin_pointer_validator_rule,
    class_selector_label_i18n_id UUID REFERENCES i18n ON UPDATE CASCADE ON DELETE RESTRICT,
    twin_class_id UUID NOT NULL REFERENCES twin_class ON UPDATE CASCADE ON DELETE CASCADE,
    extends_depth INTEGER NOT NULL DEFAULT 0,
    head_twin_pointer_id UUID REFERENCES twin_pointer,
    field_finder_featurer_id INTEGER NOT NULL REFERENCES featurer,
    field_finder_params HSTORE
    );

CREATE INDEX IF NOT EXISTS face_tc002_face_id_idx ON face_tc002(face_id);
CREATE INDEX IF NOT EXISTS face_tc002_validator_rule_idx ON face_tc002(twin_pointer_validator_rule_id);
CREATE INDEX IF NOT EXISTS face_tc002_key_idx ON face_tc002(key);

CREATE INDEX IF NOT EXISTS face_tc002_option_face_tc002_idx ON face_tc002_option(face_tc002_id);
CREATE INDEX IF NOT EXISTS face_tc002_option_class_selector_idx ON face_tc002_option(class_selector_label_i18n_id);
CREATE INDEX IF NOT EXISTS face_tc002_option_twin_class_idx ON face_tc002_option(twin_class_id);
CREATE INDEX IF NOT EXISTS face_tc002_option_head_pointer_idx ON face_tc002_option(head_twin_pointer_id);
CREATE INDEX IF NOT EXISTS face_tc002_option_featurer_idx ON face_tc002_option(field_finder_featurer_id);