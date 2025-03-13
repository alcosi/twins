CREATE TABLE twin_field_i18n (
  id UUID PRIMARY KEY,
  twin_id UUID NOT NULL REFERENCES twin(id) ON DELETE CASCADE,
  twin_class_field_id UUID NOT NULL REFERENCES twin_class_field(id) ON DELETE CASCADE,
  i18n_id UUID NOT NULL REFERENCES i18n(id) ON DELETE CASCADE,
  UNIQUE (twin_id, twin_class_field_id)
);