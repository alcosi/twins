package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.twinfield.TwinFieldSimpleService;
import org.twins.core.dao.twin.TwinFieldSimpleNoRelationsProjection;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinSave;
import org.twins.core.featurer.factory.conditioner.ConditionerMathCompareChildrenTwinFieldValueWithParentTwinFieldValue;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromItemOutputUncommitedFields;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConditionerMathCompareChildrenTwinFieldValueWithParentTwinFieldValueTest extends BaseUnitTest {

    @Mock
    private FieldLookupers fieldLookupers;

    @Mock
    private FieldLookuperFromItemOutputUncommitedFields lookuper;

    @Mock
    private TwinFieldSimpleService twinFieldSimpleService;

    private ConditionerMathCompareChildrenTwinFieldValueWithParentTwinFieldValue conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerMathCompareChildrenTwinFieldValueWithParentTwinFieldValue();
        setField(conditioner, "fieldLookupers", fieldLookupers);
        setField(conditioner, "twinFieldSimpleService", twinFieldSimpleService);
        // lenient: the throwing / no-children tests below never resolve a field via the lookuper
        lenient().when(fieldLookupers.getFromItemOutputUncommitedFields()).thenReturn(lookuper);
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

    private Properties props(UUID greaterFieldId, UUID comparisonFieldId, boolean equals, UUID statusId) {
        var p = new Properties();
        p.put("greaterTwinClassField", greaterFieldId.toString());
        p.put("comparisonTwinClassField", comparisonFieldId.toString());
        p.put("equals", String.valueOf(equals));
        p.put("statusIds", statusId.toString());
        return p;
    }

    private FactoryItem item(FieldValue greaterValue) throws ServiceException {
        var outputTwin = mock(TwinEntity.class);
        var outputTwinId = UUID.randomUUID();
        when(outputTwin.getId()).thenReturn(outputTwinId);
        var output = mock(TwinSave.class);
        when(output.getTwinEntity()).thenReturn(outputTwin);
        var ctx = mock(FactoryContext.class);
        when(ctx.getInputTwinList()).thenReturn(List.of());
        var item = mock(FactoryItem.class);
        when(item.getOutput()).thenReturn(output);
        when(item.getFactoryContext()).thenReturn(ctx);
        // the greater value is resolved off the output uncommited fields lookuper
        when(lookuper.lookupFieldValue(any(FactoryItem.class), any(UUID.class))).thenReturn(greaterValue);
        when(twinFieldSimpleService.findTwinFieldsSimple(
                any(List.class), any(Set.class), any(Set.class))).thenReturn(List.of());
        return item;
    }

    @Nested
    class Check {

        @Test
        void check_noChildrenFound_returnsTrue() throws ServiceException {
            // contract: "is there NOT at least one twin that satisfies?" — empty children → true
            var greaterFieldId = UUID.randomUUID();
            var comparisonFieldId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var greater = mock(FieldValueText.class);
            lenient().when(greater.getValue()).thenReturn("10");

            assertTrue(conditioner.check(props(greaterFieldId, comparisonFieldId, false, statusId), item(greater)));
        }

        @Test
        void check_childSatisfiesStrictParentGreater_returnsFalse() throws ServiceException {
            // equals=false → condition is parent > child; satisfied → returns false
            var statusId = UUID.randomUUID();
            var greaterFieldId = UUID.randomUUID();
            var comparisonFieldId = UUID.randomUUID();
            var greater = mock(FieldValueText.class);
            lenient().when(greater.getValue()).thenReturn("10"); // parent

            var childProjection = new TwinFieldSimpleNoRelationsProjection(
                    UUID.randomUUID(), UUID.randomUUID(), comparisonFieldId, "5"); // child=5, parent 10>5 → satisfied

            var output = mock(TwinSave.class);
            var outputTwin = mock(TwinEntity.class);
            var outputTwinId = UUID.randomUUID();
            when(outputTwin.getId()).thenReturn(outputTwinId);
            when(output.getTwinEntity()).thenReturn(outputTwin);
            var ctx = mock(FactoryContext.class);
            when(ctx.getInputTwinList()).thenReturn(List.of());
            var fi = mock(FactoryItem.class);
            when(fi.getOutput()).thenReturn(output);
            when(fi.getFactoryContext()).thenReturn(ctx);
            when(lookuper.lookupFieldValue(any(FactoryItem.class), any(UUID.class))).thenReturn(greater);
            when(twinFieldSimpleService.findTwinFieldsSimple(
                    any(List.class), any(Set.class), any(Set.class))).thenReturn(List.of(childProjection));

            assertFalse(conditioner.check(props(greaterFieldId, comparisonFieldId, false, statusId), fi));
        }

        @Test
        void check_childEqualsParent_strictReturnsTrue_equalToReturnsFalse() throws ServiceException {
            // equals=false → parent > child required → parent==child NOT satisfied → true
            // equals=true  → parent >= child required → satisfied → false
            var statusId = UUID.randomUUID();
            var greaterFieldId = UUID.randomUUID();
            var comparisonFieldId = UUID.randomUUID();
            var greater = mock(FieldValueText.class);
            lenient().when(greater.getValue()).thenReturn("10");

            var childProjection = new TwinFieldSimpleNoRelationsProjection(
                    UUID.randomUUID(), UUID.randomUUID(), comparisonFieldId, "10");

            var output = mock(TwinSave.class);
            var outputTwin = mock(TwinEntity.class);
            when(outputTwin.getId()).thenReturn(UUID.randomUUID());
            when(output.getTwinEntity()).thenReturn(outputTwin);
            var ctx = mock(FactoryContext.class);
            when(ctx.getInputTwinList()).thenReturn(List.of());
            var fi = mock(FactoryItem.class);
            when(fi.getOutput()).thenReturn(output);
            when(fi.getFactoryContext()).thenReturn(ctx);
            when(lookuper.lookupFieldValue(any(FactoryItem.class), any(UUID.class))).thenReturn(greater);
            when(twinFieldSimpleService.findTwinFieldsSimple(
                    any(List.class), any(Set.class), any(Set.class))).thenReturn(List.of(childProjection));

            assertTrue(conditioner.check(props(greaterFieldId, comparisonFieldId, false, statusId), fi));
            assertFalse(conditioner.check(props(greaterFieldId, comparisonFieldId, true, statusId), fi));
        }

        @Test
        void check_greaterValueNotText_throws() throws ServiceException {
            var statusId = UUID.randomUUID();
            var greaterFieldId = UUID.randomUUID();
            var comparisonFieldId = UUID.randomUUID();
            var notText = mock(FieldValue.class);

            var childProjection = new TwinFieldSimpleNoRelationsProjection(
                    UUID.randomUUID(), UUID.randomUUID(), comparisonFieldId, "5");

            var output = mock(TwinSave.class);
            var outputTwin = mock(TwinEntity.class);
            when(outputTwin.getId()).thenReturn(UUID.randomUUID());
            when(output.getTwinEntity()).thenReturn(outputTwin);
            var ctx = mock(FactoryContext.class);
            when(ctx.getInputTwinList()).thenReturn(List.of());
            var fi = mock(FactoryItem.class);
            when(fi.getOutput()).thenReturn(output);
            when(fi.getFactoryContext()).thenReturn(ctx);
            when(lookuper.lookupFieldValue(any(FactoryItem.class), any(UUID.class))).thenReturn(notText);
            when(twinFieldSimpleService.findTwinFieldsSimple(
                    any(List.class), any(Set.class), any(Set.class))).thenReturn(List.of(childProjection));

            assertThrows(ServiceException.class,
                    () -> conditioner.check(props(greaterFieldId, comparisonFieldId, false, statusId), fi));
        }

        @Test
        void check_childValueNotNumeric_throws() throws ServiceException {
            var statusId = UUID.randomUUID();
            var greaterFieldId = UUID.randomUUID();
            var comparisonFieldId = UUID.randomUUID();
            var greater = mock(FieldValueText.class);
            lenient().when(greater.getValue()).thenReturn("10");

            var childProjection = new TwinFieldSimpleNoRelationsProjection(
                    UUID.randomUUID(), UUID.randomUUID(), comparisonFieldId, "not-a-number");

            var output = mock(TwinSave.class);
            var outputTwin = mock(TwinEntity.class);
            when(outputTwin.getId()).thenReturn(UUID.randomUUID());
            when(output.getTwinEntity()).thenReturn(outputTwin);
            var ctx = mock(FactoryContext.class);
            when(ctx.getInputTwinList()).thenReturn(List.of());
            var fi = mock(FactoryItem.class);
            when(fi.getOutput()).thenReturn(output);
            when(fi.getFactoryContext()).thenReturn(ctx);
            when(lookuper.lookupFieldValue(any(FactoryItem.class), any(UUID.class))).thenReturn(greater);
            when(twinFieldSimpleService.findTwinFieldsSimple(
                    any(List.class), any(Set.class), any(Set.class))).thenReturn(List.of(childProjection));

            assertThrows(ServiceException.class,
                    () -> conditioner.check(props(greaterFieldId, comparisonFieldId, false, statusId), fi));
        }
    }
}
