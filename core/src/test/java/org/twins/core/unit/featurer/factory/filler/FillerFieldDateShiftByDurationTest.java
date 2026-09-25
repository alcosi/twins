package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.filler.FillerFieldDateShiftByDuration;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromItemOutputFields;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.factory.lookuper.LookupResult;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class FillerFieldDateShiftByDurationTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;
    @Mock
    private TwinClassFieldService twinClassFieldService;
    @Mock
    private FieldLookupers fieldLookupers;
    @Mock
    private FieldLookuperFromItemOutputFields lookuper;

    private FillerFieldDateShiftByDuration filler;

    private static final UUID TARGET_ID = UUID.randomUUID();
    private static final UUID SOURCE_ID = UUID.randomUUID();
    private static final UUID DURATION_ID = UUID.randomUUID();
    private static final TwinClassFieldEntity TARGET_FIELD = new TwinClassFieldEntity().setId(TARGET_ID);
    private static final TwinClassFieldEntity SOURCE_FIELD = new TwinClassFieldEntity().setId(SOURCE_ID);
    private static final TwinClassFieldEntity DURATION_FIELD = new TwinClassFieldEntity().setId(DURATION_ID);

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerFieldDateShiftByDuration(twinService, twinClassFieldService);
        inject(filler, "fieldLookupers", fieldLookupers);
        when(fieldLookupers.getByType(FieldLookupers.Type.fromItemOutputFields)).thenReturn(lookuper);
        lenient().when(twinClassFieldService.findEntitySafe(TARGET_ID)).thenReturn(TARGET_FIELD);
        lenient().when(twinClassFieldService.findEntitySafe(SOURCE_ID)).thenReturn(SOURCE_FIELD);
        lenient().when(twinClassFieldService.findEntitySafe(DURATION_ID)).thenReturn(DURATION_FIELD);
    }

    private void inject(Object target, String name, Object value) throws Exception {
        Field f = findField(target.getClass(), name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private Field findField(Class<?> clazz, String name) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("field not found: " + name);
    }

    private Properties props(boolean subtract) {
        var p = new Properties();
        p.setProperty("targetTwinClassFieldId", TARGET_ID.toString());
        p.setProperty("fieldLookuper", "fromItemOutputFields");
        p.setProperty("sourceDateTwinClassFieldId", SOURCE_ID.toString());
        p.setProperty("durationTwinClassFieldId", DURATION_ID.toString());
        p.setProperty("subtractDuration", Boolean.toString(subtract));
        return p;
    }

    private FactoryItem buildFactoryItem() {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity().setId(UUID.randomUUID()));
        return new FactoryItem().setOutput(output);
    }

    private FieldValueDate date(UUID id, LocalDateTime value) {
        var field = new FieldValueDate(new TwinClassFieldEntity().setId(id), "yyyy-MM-dd");
        if (value != null)
            field.setDate(value);
        return field;
    }

    private FieldValueText duration(String value) {
        var text = new FieldValueText(new TwinClassFieldEntity().setId(DURATION_ID));
        if (value != null)
            text.setValue(value);
        return text;
    }

    private LookupResult result(FactoryItem item, FieldValue value) {
        LookupResult result = LookupResult.empty(1);
        result.values().put(item, value);
        return result;
    }

    @Nested
    class Fill {
        @Test
        void fill_plusInclusiveDays() throws ServiceException {
            var factoryItem = buildFactoryItem();
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
            when(lookuper.lookupFieldValue(any(FactoryItemsBatch.class), eq(TARGET_FIELD))).thenReturn(result(factoryItem, date(TARGET_ID, null)));
            when(lookuper.lookupFieldValue(any(FactoryItemsBatch.class), eq(SOURCE_FIELD))).thenReturn(result(factoryItem, date(SOURCE_ID, start)));
            when(lookuper.lookupFieldValue(any(FactoryItemsBatch.class), eq(DURATION_FIELD))).thenReturn(result(factoryItem, duration("5")));
            when(twinService.createFieldValue(TARGET_FIELD)).thenReturn(date(TARGET_ID, null));

            filler.fill(props(false), new FactoryItemsBatch().add(factoryItem), null, false);

            FieldValueDate written = (FieldValueDate) factoryItem.getOutput().getField(TARGET_ID);
            assertEquals(LocalDateTime.of(2024, 1, 5, 10, 0), written.getDate());
        }

        @Test
        void fill_minusInclusiveDays() throws ServiceException {
            var factoryItem = buildFactoryItem();
            LocalDateTime end = LocalDateTime.of(2024, 1, 5, 10, 0);
            when(lookuper.lookupFieldValue(any(FactoryItemsBatch.class), eq(TARGET_FIELD))).thenReturn(result(factoryItem, date(TARGET_ID, null)));
            when(lookuper.lookupFieldValue(any(FactoryItemsBatch.class), eq(SOURCE_FIELD))).thenReturn(result(factoryItem, date(SOURCE_ID, end)));
            when(lookuper.lookupFieldValue(any(FactoryItemsBatch.class), eq(DURATION_FIELD))).thenReturn(result(factoryItem, duration("5")));
            when(twinService.createFieldValue(TARGET_FIELD)).thenReturn(date(TARGET_ID, null));

            filler.fill(props(true), new FactoryItemsBatch().add(factoryItem), null, false);

            FieldValueDate written = (FieldValueDate) factoryItem.getOutput().getField(TARGET_ID);
            assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), written.getDate());
        }

        @Test
        void fill_skipsWhenTargetFilled() throws ServiceException {
            var factoryItem = buildFactoryItem();
            when(lookuper.lookupFieldValue(any(FactoryItemsBatch.class), eq(TARGET_FIELD)))
                    .thenReturn(result(factoryItem, date(TARGET_ID, LocalDateTime.of(2024, 2, 1, 0, 0))));

            filler.fill(props(false), new FactoryItemsBatch().add(factoryItem), null, false);

            assertNull(factoryItem.getOutput().getField(TARGET_ID));
            verify(twinService, never()).createFieldValue(any(TwinClassFieldEntity.class));
        }

        @Test
        void fill_skipsWhenDurationMissing() throws ServiceException {
            var factoryItem = buildFactoryItem();
            when(lookuper.lookupFieldValue(any(FactoryItemsBatch.class), eq(TARGET_FIELD))).thenReturn(result(factoryItem, date(TARGET_ID, null)));
            when(lookuper.lookupFieldValue(any(FactoryItemsBatch.class), eq(SOURCE_FIELD)))
                    .thenReturn(result(factoryItem, date(SOURCE_ID, LocalDateTime.of(2024, 1, 1, 0, 0))));
            when(lookuper.lookupFieldValue(any(FactoryItemsBatch.class), eq(DURATION_FIELD))).thenReturn(result(factoryItem, duration(null)));

            filler.fill(props(false), new FactoryItemsBatch().add(factoryItem), null, false);

            assertNull(factoryItem.getOutput().getField(TARGET_ID));
        }
    }
}
