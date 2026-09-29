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
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromItemOutputHeadTwinLinkedTwinFields;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class FieldLookuperFromItemOutputHeadTwinLinkedTwinFieldsTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    @Mock
    private TwinClassFieldService twinClassFieldService;

    private FieldLookuperFromItemOutputHeadTwinLinkedTwinFields lookuper;

    @BeforeEach
    void setUp() throws Exception {
        lookuper = new FieldLookuperFromItemOutputHeadTwinLinkedTwinFields();
        setField(lookuper, "twinService", twinService);
        setField(lookuper, "twinClassFieldService", twinClassFieldService);
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
            // twin carries no head -> pass 1 fails the item

            var result = lookuper.lookupFieldValue(new FactoryItemsBatch().add(factoryItem), linkFieldId, lookupFieldId);

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), result.failures().get(factoryItem).getErrorCode());
            verify(twinService, never()).getTwinFieldValue(any(TwinEntity.class), any(TwinClassFieldEntity.class));
        }

        @Test
        void lookupFieldValue_singleLinkOnHead_resolvesLookupFieldFromLinkDst() throws Exception {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var linkFieldEntity = new TwinClassFieldEntity().setId(linkFieldId);
            var lookupFieldEntity = new TwinClassFieldEntity().setId(lookupFieldId);
            when(twinClassFieldService.findEntitySafe(lookupFieldId)).thenReturn(lookupFieldEntity); // the UUID entry resolves the field once per batch
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var headTwin = new TwinEntity().setId(UUID.randomUUID());
            var dstTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithTwin(twin);
            var linkField = singleLinkField(linkFieldId, dstTwin);

            twin.setHeadTwin(headTwin); // preloaded by beforeLookup's bulk load — a plain field read in pass 1
            when(twinClassFieldService.findEntitySafe(linkFieldId)).thenReturn(linkFieldEntity); // navigation resolves the linkedBy UUID -> entity
            when(twinService.getTwinFieldValue(headTwin, linkFieldEntity)).thenReturn(linkField);
            var expected = fieldValue(lookupFieldEntity, "dst-val");
            when(twinService.getTwinFieldValue(dstTwin, lookupFieldEntity)).thenReturn(expected);

            var result = lookuper.lookupFieldValue(new FactoryItemsBatch().add(factoryItem), linkFieldId, lookupFieldId);

            assertSame(expected, result.value(factoryItem));
            verify(twinService).getTwinFieldValue(headTwin, linkFieldEntity);
            verify(twinService).getTwinFieldValue(dstTwin, lookupFieldEntity);
        }

        @Test
        void lookupFieldValue_linkFieldOnHeadNotALink_throwsFactoryPipelineError() throws ServiceException {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var linkFieldEntity = new TwinClassFieldEntity().setId(linkFieldId);
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var headTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithTwin(twin);

            twin.setHeadTwin(headTwin); // preloaded by beforeLookup's bulk load — a plain field read in pass 1
            when(twinClassFieldService.findEntitySafe(linkFieldId)).thenReturn(linkFieldEntity); // navigation resolves the linkedBy UUID -> entity
            when(twinService.getTwinFieldValue(headTwin, linkFieldEntity)).thenReturn(fieldValue(linkFieldEntity, "text"));

            var lookupFieldEntity = new TwinClassFieldEntity().setId(lookupFieldId);
            when(twinClassFieldService.findEntitySafe(lookupFieldId)).thenReturn(lookupFieldEntity); // the UUID entry resolves the field once per batch
            var result = lookuper.lookupFieldValue(new FactoryItemsBatch().add(factoryItem), linkFieldId, lookupFieldId);

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT.getCode(), result.failures().get(factoryItem).getErrorCode());
        }

        @Test
        void lookupFieldValue_linkFieldOnHeadHasMultipleItems_throwsFactoryPipelineError() throws ServiceException {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var linkFieldEntity = new TwinClassFieldEntity().setId(linkFieldId);
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var headTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithTwin(twin);
            var multiLink = new FieldValueLink(new TwinClassFieldEntity().setId(linkFieldId));
            multiLink.add(new TwinEntity().setId(UUID.randomUUID()));
            multiLink.add(new TwinEntity().setId(UUID.randomUUID()));

            twin.setHeadTwin(headTwin); // preloaded by beforeLookup's bulk load — a plain field read in pass 1
            when(twinClassFieldService.findEntitySafe(linkFieldId)).thenReturn(linkFieldEntity); // navigation resolves the linkedBy UUID -> entity
            when(twinService.getTwinFieldValue(headTwin, linkFieldEntity)).thenReturn(multiLink);

            var lookupFieldEntity = new TwinClassFieldEntity().setId(lookupFieldId);
            when(twinClassFieldService.findEntitySafe(lookupFieldId)).thenReturn(lookupFieldEntity); // the UUID entry resolves the field once per batch
            var result = lookuper.lookupFieldValue(new FactoryItemsBatch().add(factoryItem), linkFieldId, lookupFieldId);

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT.getCode(), result.failures().get(factoryItem).getErrorCode());
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

    private FieldValue fieldValue(TwinClassFieldEntity field, String value) {
        var fv = new FieldValueText(field);
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
