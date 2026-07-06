package org.twins.core.unit.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
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
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextTwinLinkedTwinByFieldDbFields;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldLookuperFromContextTwinLinkedTwinByFieldDbFieldsTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    @Mock
    private TwinLinkService twinLinkService;

    private FieldLookuperFromContextTwinLinkedTwinByFieldDbFields lookuper;

    @BeforeEach
    void setUp() throws Exception {
        lookuper = new FieldLookuperFromContextTwinLinkedTwinByFieldDbFields();
        setField(lookuper, "twinService", twinService);
        setField(lookuper, "twinLinkService", twinLinkService);
    }

    // contract: from the SINGLE context twin, read the link FIELD (linkedTwinByTwinClassFieldId)
    //           from its loaded field-values kit; it MUST be a FieldValueLink. Take its first
    //           link's dstTwin, then resolve lookupTwinClassFieldId from that dst twin's DB.
    //           Missing link field / wrong type / null dst value -> ServiceException(FACTORY_PIPELINE_STEP_ERROR).
    //           Source: ONLY the dst twin of the context twin's link field.

    @Nested
    class LookupFieldValue {

        @Test
        void lookupFieldValue_linkFieldPointsAtDst_resolvesLookupFieldFromDstDb() throws Exception {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var dstTwin = new TwinEntity().setId(UUID.randomUUID());
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            plantLinkField(contextTwin, linkFieldId, dstTwin);
            var factoryItem = itemWithSingleContext(contextTwin);

            // loadFieldsValues is a side-effect call on TwinService.
            doNothing().when(twinService).loadFieldsValues(contextTwin);
            when(twinLinkService.getDstTwinSafe(any(TwinLinkEntity.class))).thenReturn(dstTwin);
            var expected = fieldValue(lookupFieldId, "dst-db-val");
            when(twinService.getTwinFieldValue(dstTwin, lookupFieldId)).thenReturn(expected);

            var result = lookuper.lookupFieldValue(factoryItem, linkFieldId, lookupFieldId);

            assertSame(expected, result);
            verify(twinService).loadFieldsValues(contextTwin);
            verify(twinService).getTwinFieldValue(dstTwin, lookupFieldId);
        }

        @Test
        void lookupFieldValue_linkFieldAbsent_throwsFactoryPipelineError() throws Exception {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            // kit with no entry for linkFieldId
            contextTwin.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId));
            var factoryItem = itemWithSingleContext(contextTwin);

            doNothing().when(twinService).loadFieldsValues(contextTwin);

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, linkFieldId, lookupFieldId));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verify(twinService, never()).getTwinFieldValue(any(TwinEntity.class), any(UUID.class));
        }

        @Test
        void lookupFieldValue_linkFieldIsNotALink_throwsFactoryPipelineError() throws Exception {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            // plant a non-link field value
            var kit = new Kit<FieldValue, UUID>(List.of(fieldValue(linkFieldId, "text")), FieldValue::getTwinClassFieldId);
            contextTwin.setFieldValuesKit(kit);
            var factoryItem = itemWithSingleContext(contextTwin);

            doNothing().when(twinService).loadFieldsValues(contextTwin);

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, linkFieldId, lookupFieldId));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verify(twinService, never()).getTwinFieldValue(any(TwinEntity.class), any(UUID.class));
        }

        @Test
        void lookupFieldValue_lookupFieldAbsentOnDst_throwsFactoryPipelineError() throws Exception {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var dstTwin = new TwinEntity().setId(UUID.randomUUID());
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            plantLinkField(contextTwin, linkFieldId, dstTwin);
            var factoryItem = itemWithSingleContext(contextTwin);

            doNothing().when(twinService).loadFieldsValues(contextTwin);
            when(twinLinkService.getDstTwinSafe(any(TwinLinkEntity.class))).thenReturn(dstTwin);
            when(twinService.getTwinFieldValue(dstTwin, lookupFieldId)).thenReturn(null);

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, linkFieldId, lookupFieldId));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void lookupFieldValue_linkFieldEmpty_throwsFactoryPipelineError() throws Exception {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            // link field present but carries no links -> must not NPE on getFirst()
            var linkField = new FieldValueLink(new TwinClassFieldEntity().setId(linkFieldId));
            contextTwin.setFieldValuesKit(new Kit<>(List.of(linkField), FieldValue::getTwinClassFieldId));
            var factoryItem = itemWithSingleContext(contextTwin);

            doNothing().when(twinService).loadFieldsValues(contextTwin);

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, linkFieldId, lookupFieldId));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verify(twinLinkService, never()).getDstTwinSafe(any(TwinLinkEntity.class));
            verify(twinService, never()).getTwinFieldValue(any(TwinEntity.class), any(UUID.class));
        }

        @Test
        void lookupFieldValue_linkFieldHasMultipleLinks_throwsFactoryPipelineError() throws Exception {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            // ambiguous link field with 2 linked twins -> must not silently pick the first
            var dstA = new TwinEntity().setId(UUID.randomUUID());
            var dstB = new TwinEntity().setId(UUID.randomUUID());
            var linkField = new FieldValueLink(new TwinClassFieldEntity().setId(linkFieldId));
            linkField.add(new TwinLinkEntity().setDstTwin(dstA).setDstTwinId(dstA.getId()));
            linkField.add(new TwinLinkEntity().setDstTwin(dstB).setDstTwinId(dstB.getId()));
            contextTwin.setFieldValuesKit(new Kit<>(List.of(linkField), FieldValue::getTwinClassFieldId));
            var factoryItem = itemWithSingleContext(contextTwin);

            doNothing().when(twinService).loadFieldsValues(contextTwin);

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, linkFieldId, lookupFieldId));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verify(twinLinkService, never()).getDstTwinSafe(any(TwinLinkEntity.class));
            verify(twinService, never()).getTwinFieldValue(any(TwinEntity.class), any(UUID.class));
        }
    }

    private void plantLinkField(TwinEntity contextTwin, UUID linkFieldId, TwinEntity dstTwin) {
        var twinClassField = new TwinClassFieldEntity().setId(linkFieldId);
        var linkValue = new FieldValueLink(twinClassField);
        var link = new TwinLinkEntity().setDstTwin(dstTwin).setDstTwinId(dstTwin.getId());
        linkValue.add(link);
        contextTwin.setFieldValuesKit(new Kit<>(List.of(linkValue), FieldValue::getTwinClassFieldId));
    }

    private FactoryItem itemWithSingleContext(TwinEntity contextTwin) {
        var output = new TwinCreate();
        output.setTwinEntity(contextTwin);
        var root = new FactoryItem().setOutput(output);
        return new FactoryItem().setContextFactoryItemList(List.of(root));
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
