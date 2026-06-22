package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemHeadAssigneeTwinAssigneeEqualsContextTwinFieldLinkAssignee;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextFields;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConditionerFactoryItemHeadAssigneeTwinAssigneeEqualsContextTwinFieldLinkAssigneeTest extends BaseUnitTest {

    @Mock
    private FieldLookupers fieldLookupers;

    @Mock
    private FieldLookuperFromContextFields lookuper;

    @Mock
    private TwinService twinService;

    private ConditionerFactoryItemHeadAssigneeTwinAssigneeEqualsContextTwinFieldLinkAssignee conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerFactoryItemHeadAssigneeTwinAssigneeEqualsContextTwinFieldLinkAssignee();
        setField(conditioner, "fieldLookupers", fieldLookupers);
        setField(conditioner, "twinService", twinService);
        when(fieldLookupers.getFromContextFields()).thenReturn(lookuper);
    }

    private void setField(Object target, String name, Object value) throws Exception {
        var field = findField(target.getClass(), name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String name) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("Field not found: " + name);
    }

    private Properties props(UUID fieldId) {
        var p = new Properties();
        p.put("twinClassFieldId", fieldId.toString());
        return p;
    }

    private FactoryItem item() {
        var twin = new TwinEntity();
        var output = new TwinCreate();
        output.setTwinEntity(twin);
        return new FactoryItem().setOutput(output);
    }

    private void stubLinkField(UUID fieldId, TwinLinkEntity link) throws ServiceException {
        var fieldValue = mock(FieldValueLink.class);
        when(lookuper.lookupFieldValue(any(FactoryItem.class), eq(fieldId))).thenReturn(fieldValue);
        when(fieldValue.getItems()).thenReturn(List.of(link));
    }

    @Nested
    class Check {

        @Test
        void check_headTwinAssignerEqualsDstAssigner_returnsTrue() throws ServiceException {
            // contract: matches when the loaded HEAD twin's assigner == the link dst twin assigner.
            var fieldId = UUID.randomUUID();
            var assignerId = UUID.randomUUID();
            var dstTwin = new TwinEntity().setAssignerUserId(assignerId);
            var link = mock(TwinLinkEntity.class);
            when(link.getDstTwin()).thenReturn(dstTwin);
            stubLinkField(fieldId, link);
            var head = new TwinEntity().setAssignerUserId(assignerId);
            when(twinService.loadHeadForTwin(any(TwinEntity.class))).thenReturn(head);

            assertTrue(conditioner.check(props(fieldId), item()));
        }

        @Test
        void check_headTwinAssignerDiffersFromDstAssigner_returnsFalse() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var dstTwin = new TwinEntity().setAssignerUserId(UUID.randomUUID());
            var link = mock(TwinLinkEntity.class);
            when(link.getDstTwin()).thenReturn(dstTwin);
            stubLinkField(fieldId, link);
            var head = new TwinEntity().setAssignerUserId(UUID.randomUUID());
            when(twinService.loadHeadForTwin(any(TwinEntity.class))).thenReturn(head);

            assertFalse(conditioner.check(props(fieldId), item()));
        }

        @Test
        void check_noHeadTwin_throwsFactoryPipelineStepError() throws ServiceException {
            // contract: a missing head twin is a pipeline misconfiguration -> ServiceException.
            var fieldId = UUID.randomUUID();
            var dstTwin = new TwinEntity().setAssignerUserId(UUID.randomUUID());
            var link = mock(TwinLinkEntity.class);
            when(link.getDstTwin()).thenReturn(dstTwin);
            stubLinkField(fieldId, link);
            when(twinService.loadHeadForTwin(any(TwinEntity.class))).thenReturn(null);

            var ex = assertThrows(ServiceException.class,
                    () -> conditioner.check(props(fieldId), item()));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void check_dstTwinNotPreloaded_fetchedAndCached() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var dstTwinId = UUID.randomUUID();
            var assignerId = UUID.randomUUID();
            var link = mock(TwinLinkEntity.class);
            when(link.getDstTwin()).thenReturn(null);
            when(link.getDstTwinId()).thenReturn(dstTwinId);
            stubLinkField(fieldId, link);
            var fetched = new TwinEntity().setAssignerUserId(assignerId);
            when(twinService.findEntitySafe(eq(dstTwinId))).thenReturn(fetched);
            var head = new TwinEntity().setAssignerUserId(assignerId);
            when(twinService.loadHeadForTwin(any(TwinEntity.class))).thenReturn(head);

            assertTrue(conditioner.check(props(fieldId), item()));

            verify(link).setDstTwin(fetched);
        }

        @Test
        void check_loadsHeadForItemTwin() throws ServiceException {
            // contract: the head is loaded for the factory item's twin (output twin).
            var fieldId = UUID.randomUUID();
            var assignerId = UUID.randomUUID();
            var dstTwin = new TwinEntity().setAssignerUserId(assignerId);
            var link = mock(TwinLinkEntity.class);
            when(link.getDstTwin()).thenReturn(dstTwin);
            stubLinkField(fieldId, link);
            var head = new TwinEntity().setAssignerUserId(assignerId);
            when(twinService.loadHeadForTwin(any(TwinEntity.class))).thenReturn(head);

            conditioner.check(props(fieldId), item());

            verify(twinService).loadHeadForTwin(any(TwinEntity.class));
        }
    }
}
