package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerFieldAsContextFieldHead;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextFields;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FillerFieldAsContextFieldHeadTest extends BaseUnitTest {

    @Mock
    private FieldLookupers fieldLookupers;

    @Mock
    private FieldLookuperFromContextFields lookuper;

    @Mock
    private TwinService twinService;

    @Mock
    private TwinLinkService twinLinkService;

    @Mock
    private TwinClassFieldService twinClassFieldService;

    private FillerFieldAsContextFieldHead filler;

    private static final UUID SRC_FIELD_ID = UUID.randomUUID();
    private static final UUID DST_FIELD_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerFieldAsContextFieldHead();
        inject(filler, "fieldLookupers", fieldLookupers);
        inject(filler, "twinService", twinService);
        inject(filler, "twinLinkService", twinLinkService);
        inject(filler, "twinClassFieldService", twinClassFieldService);
        when(fieldLookupers.getFromContextFields()).thenReturn(lookuper);
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
        p.setProperty("dstTwinClassFieldId", DST_FIELD_ID.toString());
        return p;
    }

    private FactoryItem buildFactoryItem() {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        return new FactoryItem().setOutput(output);
    }

    private TwinClassFieldEntity field(UUID id) {
        return new TwinClassFieldEntity().setId(id);
    }

    @Nested
    class Fill {

        @Test
        void fill_linkFieldWithDstTwinHead_writesHeadIdToDst() throws ServiceException {
            // NAME promises: take head of the dst twin of the CONTEXT LINK FIELD's value, write head id to dst field.
            var headId = UUID.randomUUID();
            var dstTwin = new TwinEntity().setHeadTwinId(headId);
            var link = new TwinLinkEntity().setDstTwin(dstTwin);
            var srcValue = new FieldValueLink(field(SRC_FIELD_ID)).add(link);
            var factoryItem = buildFactoryItem();
            when(lookuper.lookupFieldValue(factoryItem, SRC_FIELD_ID)).thenReturn(srcValue);
            var dstFieldEntity = field(DST_FIELD_ID);
            var createdHeadLink = new FieldValueLink(dstFieldEntity);
            when(twinClassFieldService.findEntitySafe(DST_FIELD_ID)).thenReturn(dstFieldEntity);
            when(twinService.createFieldValue(dstFieldEntity, headId.toString())).thenReturn(createdHeadLink);

            filler.fill(props(), List.of(factoryItem), null, false);

            assertSame(createdHeadLink, factoryItem.getOutput().getField(DST_FIELD_ID));
        }

        @Test
        void fill_nonLinkField_throwsStepError() throws ServiceException {
            var factoryItem = buildFactoryItem();
            when(lookuper.lookupFieldValue(factoryItem, SRC_FIELD_ID))
                    .thenReturn(new FieldValueText(field(SRC_FIELD_ID)).setValue("v"));

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verifyNoInteractions(twinService);
        }

        @Test
        void fill_emptyLink_throwsStepError() throws ServiceException {
            var factoryItem = buildFactoryItem();
            // FieldValueLink with no items -> isEmpty()==true (isUndefined since collection==null).
            when(lookuper.lookupFieldValue(factoryItem, SRC_FIELD_ID)).thenReturn(new FieldValueLink(field(SRC_FIELD_ID)));

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verifyNoInteractions(twinService);
        }

        @Test
        void fill_dstTwinHasNoHead_throwsStepError() throws ServiceException {
            var dstTwin = new TwinEntity(); // headTwinId == null
            var link = new TwinLinkEntity().setDstTwin(dstTwin);
            var srcValue = new FieldValueLink(field(SRC_FIELD_ID)).add(link);
            var factoryItem = buildFactoryItem();
            when(lookuper.lookupFieldValue(factoryItem, SRC_FIELD_ID)).thenReturn(srcValue);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verifyNoInteractions(twinService);
        }
    }
}
