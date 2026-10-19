package org.twins.core.unit.featurer.factory.lookuper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextTwinLinkedTwinByLinkDbFields;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;
import org.twins.core.service.twinlink.TwinLinkService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FieldLookuperFromContextTwinLinkedTwinByLinkDbFieldsTest extends BaseUnitTest {

    @Mock
    private TwinLinkService twinLinkService;

    private FieldLookuperFromContextTwinLinkedTwinByLinkDbFields lookuper;

    // twinService is required by the abstract FieldLookuper base (injected via reflection).
    @Mock
    private TwinService twinService;

    @Mock
    private TwinClassFieldService twinClassFieldService;

    @BeforeEach
    void setUp() throws Exception {
        lookuper = new FieldLookuperFromContextTwinLinkedTwinByLinkDbFields();
        setField(lookuper, "twinService", twinService);
        setField(lookuper, "twinClassFieldService", twinClassFieldService);
        setField(lookuper, "twinLinkService", twinLinkService);
    }

    // contract (batch entry): from the SINGLE context twin's forward links grouped by
    //           linkedTwinByLinkId, take the FIRST link's dstTwin (links and dst twins are
    //           bulk-preloaded by beforeLookup), then resolve the lookup field from that dst twin's
    //           DB. Missing link -> failure; a null final value becomes an UNDEFINED value.
    //           Source: ONLY the dst twin of the context twin's forward link.

    @Nested
    class LookupFieldValue {

        @Test
        void lookupFieldValue_forwardLinkPresent_resolvesLookupFieldFromDstDb() throws Exception {
            var linkId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var lookupField = new TwinClassFieldEntity().setId(lookupFieldId);
            when(twinClassFieldService.findEntitySafe(lookupFieldId)).thenReturn(lookupField); // the UUID entry resolves the field once per batch
            var dstTwin = new TwinEntity().setId(UUID.randomUUID());
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            plantForwardLink(contextTwin, linkId, dstTwin);
            var factoryItem = itemWithSingleContext(contextTwin);

            var expected = fieldValue(lookupField, "dst-db-val");
            when(twinService.getTwinFieldValue(dstTwin, lookupField)).thenReturn(expected);

            var result = lookuper.lookupFieldValue(new FactoryItemsBatch().add(factoryItem), linkId, lookupFieldId);

            assertSame(expected, result.value(factoryItem));
        }

        @Test
        void lookupFieldValue_noForwardLinkForLinkId_failure() throws Exception {
            var linkId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var lookupField = new TwinClassFieldEntity().setId(lookupFieldId);
            when(twinClassFieldService.findEntitySafe(lookupFieldId)).thenReturn(lookupField); // the UUID entry resolves the field once per batch
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            // empty forward links -> getFirst() throws -> wrapped into the item's failure.
            contextTwin.setTwinLinks(new TwinLinkService.FindTwinLinksResult());
            var factoryItem = itemWithSingleContext(contextTwin);

            var result = lookuper.lookupFieldValue(new FactoryItemsBatch().add(factoryItem), linkId, lookupFieldId);

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), result.failures().get(factoryItem).getErrorCode());
            verify(twinService, never()).getTwinFieldValue(any(TwinEntity.class), any(TwinClassFieldEntity.class));
        }

        @Test
        void lookupFieldValue_lookupFieldAbsentOnDst_returnsUndefinedValue() throws Exception {
            var linkId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var lookupField = new TwinClassFieldEntity().setId(lookupFieldId);
            when(twinClassFieldService.findEntitySafe(lookupFieldId)).thenReturn(lookupField); // the UUID entry resolves the field once per batch
            var dstTwin = new TwinEntity().setId(UUID.randomUUID());
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            plantForwardLink(contextTwin, linkId, dstTwin);
            var factoryItem = itemWithSingleContext(contextTwin);

            when(twinService.getTwinFieldValue(dstTwin, lookupField)).thenReturn(null);
            var undefined = new FieldValueText(lookupField); // no value set -> isUndefined()
            when(twinService.createFieldValue(lookupField)).thenReturn(undefined);

            var result = lookuper.lookupFieldValue(new FactoryItemsBatch().add(factoryItem), linkId, lookupFieldId);
            assertSame(undefined, result.value(factoryItem)); // the batch entry converts a not-found lookup into an undefined value
        }
    }

    private void plantForwardLink(TwinEntity contextTwin, UUID linkId, TwinEntity dstTwin) {
        var links = new TwinLinkService.FindTwinLinksResult();
        var link = new TwinLinkEntity()
                .setId(UUID.randomUUID())
                .setLinkId(linkId)
                .setDstTwin(dstTwin)
                .setDstTwinId(dstTwin.getId());
        links.getForwardLinks().add(link);
        contextTwin.setTwinLinks(links);
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
