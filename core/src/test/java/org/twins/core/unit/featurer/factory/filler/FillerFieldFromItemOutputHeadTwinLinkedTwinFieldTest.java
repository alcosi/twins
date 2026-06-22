package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerFieldFromItemOutputHeadTwinLinkedTwinField;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromItemOutputHeadTwinLinkedTwinFields;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FillerFieldFromItemOutputHeadTwinLinkedTwinFieldTest extends BaseUnitTest {

    @Mock
    private FieldLookupers fieldLookupers;

    @Mock
    private FieldLookuperFromItemOutputHeadTwinLinkedTwinFields lookuper;

    @Mock
    private TwinService twinService;

    @Mock
    private TwinClassService twinClassService;

    private FillerFieldFromItemOutputHeadTwinLinkedTwinField filler;

    private static final UUID SRC_FIELD_ID = UUID.randomUUID();
    private static final UUID LOOKUP_FIELD_ID = UUID.randomUUID();
    private static final UUID DST_FIELD_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerFieldFromItemOutputHeadTwinLinkedTwinField();
        inject(filler, "fieldLookupers", fieldLookupers);
        inject(filler, "twinService", twinService);
        inject(filler, "twinClassService", twinClassService);
        when(fieldLookupers.getFromItemOutputHeadTwinLinkedTwinFields()).thenReturn(lookuper);
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
        p.setProperty("srcTwinClassFieldId", SRC_FIELD_ID.toString());
        p.setProperty("lookupTwinClassFieldId", LOOKUP_FIELD_ID.toString());
        p.setProperty("dstTwinClassFieldId", DST_FIELD_ID.toString());
        return p;
    }

    private FactoryItem buildFactoryItem() {
        var twinClass = new TwinClassEntity().setId(UUID.randomUUID());
        var twin = new TwinEntity().setTwinClass(twinClass);
        var output = new TwinCreate();
        output.setTwinEntity(twin);
        return new FactoryItem().setOutput(output);
    }

    private TwinClassFieldEntity field(UUID id) {
        return new TwinClassFieldEntity().setId(id).setTwinClassId(UUID.randomUUID());
    }

    @Nested
    class Fill {

        @Test
        void fill_resolvesOutputHeadTwinLinkedTwinThenLookupField() throws ServiceException {
            // NAME promises: from OUTPUT HEAD twin's linked twin (by src link field), read lookup field, write to dst.
            var factoryItem = buildFactoryItem();
            var srcValue = new FieldValueText(field(LOOKUP_FIELD_ID)).setValue("v");
            var dstClone = new FieldValueText(field(DST_FIELD_ID)).setValue("v");
            when(lookuper.lookupFieldValue(factoryItem, SRC_FIELD_ID, LOOKUP_FIELD_ID)).thenReturn(srcValue);
            when(twinService.copyToField(srcValue, DST_FIELD_ID)).thenReturn(dstClone);
            when(twinClassService.isInstanceOf(any(TwinClassEntity.class), eq(dstClone.getTwinClassField().getTwinClassId())))
                    .thenReturn(true);

            filler.fill(props(), factoryItem, null);

            verify(fieldLookupers).getFromItemOutputHeadTwinLinkedTwinFields();
            assertSame(dstClone, factoryItem.getOutput().getField(DST_FIELD_ID));
        }
    }
}
