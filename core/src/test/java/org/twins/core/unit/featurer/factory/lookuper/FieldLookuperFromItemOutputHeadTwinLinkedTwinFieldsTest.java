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
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromItemOutputHeadTwinLinkedTwinFields;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldLookuperFromItemOutputHeadTwinLinkedTwinFieldsTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    @Mock
    private TwinLinkService twinLinkService;

    private FieldLookuperFromItemOutputHeadTwinLinkedTwinFields lookuper;

    @BeforeEach
    void setUp() throws Exception {
        lookuper = new FieldLookuperFromItemOutputHeadTwinLinkedTwinFields();
        setField(lookuper, "twinService", twinService);
        setField(lookuper, "twinLinkService", twinLinkService);
    }

    // contract: load head twin of factoryItem.getTwin() (null head -> ServiceException), then read
    //           the link FIELD (linkedTwinByTwinClassFieldId) from head's freshest value. It MUST
    //           be a non-empty single-item FieldValueLink. Resolve lookupTwinClassFieldId from the
    //           dst twin of that link (freshest).
    //           Source: ONLY the single dst twin of the HEAD twin's link field.

    @Nested
    class LookupFieldValue {

        @Test
        void lookupFieldValue_headNull_throwsFactoryPipelineError() throws ServiceException {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithTwin(twin);

            when(twinService.loadHead(twin)).thenReturn(null);

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, linkFieldId, lookupFieldId));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verify(twinService, never()).getTwinFieldValue(any(TwinEntity.class), any(UUID.class));
        }

        @Test
        void lookupFieldValue_singleLinkOnHead_resolvesLookupFieldFromLinkDst() throws Exception {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var headTwin = new TwinEntity().setId(UUID.randomUUID());
            var dstTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithTwin(twin);
            var linkField = singleLinkField(linkFieldId, dstTwin);

            when(twinService.loadHead(twin)).thenReturn(headTwin);
            when(twinService.getTwinFieldValue(headTwin, linkFieldId)).thenReturn(linkField);
            var expected = fieldValue(lookupFieldId, "dst-val");
            when(twinService.getTwinFieldValue(dstTwin, lookupFieldId)).thenReturn(expected);
            when(twinLinkService.getDstTwinSafe(linkField.getItems().getFirst())).thenReturn(dstTwin);

            var result = lookuper.lookupFieldValue(factoryItem, linkFieldId, lookupFieldId);

            assertSame(expected, result);
            verify(twinService).getTwinFieldValue(headTwin, linkFieldId);
            verify(twinService).getTwinFieldValue(dstTwin, lookupFieldId);
            verify(twinLinkService).getDstTwinSafe(linkField.getItems().getFirst());
        }

        @Test
        void lookupFieldValue_linkFieldOnHeadNotALink_throwsFactoryPipelineError() throws ServiceException {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var headTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithTwin(twin);

            when(twinService.loadHead(twin)).thenReturn(headTwin);
            when(twinService.getTwinFieldValue(headTwin, linkFieldId)).thenReturn(fieldValue(linkFieldId, "text"));

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, linkFieldId, lookupFieldId));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verify(twinLinkService, never()).getDstTwinSafe(any());
        }

        @Test
        void lookupFieldValue_linkFieldOnHeadHasMultipleItems_throwsFactoryPipelineError() throws ServiceException {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var headTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithTwin(twin);
            var multiLink = new FieldValueLink(new TwinClassFieldEntity().setId(linkFieldId));
            multiLink.add(new TwinLinkEntity().setId(UUID.randomUUID()).setDstTwinId(UUID.randomUUID()));
            multiLink.add(new TwinLinkEntity().setId(UUID.randomUUID()).setDstTwinId(UUID.randomUUID()));

            when(twinService.loadHead(twin)).thenReturn(headTwin);
            when(twinService.getTwinFieldValue(headTwin, linkFieldId)).thenReturn(multiLink);

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
