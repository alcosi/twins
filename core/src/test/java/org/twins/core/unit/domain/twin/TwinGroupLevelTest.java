package org.twins.core.unit.domain.twin;

import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.Test;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.twin.TwinGroupLevel;
import org.twins.core.enums.twin.TwinGroupKeyType;
import org.twins.core.featurer.fieldtyper.value.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TwinGroupLevelTest {
    @Test
    void selectValueUsesEachOptionOnceAndAMissingValueIsTheNullKey() throws Exception {
        UUID fieldId = UUID.fromString("95f2c8ca-8b79-4971-9726-270a11c7d031");
        UUID option = UUID.randomUUID();
        UUID otherOption = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);
        TwinGroupLevel level = level(fieldId, TwinGroupKeyType.DATA_LIST_OPTION_FIELD);

        // a twin of a class without the field has no value in the kit
        assertEquals(Collections.singletonList(null), level.extractKeys(new TwinEntity().setId(UUID.randomUUID())));

        TwinEntity task = twinWithValue(new FieldValueSelect(field).setItems(List.of(
                new DataListOptionEntity().setId(option),
                new DataListOptionEntity().setId(option),
                new DataListOptionEntity().setId(otherOption))));
        assertEquals(List.of(option, otherOption), level.extractKeys(task));
    }

    @Test
    void decimalValueParsesFromItsTextFormToAScaleIndependentKey() throws Exception {
        UUID fieldId = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);
        TwinEntity twin = twinWithValue(new FieldValueText(field).setValue("0.00"));

        assertEquals(List.of(BigDecimal.ZERO), level(fieldId, TwinGroupKeyType.DECIMAL).extractKeys(twin));
        // a plain text key stays the string it is
        assertEquals(List.of("0.00"), level(fieldId, TwinGroupKeyType.TEXT).extractKeys(twin));
    }

    @Test
    void dateValueIsUsedAsTheKey() throws Exception {
        UUID fieldId = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);
        LocalDateTime date = LocalDateTime.of(2026, 1, 1, 0, 0);
        TwinEntity twin = twinWithValue(new FieldValueDate(field, null).setDate(date));

        assertEquals(List.of(date), level(fieldId, TwinGroupKeyType.TIMESTAMP).extractKeys(twin));
    }

    @Test
    void linkFieldValueUsesEachDstOnceAndAMissingValueIsTheNullKey() throws Exception {
        UUID fieldId = UUID.randomUUID();
        UUID dst = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);
        TwinGroupLevel level = level(fieldId, TwinGroupKeyType.TWIN);

        TwinEntity twin = twinWithValue(new FieldValueLink(field)
                .setItems(List.of(new TwinEntity().setId(dst), new TwinEntity().setId(dst))));
        assertEquals(List.of(dst), level.extractKeys(twin));

        TwinEntity undefined = twinWithValue(new FieldValueLink(field).undefine());
        assertEquals(Collections.singletonList(null), level.extractKeys(undefined));

        // no kit at all (a twin of a class without the field)
        assertEquals(Collections.singletonList(null), level.extractKeys(new TwinEntity().setId(UUID.randomUUID())));
    }

    private static TwinGroupLevel level(UUID fieldId, TwinGroupKeyType keyType) {
        return new TwinGroupLevel()
                .setFieldId(fieldId)
                .setKeyType(keyType)
                .setDynamicField(new TwinClassFieldEntity().setId(fieldId));
    }

    private static TwinEntity twinWithValue(FieldValue value) {
        TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
        twin.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId));
        twin.getFieldValuesKit().add(value);
        return twin;
    }
}
