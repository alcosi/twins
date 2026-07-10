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
import org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemTwinAssigneeEqualsContextTwinFieldLinkAssignee;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextFields;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.service.link.TwinLinkService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConditionerFactoryItemTwinAssigneeEqualsContextTwinFieldLinkAssigneeTest extends BaseUnitTest {

    @Mock
    private FieldLookupers fieldLookupers;

    @Mock
    private FieldLookuperFromContextFields lookuper;

    @Mock
    private TwinLinkService twinLinkService;

    private ConditionerFactoryItemTwinAssigneeEqualsContextTwinFieldLinkAssignee conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerFactoryItemTwinAssigneeEqualsContextTwinFieldLinkAssignee();
        setField(conditioner, "fieldLookupers", fieldLookupers);
        setField(conditioner, "twinLinkService", twinLinkService);
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

    private FactoryItem item(UUID outputAssignerId) {
        var twin = new TwinEntity().setAssignerUserId(outputAssignerId);
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
        void check_dstTwinAssignerEqualsOutputTwinAssigner_returnsTrue() throws ServiceException {
            // contract: matches when the link's dst twin assigner == the item output twin assigner.
            var fieldId = UUID.randomUUID();
            var assignerId = UUID.randomUUID();
            var dstTwin = new TwinEntity().setAssignerUserId(assignerId);
            var link = mock(TwinLinkEntity.class);
            when(link.getDstTwin()).thenReturn(dstTwin);
            stubLinkField(fieldId, link);

            assertTrue(conditioner.check(props(fieldId), item(assignerId)));
        }

        @Test
        void check_dstTwinAssignerDiffersFromOutputTwinAssigner_returnsFalse() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var dstTwin = new TwinEntity().setAssignerUserId(UUID.randomUUID());
            var link = mock(TwinLinkEntity.class);
            when(link.getDstTwin()).thenReturn(dstTwin);
            stubLinkField(fieldId, link);

            assertFalse(conditioner.check(props(fieldId), item(UUID.randomUUID())));
        }

        @Test
        void check_dstTwinLoadedViaTwinLinkService() throws ServiceException {
            // contract: the link's dst twin is loaded via twinLinkService.loadDstTwin before comparison.
            var fieldId = UUID.randomUUID();
            var assignerId = UUID.randomUUID();
            var link = mock(TwinLinkEntity.class);
            var fetched = new TwinEntity().setAssignerUserId(assignerId);
            when(link.getDstTwin()).thenReturn(fetched);
            stubLinkField(fieldId, link);

            assertTrue(conditioner.check(props(fieldId), item(assignerId)));

            verify(twinLinkService).loadDstTwin(link);
        }

        @Test
        void check_looksUpFromContextFields_notContextTwinDb() throws ServiceException {
            // contract per class name: the field is looked up via the context-FIELDS lookuper
            // (not the context-twin-db lookuper).
            var fieldId = UUID.randomUUID();
            var assignerId = UUID.randomUUID();
            var dstTwin = new TwinEntity().setAssignerUserId(assignerId);
            var link = mock(TwinLinkEntity.class);
            when(link.getDstTwin()).thenReturn(dstTwin);
            stubLinkField(fieldId, link);

            conditioner.check(props(fieldId), item(assignerId));

            verify(fieldLookupers).getFromContextFields();
        }
    }
}
