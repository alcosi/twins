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
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextTwinLinkedTwinByLinkDbFields;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.link.TwinLinkService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldLookuperFromContextTwinLinkedTwinByLinkDbFieldsTest extends BaseUnitTest {

    @Mock
    private TwinLinkService twinLinkService;

    private FieldLookuperFromContextTwinLinkedTwinByLinkDbFields lookuper;

    // twinService is required by the abstract FieldLookuper base (injected via reflection).
    @Mock
    private org.twins.core.service.twin.TwinService twinService;

    @BeforeEach
    void setUp() throws Exception {
        lookuper = new FieldLookuperFromContextTwinLinkedTwinByLinkDbFields();
        setField(lookuper, "twinService", twinService);
        setField(lookuper, "twinLinkService", twinLinkService);
    }

    // contract: from the SINGLE context twin's forward links grouped by linkedTwinByLinkId,
    //           take the FIRST link's dstTwin, then resolve lookupTwinClassFieldId from that
    //           dst twin's DB. Missing link / null dst value -> ServiceException(FACTORY_PIPELINE_STEP_ERROR).
    //           Source: ONLY the dst twin of the context twin's forward link.

    @Nested
    class LookupFieldValue {

        @Test
        void lookupFieldValue_forwardLinkPresent_resolvesLookupFieldFromDstDb() throws Exception {
            var linkId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var dstTwin = new TwinEntity().setId(UUID.randomUUID());
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            plantForwardLink(contextTwin, linkId, dstTwin);
            var factoryItem = itemWithSingleContext(contextTwin);

            doNothing().when(twinLinkService).loadTwinLinks(contextTwin);
            var expected = fieldValue(lookupFieldId, "dst-db-val");
            when(twinService.getTwinFieldValue(dstTwin, lookupFieldId)).thenReturn(expected);

            var result = lookuper.lookupFieldValue(factoryItem, linkId, lookupFieldId);

            assertSame(expected, result);
            verify(twinLinkService).loadTwinLinks(contextTwin);
            verify(twinService).getTwinFieldValue(dstTwin, lookupFieldId);
        }

        @Test
        void lookupFieldValue_noForwardLinkForLinkId_throwsFactoryPipelineError() throws Exception {
            var linkId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            // empty forward links -> getFirst() throws NPE -> wrapped.
            contextTwin.setTwinLinks(new TwinLinkService.FindTwinLinksResult());
            var factoryItem = itemWithSingleContext(contextTwin);

            doNothing().when(twinLinkService).loadTwinLinks(contextTwin);

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, linkId, lookupFieldId));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verify(twinService, never()).getTwinFieldValue(any(TwinEntity.class), any(UUID.class));
        }

        @Test
        void lookupFieldValue_lookupFieldAbsentOnDst_throwsFactoryPipelineError() throws Exception {
            var linkId = UUID.randomUUID();
            var lookupFieldId = UUID.randomUUID();
            var dstTwin = new TwinEntity().setId(UUID.randomUUID());
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            plantForwardLink(contextTwin, linkId, dstTwin);
            var factoryItem = itemWithSingleContext(contextTwin);

            doNothing().when(twinLinkService).loadTwinLinks(contextTwin);
            when(twinService.getTwinFieldValue(dstTwin, lookupFieldId)).thenReturn(null);

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, linkId, lookupFieldId));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
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
