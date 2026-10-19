package org.twins.core.unit.featurer.factory.lookuper;

import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextTwinLinkedTwinByFieldDbFields;
import org.twins.core.featurer.factory.lookuper.LookupResult;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

class FieldLookuperFromContextTwinLinkedTwinByFieldDbFieldsTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    @Mock
    private TwinClassFieldService twinClassFieldService;

    private FieldLookuperFromContextTwinLinkedTwinByFieldDbFields lookuper;

    @BeforeEach
    void setUp() throws Exception {
        lookuper = new FieldLookuperFromContextTwinLinkedTwinByFieldDbFields();
        setField(lookuper, "twinService", twinService);
        setField(lookuper, "twinClassFieldService", twinClassFieldService);
    }

    // contract (batch entry): pass 1 navigates per item from the SINGLE context twin's loaded
    //           field-values kit via the link FIELD (must be a single-item FieldValueLink); pass 2
    //           resolves the lookup field from the linked twin's DB (bulk-preloaded together with
    //           the other items' linked twins). Navigation failures (missing / wrong type / empty /
    //           ambiguous link field) land in failures(); a null final value becomes an UNDEFINED
    //           value. Source: ONLY the far twin linked via the context twin's link field.

    @Nested
    class LookupFieldValue {

        @Test
        void lookupFieldValue_linkFieldPointsAtDst_resolvesLookupFieldFromDstDb() throws Exception {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var lookupField = new TwinClassFieldEntity().setId(lookupFieldId);
            when(twinClassFieldService.findEntitySafe(lookupFieldId)).thenReturn(lookupField); // the UUID entry resolves the field once per batch
            var dstTwin = new TwinEntity().setId(UUID.randomUUID());
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            plantLinkField(contextTwin, linkFieldId, dstTwin);
            var factoryItem = itemWithSingleContext(contextTwin);

            var expected = fieldValue(lookupField, "dst-db-val");
            when(twinService.getTwinFieldValue(dstTwin, lookupField)).thenReturn(expected);

            var result = lookuper.lookupFieldValue(new FactoryItemsBatch().add(factoryItem), linkFieldId, lookupFieldId);

            assertSame(expected, result.value(factoryItem));
            verify(twinService).loadFieldsValues(anyCollection()); // the bulk context-twin preload
            verify(twinService).loadTwinFields(anyCollection(), any(TwinClassFieldEntity.class)); // the bulk linked-twin preload
        }

        @Test
        void lookupFieldValue_linkFieldAbsent_navigationFailure() throws Exception {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var lookupField = new TwinClassFieldEntity().setId(lookupFieldId);
            when(twinClassFieldService.findEntitySafe(lookupFieldId)).thenReturn(lookupField); // the UUID entry resolves the field once per batch
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            // kit with no entry for linkFieldId
            contextTwin.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId));
            var factoryItem = itemWithSingleContext(contextTwin);

            var result = lookuper.lookupFieldValue(new FactoryItemsBatch().add(factoryItem), linkFieldId, lookupFieldId);

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT.getCode(), result.failures().get(factoryItem).getErrorCode());
            verify(twinService, never()).getTwinFieldValue(any(TwinEntity.class), any(TwinClassFieldEntity.class));
        }

        @Test
        void lookupFieldValue_linkFieldIsNotALink_navigationFailure() throws Exception {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var lookupField = new TwinClassFieldEntity().setId(lookupFieldId);
            when(twinClassFieldService.findEntitySafe(lookupFieldId)).thenReturn(lookupField); // the UUID entry resolves the field once per batch
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            // plant a non-link field value
            var kit = new Kit<FieldValue, UUID>(List.of(fieldValue(new TwinClassFieldEntity().setId(linkFieldId), "text")), FieldValue::getTwinClassFieldId);
            contextTwin.setFieldValuesKit(kit);
            var factoryItem = itemWithSingleContext(contextTwin);

            var result = lookuper.lookupFieldValue(new FactoryItemsBatch().add(factoryItem), linkFieldId, lookupFieldId);

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT.getCode(), result.failures().get(factoryItem).getErrorCode());
            verify(twinService, never()).getTwinFieldValue(any(TwinEntity.class), any(TwinClassFieldEntity.class));
        }

        @Test
        void lookupFieldValue_lookupFieldAbsentOnDst_returnsUndefinedValue() throws Exception {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var lookupField = new TwinClassFieldEntity().setId(lookupFieldId);
            when(twinClassFieldService.findEntitySafe(lookupFieldId)).thenReturn(lookupField); // the UUID entry resolves the field once per batch
            var dstTwin = new TwinEntity().setId(UUID.randomUUID());
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            plantLinkField(contextTwin, linkFieldId, dstTwin);
            var factoryItem = itemWithSingleContext(contextTwin);

            when(twinService.getTwinFieldValue(dstTwin, lookupField)).thenReturn(null);
            var undefined = new FieldValueText(lookupField); // no value set -> isUndefined()
            when(twinService.createFieldValue(lookupField)).thenReturn(undefined);

            LookupResult result = lookuper.lookupFieldValue(new FactoryItemsBatch().add(factoryItem), linkFieldId, lookupFieldId);
            assertSame(undefined, result.value(factoryItem)); // the batch entry converts a not-found lookup into an undefined value
        }

        @Test
        void lookupFieldValue_linkFieldEmpty_navigationFailure() throws Exception {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var lookupField = new TwinClassFieldEntity().setId(lookupFieldId);
            when(twinClassFieldService.findEntitySafe(lookupFieldId)).thenReturn(lookupField); // the UUID entry resolves the field once per batch
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            // link field present but carries no links -> must not NPE on getFirst()
            var linkField = new FieldValueLink(new TwinClassFieldEntity().setId(linkFieldId));
            contextTwin.setFieldValuesKit(new Kit<>(List.of(linkField), FieldValue::getTwinClassFieldId));
            var factoryItem = itemWithSingleContext(contextTwin);

            var result = lookuper.lookupFieldValue(new FactoryItemsBatch().add(factoryItem), linkFieldId, lookupFieldId);

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT.getCode(), result.failures().get(factoryItem).getErrorCode());
            verify(twinService, never()).getTwinFieldValue(any(TwinEntity.class), any(TwinClassFieldEntity.class));
        }

        @Test
        void lookupFieldValue_linkFieldHasMultipleLinks_navigationFailure() throws Exception {
            var linkFieldId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var lookupField = new TwinClassFieldEntity().setId(lookupFieldId);
            when(twinClassFieldService.findEntitySafe(lookupFieldId)).thenReturn(lookupField); // the UUID entry resolves the field once per batch
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            // ambiguous link field with 2 linked twins -> must not silently pick the first
            var dstA = new TwinEntity().setId(UUID.randomUUID());
            var dstB = new TwinEntity().setId(UUID.randomUUID());
            var linkField = new FieldValueLink(new TwinClassFieldEntity().setId(linkFieldId));
            linkField.add(dstA); // items carry the far twins
            linkField.add(dstB);
            contextTwin.setFieldValuesKit(new Kit<>(List.of(linkField), FieldValue::getTwinClassFieldId));
            var factoryItem = itemWithSingleContext(contextTwin);

            var result = lookuper.lookupFieldValue(new FactoryItemsBatch().add(factoryItem), linkFieldId, lookupFieldId);

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT.getCode(), result.failures().get(factoryItem).getErrorCode());
            verify(twinService, never()).getTwinFieldValue(any(TwinEntity.class), any(TwinClassFieldEntity.class));
        }
    }

    private void plantLinkField(TwinEntity contextTwin, UUID linkFieldId, TwinEntity dstTwin) {
        var twinClassField = new TwinClassFieldEntity().setId(linkFieldId);
        var linkValue = new FieldValueLink(twinClassField);
        linkValue.add(dstTwin); // items carry the far twins
        contextTwin.setFieldValuesKit(new Kit<>(List.of(linkValue), FieldValue::getTwinClassFieldId));
    }

    private FactoryItem itemWithSingleContext(TwinEntity contextTwin) {
        var output = new TwinCreate();
        output.setTwinEntity(contextTwin);
        var root = new FactoryItem().setOutput(output);
        return new FactoryItem().setContextFactoryItemList(List.of(root));
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
