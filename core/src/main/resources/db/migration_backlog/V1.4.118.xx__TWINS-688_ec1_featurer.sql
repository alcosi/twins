CREATE TABLE IF NOT EXISTS twin_field_decimal
(
    id                  UUID PRIMARY KEY,
    twin_id             UUID NOT NULL REFERENCES twin (id) ON UPDATE CASCADE ON DELETE CASCADE,
    twin_class_field_id UUID NOT NULL REFERENCES twin_class_field (id) ON UPDATE CASCADE ON DELETE CASCADE,
    value               DECIMAL
);

CREATE INDEX IF NOT EXISTS twin_field_decimal_twin_class_field_id_value_index
    ON twin_field_decimal (twin_class_field_id, value);
CREATE UNIQUE INDEX IF NOT EXISTS twin_field_decimal_twin_class_field_id_twin_id_uindex
    ON twin_field_decimal (twin_id, twin_class_field_id);

update featurer
set class = 'org.twins.core.featurer.fieldtyper.FieldTyperDecimal', name = 'Decimal', description = 'Decimal field with dedicated table storage'
where id=1317;
