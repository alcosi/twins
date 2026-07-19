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
import org.twins.core.featurer.factory.filler.FillerFieldFromParams;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FillerFieldFromParamsTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    @Mock
    private TwinClassFieldService twinClassFieldService;

    private FillerFieldFromParams filler;

    private static final UUID FIELD_ID = UUID.randomUUID();
    private static final String VALUE = "literal-from-params";

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerFieldFromParams();
        inject(filler, "twinService", twinService);
        inject(filler, "twinClassFieldService", twinClassFieldService);
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
        p.setProperty("value", VALUE);
        return p;
    }

    private FactoryItem buildFactoryItem() {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        return new FactoryItem().setOutput(output);
    }

    @Nested
    class Fill {

        @Test
        void fill_writesParamValueToOutputField() throws ServiceException {
            // NAME promises: write the literal VALUE FROM FEATURER PARAMS into the output field.
            var factoryItem = buildFactoryItem();
            var fieldEntity = new TwinClassFieldEntity().setId(FIELD_ID);
            var created = new FieldValueText(fieldEntity).setValue(VALUE);
            when(twinClassFieldService.findEntitySafe(FIELD_ID)).thenReturn(fieldEntity);
            when(twinService.createFieldValue(fieldEntity, VALUE)).thenReturn(created);

            filler.fill(props(), List.of(factoryItem), null, false);

            assertSame(created, factoryItem.getOutput().getField(FIELD_ID));
            verify(twinService).createFieldValue(fieldEntity, VALUE);
        }
    }
}
