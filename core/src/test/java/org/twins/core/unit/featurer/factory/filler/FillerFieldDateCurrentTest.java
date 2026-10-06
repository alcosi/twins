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
import org.twins.core.featurer.factory.filler.FillerFieldDateCurrent;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromItemOutputFields;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FillerFieldDateCurrentTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;
    @Mock
    private TwinClassFieldService twinClassFieldService;
    @Mock
    private FieldLookupers fieldLookupers;
    @Mock
    private FieldLookuperFromItemOutputFields lookuper;

    private FillerFieldDateCurrent filler;

    private static final UUID FIELD_ID = UUID.randomUUID();
    private static final TwinClassFieldEntity FIELD = new TwinClassFieldEntity().setId(FIELD_ID);

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerFieldDateCurrent(twinService, twinClassFieldService);
        inject(filler, "fieldLookupers", fieldLookupers);
        when(fieldLookupers.getFromItemOutputFields()).thenReturn(lookuper);
        lenient().when(twinClassFieldService.findEntitySafe(FIELD_ID)).thenReturn(FIELD);
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
        p.setProperty("twinClassFieldId", FIELD_ID.toString());
        return p;
    }

    private FactoryItem buildFactoryItem() {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity().setId(UUID.randomUUID()));
        return new FactoryItem().setOutput(output);
    }

    private FieldValueDate emptyDate() {
        return new FieldValueDate(FIELD, "yyyy-MM-dd");
    }

    private FieldValueDate filledDate(LocalDateTime value) {
        return emptyDate().setDate(value);
    }

    @Nested
    class Fill {
        @Test
        void fill_setsNowWhenEmpty() throws ServiceException {
            var factoryItem = buildFactoryItem();
            when(lookuper.lookupFieldValue(factoryItem, FIELD_ID)).thenReturn(emptyDate());
            when(twinService.createFieldValue(FIELD)).thenReturn(emptyDate());

            filler.fill(props(), factoryItem, null);

            FieldValueDate written = (FieldValueDate) factoryItem.getOutput().getField(FIELD_ID);
            assertNotNull(written);
            assertEquals(LocalDate.now(), written.getDate().toLocalDate());
        }

        @Test
        void fill_skipsWhenAlreadyFilled() throws ServiceException {
            var factoryItem = buildFactoryItem();
            LocalDateTime existing = LocalDateTime.of(2024, 1, 15, 0, 0);
            when(lookuper.lookupFieldValue(factoryItem, FIELD_ID)).thenReturn(filledDate(existing));

            filler.fill(props(), factoryItem, null);

            assertNull(factoryItem.getOutput().getField(FIELD_ID));
            verify(twinService, never()).createFieldValue(any(TwinClassFieldEntity.class));
        }
    }
}
