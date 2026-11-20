package org.twins.core.unit.service.twin;

import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.junit.jupiter.api.Test;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDataListEntity;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twin.TwinFieldTimestampEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDatalist;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDecimal;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTimestamp;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.service.twin.TwinGroupKeyValues;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class TwinGroupKeyValuesTest {
    @Test
    void dataListWithoutARowIsTheNullKeyAndRepeatedOptionsCollapse() throws Exception {
        UUID fieldId = UUID.fromString("95f2c8ca-8b79-4971-9726-270a11c7d031");
        UUID option = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);
        TwinFieldStorageDatalist storage = mock(TwinFieldStorageDatalist.class);
        TwinEntity empty = new TwinEntity().setId(UUID.randomUUID());
        assertEquals(Collections.singletonList(null), TwinGroupKeyValues.dynamic(empty, field, storage));

        TwinEntity task = new TwinEntity().setId(UUID.randomUUID());
        task.setTwinFieldDatalistKit(new KitGrouped<>(
                List.of(
                        row(fieldId, option),
                        row(fieldId, option),
                        row(fieldId, null)),
                TwinFieldDataListEntity::getId,
                TwinFieldDataListEntity::getTwinClassFieldId));
        assertEquals(Arrays.asList(option, null), TwinGroupKeyValues.dynamic(task, field, storage));
    }

    @Test
    void decimalZeroWithDifferentScaleIsOneKey() throws Exception {
        UUID fieldId = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);
        TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
        twin.setTwinFieldDecimalKit(new Kit<>(
                List.of(new TwinFieldDecimalEntity().setId(UUID.randomUUID()).setTwinClassFieldId(fieldId).setValue(new BigDecimal("0.00"))),
                TwinFieldDecimalEntity::getTwinClassFieldId));
        assertEquals(List.of(BigDecimal.ZERO), TwinGroupKeyValues.dynamic(twin, field, mock(TwinFieldStorageDecimal.class)));
    }

    @Test
    void timestampKeyIsLocalDateTimeWithoutTzShift() throws Exception {
        UUID fieldId = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);
        TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
        twin.setTwinFieldTimestampKit(new Kit<>(
                List.of(new TwinFieldTimestampEntity().setId(UUID.randomUUID()).setTwinClassFieldId(fieldId)
                        .setValue(Timestamp.valueOf("2026-01-01 00:00:00"))),
                TwinFieldTimestampEntity::getTwinClassFieldId));
        assertEquals(List.of(LocalDateTime.of(2026, 1, 1, 0, 0)),
                TwinGroupKeyValues.dynamic(twin, field, mock(TwinFieldStorageTimestamp.class)));
    }

    @Test
    void linkFieldValueUsesEachDstOnceAndAMissingValueIsTheNullKey() {
        UUID fieldId = UUID.randomUUID();
        UUID dst = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);
        FieldValueLink value = new FieldValueLink(field);
        value.setItems(List.of(new TwinEntity().setId(dst), new TwinEntity().setId(dst)));
        assertEquals(List.of(dst), TwinGroupKeyValues.linkEnds(value));
        assertEquals(Collections.singletonList(null), TwinGroupKeyValues.linkEnds(new FieldValueLink(field).undefine()));
        assertEquals(Collections.singletonList(null), TwinGroupKeyValues.linkEnds(null));
    }

    private static TwinFieldDataListEntity row(UUID fieldId, UUID optionId) {
        return new TwinFieldDataListEntity()
                .setId(UUID.randomUUID())
                .setTwinClassFieldId(fieldId)
                .setDataListOptionId(optionId);
    }
}
