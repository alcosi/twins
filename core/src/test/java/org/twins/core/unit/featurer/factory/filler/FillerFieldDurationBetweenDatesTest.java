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
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.filler.FillerFieldDurationBetweenDates;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromItemOutputFields;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FillerFieldDurationBetweenDatesTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;
    @Mock
    private TwinClassFieldService twinClassFieldService;
    @Mock
    private FieldLookupers fieldLookupers;
    @Mock
    private FieldLookuperFromItemOutputFields lookuper;

    private FillerFieldDurationBetweenDates filler;

    private static final UUID DURATION_ID = UUID.randomUUID();
    private static final UUID START_ID = UUID.randomUUID();
    private static final UUID END_ID = UUID.randomUUID();
    private static final TwinClassFieldEntity DURATION_FIELD = new TwinClassFieldEntity().setId(DURATION_ID);

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerFieldDurationBetweenDates(twinService, twinClassFieldService);
        inject(filler, "fieldLookupers", fieldLookupers);
        when(fieldLookupers.getFromItemOutputFields()).thenReturn(lookuper);
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

    private Properties props() {
        var p = new Properties();
        p.setProperty("durationTwinClassFieldId", DURATION_ID.toString());
        p.setProperty("startDateTwinClassFieldId", START_ID.toString());
        p.setProperty("endDateTwinClassFieldId", END_ID.toString());
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

    @Nested
    class Fill {
        @Test
        void fill_setsInclusiveDays() throws ServiceException {
            var factoryItem = buildFactoryItem();
            when(lookuper.lookupFieldValue(factoryItem, DURATION_ID))
                    .thenReturn(new FieldValueText(DURATION_FIELD));
            when(lookuper.lookupFieldValue(factoryItem, START_ID))
                    .thenReturn(date(START_ID, LocalDateTime.of(2024, 1, 1, 0, 0)));
            when(lookuper.lookupFieldValue(factoryItem, END_ID))
                    .thenReturn(date(END_ID, LocalDateTime.of(2024, 1, 5, 0, 0)));
            when(twinService.createFieldValue(DURATION_FIELD))
                    .thenReturn(new FieldValueText(DURATION_FIELD));

            filler.fill(props(), factoryItem, null);

            FieldValueText written = (FieldValueText) factoryItem.getOutput().getField(DURATION_ID);
            assertEquals("5", written.getValue());
        }

        @Test
        void fill_skipsWhenDurationFilled() throws ServiceException {
            var factoryItem = buildFactoryItem();
            when(lookuper.lookupFieldValue(factoryItem, DURATION_ID))
                    .thenReturn(new FieldValueText(DURATION_FIELD).setValue("3"));

            filler.fill(props(), factoryItem, null);

            assertNull(factoryItem.getOutput().getField(DURATION_ID));
            verify(twinService, never()).createFieldValue(any(TwinClassFieldEntity.class));
        }

        @Test
        void fill_skipsWhenEndMissing() throws ServiceException {
            var factoryItem = buildFactoryItem();
            when(lookuper.lookupFieldValue(factoryItem, DURATION_ID))
                    .thenReturn(new FieldValueText(DURATION_FIELD));
            when(lookuper.lookupFieldValue(factoryItem, START_ID))
                    .thenReturn(date(START_ID, LocalDateTime.of(2024, 1, 1, 0, 0)));
            when(lookuper.lookupFieldValue(factoryItem, END_ID)).thenReturn(date(END_ID, null));

            filler.fill(props(), factoryItem, null);

            assertNull(factoryItem.getOutput().getField(DURATION_ID));
        }
    }
}
