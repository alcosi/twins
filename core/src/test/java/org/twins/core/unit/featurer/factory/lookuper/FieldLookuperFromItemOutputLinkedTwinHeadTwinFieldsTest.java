package org.twins.core.unit.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromItemOutputLinkedTwinHeadTwinFields;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldLookuperFromItemOutputLinkedTwinHeadTwinFieldsTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    @Mock
    private TwinLinkService twinLinkService;

    private FieldLookuperFromItemOutputLinkedTwinHeadTwinFields lookuper;

    @BeforeEach
    void setUp() throws Exception {
        lookuper = new FieldLookuperFromItemOutputLinkedTwinHeadTwinFields();
        setField(lookuper, "twinService", twinService);
        setField(lookuper, "twinLinkService", twinLinkService);
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
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var dstTwin = new TwinEntity().setId(UUID.randomUUID());
            var dstHeadTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithTwin(twin);
            var linkField = singleLinkField(linkFieldId, dstTwin);

            when(twinService.getTwinFieldValue(twin, linkFieldId)).thenReturn(linkField);
            when(twinService.loadHeadForTwin(dstTwin)).thenReturn(dstHeadTwin);
            var expected = fieldValue(lookupFieldId, "dst-head-val");
            when(twinService.getTwinFieldValue(dstHeadTwin, lookupFieldId)).thenReturn(expected);
            when(twinLinkService.getDstTwinSafe(linkField.getItems().getFirst())).thenReturn(dstTwin);

            var result = lookuper.lookupFieldValue(factoryItem, linkFieldId, lookupFieldId);

            assertSame(expected, result);
            verify(twinLinkService).getDstTwinSafe(linkField.getItems().getFirst());
            verify(twinService).loadHeadForTwin(dstTwin);
            verify(twinService).getTwinFieldValue(dstHeadTwin, lookupFieldId);
            // Must NOT consult the item twin itself for the lookup field.
            verify(twinService, never()).getTwinFieldValue(twin, lookupFieldId);
        }

        @Test
        void lookupFieldValue_linkDstHeadNull_throwsFactoryPipelineError() throws Exception {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var dstTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithTwin(twin);
            var linkField = singleLinkField(linkFieldId, dstTwin);

            when(twinService.getTwinFieldValue(twin, linkFieldId)).thenReturn(linkField);
            when(twinService.loadHeadForTwin(dstTwin)).thenReturn(null);
            when(twinLinkService.getDstTwinSafe(linkField.getItems().getFirst())).thenReturn(dstTwin);

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, linkFieldId, lookupFieldId));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verify(twinService, never()).getTwinFieldValue(any(TwinEntity.class), eq(lookupFieldId));
        }

        @Test
        void lookupFieldValue_linkFieldMultipleItems_throwsFactoryPipelineError() throws ServiceException {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithTwin(twin);
            var multiLink = new FieldValueLink(new TwinClassFieldEntity().setId(linkFieldId));
            multiLink.add(new TwinLinkEntity().setId(UUID.randomUUID()).setDstTwinId(UUID.randomUUID()));
            multiLink.add(new TwinLinkEntity().setId(UUID.randomUUID()).setDstTwinId(UUID.randomUUID()));

            when(twinService.getTwinFieldValue(twin, linkFieldId)).thenReturn(multiLink);

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, linkFieldId, lookupFieldId));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verify(twinLinkService, never()).getDstTwinSafe(any());
        }
    }

    private FieldValueLink singleLinkField(UUID linkFieldId, TwinEntity dstTwin) {
        var link = new TwinLinkEntity()
                .setId(UUID.randomUUID())
                .setDstTwin(dstTwin)
                .setDstTwinId(dstTwin.getId());
        var fv = new FieldValueLink(new TwinClassFieldEntity().setId(linkFieldId));
        fv.add(link);
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
