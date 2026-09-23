package org.twins.core.unit.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromItemOutputLinkedTwinHeadTwinFields;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldLookuperFromItemOutputLinkedTwinHeadTwinFieldsTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    @Mock
    private TwinClassFieldService twinClassFieldService;

    private FieldLookuperFromItemOutputLinkedTwinHeadTwinFields lookuper;

    @BeforeEach
    void setUp() throws Exception {
        lookuper = new FieldLookuperFromItemOutputLinkedTwinHeadTwinFields();
        setField(lookuper, "twinService", twinService);
        setField(lookuper, "twinClassFieldService", twinClassFieldService);
    }

    // contract: read the link FIELD (linkedTwinByTwinClassFieldId) from factoryItem.getTwin() freshest value;
    //           it MUST be a non-empty single-item FieldValueLink. Take its dst twin, load the HEAD of
    //           that dst twin (null -> ServiceException), and resolve lookupTwinClassFieldId from that head.
    //           Source: ONLY the head twin of the item twin's link-field dst twin.

    @Nested
    class LookupFieldValue {

        @Test
        void lookupFieldValue_linkDstHead_resolvesLookupFieldFromLinkDstHead() throws Exception {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var linkFieldEntity = new TwinClassFieldEntity().setId(linkFieldId);
            var lookupFieldEntity = new TwinClassFieldEntity().setId(lookupFieldId);
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var dstTwin = new TwinEntity().setId(UUID.randomUUID());
            var dstHeadTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithTwin(twin);
            var linkField = singleLinkField(linkFieldId, dstTwin);

            when(twinClassFieldService.findEntitySafe(linkFieldId)).thenReturn(linkFieldEntity);
            when(twinClassFieldService.findEntitySafe(lookupFieldId)).thenReturn(lookupFieldEntity);
            when(twinService.getTwinFieldValue(twin, linkFieldEntity)).thenReturn(linkField);
            when(twinService.loadHead(dstTwin)).thenReturn(dstHeadTwin);
            var expected = fieldValue(lookupFieldId, "dst-head-val");
            when(twinService.getTwinFieldValue(dstHeadTwin, lookupFieldEntity)).thenReturn(expected);

            var result = lookuper.lookupFieldValue(factoryItem, linkFieldId, lookupFieldId);

            assertSame(expected, result);
            verify(twinService).loadHead(dstTwin);
            verify(twinService).getTwinFieldValue(dstHeadTwin, lookupFieldEntity);
            // Must NOT consult the item twin itself for the lookup field.
            verify(twinService, never()).getTwinFieldValue(twin, lookupFieldEntity);
        }

        @Test
        void lookupFieldValue_linkDstHeadNull_throwsFactoryPipelineError() throws Exception {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var linkFieldEntity = new TwinClassFieldEntity().setId(linkFieldId);
            var lookupFieldEntity = new TwinClassFieldEntity().setId(lookupFieldId);
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var dstTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithTwin(twin);
            var linkField = singleLinkField(linkFieldId, dstTwin);

            when(twinClassFieldService.findEntitySafe(linkFieldId)).thenReturn(linkFieldEntity);
            when(twinService.getTwinFieldValue(twin, linkFieldEntity)).thenReturn(linkField);
            when(twinService.loadHead(dstTwin)).thenReturn(null);

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, linkFieldId, lookupFieldId));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verify(twinService, never()).getTwinFieldValue(any(TwinEntity.class), eq(lookupFieldEntity));
        }

        @Test
        void lookupFieldValue_linkFieldMultipleItems_throwsFactoryPipelineError() throws ServiceException {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var linkFieldEntity = new TwinClassFieldEntity().setId(linkFieldId);
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithTwin(twin);
            var multiLink = new FieldValueLink(new TwinClassFieldEntity().setId(linkFieldId));
            multiLink.add(new TwinEntity().setId(UUID.randomUUID()));
            multiLink.add(new TwinEntity().setId(UUID.randomUUID()));

            when(twinClassFieldService.findEntitySafe(linkFieldId)).thenReturn(linkFieldEntity);
            when(twinService.getTwinFieldValue(twin, linkFieldEntity)).thenReturn(multiLink);

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, linkFieldId, lookupFieldId));

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT.getCode(), ex.getErrorCode());
        }
    }

    private FieldValueLink singleLinkField(UUID linkFieldId, TwinEntity dstTwin) {
        var fv = new FieldValueLink(new TwinClassFieldEntity().setId(linkFieldId));
        fv.add(dstTwin); // items carry the far twins
        return fv;
    }

    private FactoryItem itemWithTwin(TwinEntity twin) {
        var output = new TwinCreate();
        output.setTwinEntity(twin);
        return new FactoryItem().setOutput(output).setFactoryContext(new FactoryContext(null, null));
    }

    private FieldValue fieldValue(UUID fieldId, String value) {
        var twinClassField = new TwinClassFieldEntity();
        twinClassField.setId(fieldId);
        var fv = new FieldValueText(twinClassField);
        fv.setValue(value);
        return fv;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }
}
