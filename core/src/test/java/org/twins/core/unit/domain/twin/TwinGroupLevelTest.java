package org.twins.core.unit.domain.twin;

import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.Test;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.twin.TwinGroupLevel;
import org.twins.core.featurer.fieldtyper.value.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class TwinGroupLevelTest {
    @Test
    void selectValueUsesEachOptionOnceAndAMissingValueIsTheNullKey() throws Exception {
        UUID fieldId = UUID.fromString("95f2c8ca-8b79-4971-9726-270a11c7d031");
        UUID option = UUID.randomUUID();
        UUID otherOption = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);
        TwinGroupLevel level = level(field, new FieldValueSelectSingle(field));

        // a twin of a class without the field has no value in the kit; the empty key keeps the level's type
        Collection<FieldValueSingle> missing = level.extractKeys(new TwinEntity().setId(UUID.randomUUID()));
        assertEquals(Collections.singletonList(null), scalars(missing));
        assertInstanceOf(FieldValueSelectSingle.class, single(missing));

        TwinEntity task = twinWithValue(new FieldValueSelect(field).setItems(List.of(
                new DataListOptionEntity().setId(option),
                new DataListOptionEntity().setId(option),
                new DataListOptionEntity().setId(otherOption))));
        Collection<FieldValueSingle> keys = level.extractKeys(task);
        assertEquals(List.of(option, otherOption), scalars(keys));
        FieldValueSelectSingle keyValue = assertInstanceOf(FieldValueSelectSingle.class, keys.iterator().next());
        // the option rides in the key value: no second load is needed downstream
        assertEquals(option, keyValue.getValue().getId());
    }

    @Test
    void decimalAndTextValuesKeepTheirFormattedStringKey() throws Exception {
        UUID fieldId = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);
        TwinEntity twin = twinWithValue(new FieldValueText(field).setValue("0.00"));

        // a decimal deserializes to FieldValueText; the formatted string is the group key
        assertEquals(List.of("0.00"), scalars(level(field, new FieldValueText(field)).extractKeys(twin)));
    }

    @Test
    void dateValueIsUsedAsTheKey() throws Exception {
        UUID fieldId = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);
        LocalDateTime date = LocalDateTime.of(2026, 1, 1, 0, 0);
        TwinEntity twin = twinWithValue(new FieldValueDate(field, null).setDate(date));

        assertEquals(List.of(date), scalars(level(field, new FieldValueDate(field, null)).extractKeys(twin)));
    }

    @Test
    void linkFieldValueUsesEachDstOnceAndAMissingValueIsTheNullKey() throws Exception {
        UUID fieldId = UUID.randomUUID();
        UUID dst = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);
        TwinGroupLevel level = level(field, new FieldValueLinkSingle(field));

        TwinEntity twin = twinWithValue(new FieldValueLink(field)
                .setItems(List.of(new TwinEntity().setId(dst), new TwinEntity().setId(dst))));
        Collection<FieldValueSingle> keys = level.extractKeys(twin);
        assertEquals(List.of(dst), scalars(keys));
        // the far twin rides in the key value: no second load is needed downstream
        FieldValueLinkSingle keyValue = assertInstanceOf(FieldValueLinkSingle.class, single(keys));
        assertEquals(dst, keyValue.getValue().getId());

        TwinEntity undefined = twinWithValue(new FieldValueLink(field).undefine());
        assertEquals(Collections.singletonList(null), scalars(level.extractKeys(undefined)));

        // no kit at all (a twin of a class without the field)
        assertEquals(Collections.singletonList(null), scalars(level.extractKeys(new TwinEntity().setId(UUID.randomUUID()))));
    }

    @Test
    void basicFieldKeyStartsAsAnIdOnlyReference() throws Exception {
        UUID assigneeId = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(UUID.randomUUID());
        TwinGroupLevel level = new TwinGroupLevel()
                .setFieldId(field.getId())
                .setBasicField(TwinEntity.BasicField.ASSIGNEE_USER_ID)
                .setField(field)
                .setKeyPrototype(new FieldValueUserSingle(field));

        Collection<FieldValueSingle> keys = level.extractKeys(new TwinEntity().setId(UUID.randomUUID()).setAssignerUserId(assigneeId));
        assertEquals(List.of(assigneeId), scalars(keys));
        FieldValueReferenceSingle reference = assertInstanceOf(FieldValueReferenceSingle.class, single(keys));
        assertEquals(FieldValueUserSingle.class, reference.getValueType());
        assertEquals(assigneeId, reference.getId());

        // an unassigned twin groups under the empty key of the level's type
        Collection<FieldValueSingle> empty = level.extractKeys(new TwinEntity().setId(UUID.randomUUID()));
        assertEquals(Collections.singletonList(null), scalars(empty));
        assertInstanceOf(FieldValueUserSingle.class, single(empty));
    }

    private static TwinGroupLevel level(TwinClassFieldEntity field, FieldValue keyPrototype) {
        return new TwinGroupLevel()
                .setFieldId(field.getId())
                .setField(field)
                .setKeyPrototype(keyPrototype);
    }

    private static List<Object> scalars(Collection<FieldValueSingle> values) {
        List<Object> scalars = new ArrayList<>();
        for (FieldValueSingle value : values) {
            scalars.add(value.getScalarValue());
        }
        return scalars;
    }

    private static FieldValueSingle single(Collection<FieldValueSingle> values) {
        assertEquals(1, values.size());
        return values.iterator().next();
    }

    private static TwinEntity twinWithValue(FieldValue value) {
        TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
        twin.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId));
        twin.getFieldValuesKit().add(value);
        return twin;
    }
}
