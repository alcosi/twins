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

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FillerFieldFromParamsTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private FillerFieldFromParams filler;

    private static final UUID FIELD_ID = UUID.randomUUID();
    private static final String VALUE = "literal-from-params";

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerFieldFromParams();
        inject(filler, "twinService", twinService);
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
            var created = new FieldValueText(new TwinClassFieldEntity().setId(FIELD_ID)).setValue(VALUE);
            when(twinService.createFieldValue(FIELD_ID, VALUE)).thenReturn(created);

            filler.fill(props(), factoryItem, null);

            assertSame(created, factoryItem.getOutput().getField(FIELD_ID));
            verify(twinService).createFieldValue(FIELD_ID, VALUE);
        }
    }
}
