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
import org.twins.core.featurer.factory.conditioner.ConditionerContextItemTwinAssigneeEqualsContextTwinFieldLinkAssignee;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextFields;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionerContextItemTwinAssigneeEqualsContextTwinFieldLinkAssigneeTest extends BaseUnitTest {

    @Mock
    private FieldLookupers fieldLookupers;

    @Mock
    private FieldLookuperFromContextFields lookuper;

    @Mock
    private TwinService twinService;

    private ConditionerContextItemTwinAssigneeEqualsContextTwinFieldLinkAssignee conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerContextItemTwinAssigneeEqualsContextTwinFieldLinkAssignee();
        setField(conditioner, "fieldLookupers", fieldLookupers);
        setField(conditioner, "twinService", twinService);
        when(fieldLookupers.getFromContextFields()).thenReturn(lookuper);
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

    private Properties props(UUID twinClassFieldId) {
        var p = new Properties();
        p.put("twinClassFieldId", twinClassFieldId.toString());
        return p;
    }

    private FactoryItem buildItem(TwinEntity dstTwin, UUID contextAssignerId) throws ServiceException {
        var link = mock(TwinLinkEntity.class);
        var fieldValue = mock(FieldValueLink.class);
        when(lookuper.lookupFieldValue(org.mockito.ArgumentMatchers.any(FactoryItem.class), any(UUID.class))).thenReturn(fieldValue);
        when(fieldValue.getItems()).thenReturn(List.of(link));
        if (dstTwin != null) {
            when(link.getDstTwin()).thenReturn(dstTwin);
        } else {
            when(link.getDstTwin()).thenReturn(null);
        }

        var contextTwin = mock(TwinEntity.class);
        when(contextTwin.getAssignerUserId()).thenReturn(contextAssignerId);
        var contextItem = mock(FactoryItem.class);
        when(contextItem.getTwin()).thenReturn(contextTwin);
        var factoryItem = mock(FactoryItem.class);
        when(factoryItem.checkSingleContextItem()).thenReturn(contextItem);
        return factoryItem;
    }

    @Nested
    class Check {

        @Test
        void check_dstTwinAssignerEqualsContextTwinAssigner_returnsTrue() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var assignerId = UUID.randomUUID();
            var dstTwin = mock(TwinEntity.class);
            when(dstTwin.getAssignerUserId()).thenReturn(assignerId);

            assertTrue(conditioner.check(props(fieldId), buildItem(dstTwin, assignerId)));
        }

        @Test
        void check_dstTwinAssignerDiffersFromContextTwinAssigner_returnsFalse() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var dstTwin = mock(TwinEntity.class);
            when(dstTwin.getAssignerUserId()).thenReturn(UUID.randomUUID());

            assertFalse(conditioner.check(props(fieldId), buildItem(dstTwin, UUID.randomUUID())));
        }

        @Test
        void check_dstTwinNotLoaded_fetchedFromServiceThenCompared() throws ServiceException {
            // contract: when the link's dstTwin is not preloaded, it is fetched by dstTwinId
            var fieldId = UUID.randomUUID();
            var assignerId = UUID.randomUUID();
            var dstTwinId = UUID.randomUUID();

            var link = mock(TwinLinkEntity.class);
            var fieldValue = mock(FieldValueLink.class);
            when(lookuper.lookupFieldValue(org.mockito.ArgumentMatchers.any(FactoryItem.class),
                    eq(fieldId))).thenReturn(fieldValue);
            when(fieldValue.getItems()).thenReturn(List.of(link));
            when(link.getDstTwin()).thenReturn(null);
            when(link.getDstTwinId()).thenReturn(dstTwinId);
            var fetched = mock(TwinEntity.class);
            when(fetched.getAssignerUserId()).thenReturn(assignerId);
            when(twinService.findEntitySafe(eq(dstTwinId))).thenReturn(fetched);

            var contextTwin = mock(TwinEntity.class);
            when(contextTwin.getAssignerUserId()).thenReturn(assignerId);
            var contextItem = mock(FactoryItem.class);
            when(contextItem.getTwin()).thenReturn(contextTwin);
            var factoryItem = mock(FactoryItem.class);
            when(factoryItem.checkSingleContextItem()).thenReturn(contextItem);

            assertTrue(conditioner.check(props(fieldId), factoryItem));

            // the fetched twin must be cached back onto the link
            org.mockito.Mockito.verify(link).setDstTwin(fetched);
        }

        @Test
        void check_comparesDstAssignerAgainstSingleContextItemTwin() throws ServiceException {
            // contract per class name: compared twin is the SINGLE context item's twin assigner
            var fieldId = UUID.randomUUID();
            var assignerId = UUID.randomUUID();
            var dstTwin = mock(TwinEntity.class);
            when(dstTwin.getAssignerUserId()).thenReturn(assignerId);

            var item = buildItem(dstTwin, assignerId);
            conditioner.check(props(fieldId), item);

            // contract: the comparison twin comes from checkSingleContextItem() (exactly-one context)
            org.mockito.Mockito.verify(item).checkSingleContextItem();
        }
    }
}
