CREATE TABLE IF NOT EXISTS face_tc002 (
                            id UUID PRIMARY KEY NOT NULL,
                            face_id UUID NOT NULL REFERENCES face(id),
                            twin_pointer_validator_rule_id UUID REFERENCES twin_pointer_validator_rule,
                            key VARCHAR NOT NULL,
                            class_selector_label_i18n_id UUID REFERENCES i18n ON UPDATE CASCADE ON DELETE RESTRICT,
                            save_button_label_i18n_id UUID REFERENCES i18n ON UPDATE CASCADE ON DELETE RESTRICT,
                            header_i18n_id UUID REFERENCES i18n ON UPDATE CASCADE ON DELETE RESTRICT,
                            header_icon_resource_id UUID REFERENCES resource ON UPDATE CASCADE ON DELETE RESTRICT,
                            style_classes VARCHAR,
                            twin_class_id UUID NOT NULL REFERENCES twin_class ON UPDATE CASCADE ON DELETE CASCADE,
                            extends_depth INTEGER NOT NULL DEFAULT 0,
                            head_twin_pointer_id UUID REFERENCES twin_pointer,
                            field_finder_featurer_id INTEGER NOT NULL REFERENCES featurer,
                            field_finder_params HSTORE
);

CREATE INDEX IF NOT EXISTS face_tc002_face_id_idx ON face_tc002(face_id);
CREATE INDEX IF NOT EXISTS face_tc002_validator_rule_idx ON face_tc002(twin_pointer_validator_rule_id);
CREATE INDEX IF NOT EXISTS face_tc002_key_idx ON face_tc002(key);
CREATE INDEX IF NOT EXISTS face_tc002_twin_class_idx ON face_tc002(twin_class_id);
CREATE INDEX IF NOT EXISTS face_tc002_head_pointer_idx ON face_tc002(head_twin_pointer_id);
CREATE INDEX IF NOT EXISTS face_tc002_featurer_idx ON face_tc002(field_finder_featurer_id);