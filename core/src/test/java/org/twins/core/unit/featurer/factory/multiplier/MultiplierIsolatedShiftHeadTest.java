package org.twins.core.unit.featurer.factory.multiplier;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.enums.status.StatusType;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.featurer.factory.multiplier.MultiplierIsolatedShiftHead;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MultiplierIsolatedShiftHeadTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private MultiplierIsolatedShiftHead multiplier;

    @BeforeEach
    void setUp() throws Exception {
        multiplier = new MultiplierIsolatedShiftHead();
        setField(multiplier, "twinService", twinService);
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

    private FactoryItem buildInputItem(TwinEntity twin) {
        var twinCreate = new TwinCreate();
        twinCreate.setTwinEntity(twin);
        return new FactoryItem().setOutput(twinCreate);
    }

    // the head twin is cloned along the multiply path; clone triggers isSketch() which reads
    // twinStatus.getType(), so the head must carry a non-null status.
    private TwinEntity buildHead() {
        var head = new TwinEntity();
        head.setId(UUID.randomUUID());
        head.setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC));
        return head;
    }

    @Nested
    class Multiply {

        @Test
        void multiply_withHead_producesOneOutputPerInputPointingAtHead() throws ServiceException {
            var head = buildHead();
            var input1 = new TwinEntity();
            input1.setId(UUID.randomUUID());
            input1.setHeadTwin(head);
            input1.setHeadTwinId(head.getId());
            var input2 = new TwinEntity();
            input2.setId(UUID.randomUUID());
            input2.setHeadTwin(head);
            input2.setHeadTwinId(head.getId());

            // loadHead populates headTwin on the entity; stub it to do the assignment
            doAnswer(inv -> {
                var t = (TwinEntity) inv.getArgument(0);
                t.setHeadTwin(head);
                return head;
            }).when(twinService).loadHead(any(TwinEntity.class));

            var result = multiplier.multiply(
                    new Properties(),
                    List.of(buildInputItem(input1), buildInputItem(input2)),
                    mock(FactoryContext.class));

            assertEquals(2, result.size());
            // critical: lazy head MUST be loaded before being read
            verify(twinService, times(2)).loadHead(any(TwinEntity.class));

            for (var item : result) {
                assertInstanceOf(TwinUpdate.class, item.getOutput());
                var update = (TwinUpdate) item.getOutput();
                // the "shifted head" output twin must be the HEAD, not the input
                assertSame(head, update.getDbTwinEntity());
            }
        }

        @Test
        void multiply_noHead_inputIsSkipped() throws ServiceException {
            var input = new TwinEntity();
            input.setId(UUID.randomUUID());

            // loadHead returns a (non-void) TwinEntity; stubbing it to return null leaves
            // head unset, so the multiplier skips this input.
            when(twinService.loadHead(any(TwinEntity.class))).thenReturn(null);

            var result = multiplier.multiply(
                    new Properties(),
                    List.of(buildInputItem(input)),
                    mock(FactoryContext.class));

            assertTrue(result.isEmpty());
        }

        @Test
        void multiply_emptyInput_producesNoOutput() throws ServiceException {
            var result = multiplier.multiply(new Properties(), List.of(), mock(FactoryContext.class));

            assertTrue(result.isEmpty());
            verify(twinService, never()).loadHead(any(TwinEntity.class));
        }

        @Test
        void multiply_eachOutputIsScopedToItsOwnInput() throws ServiceException {
            var head = buildHead();
            var input1 = new TwinEntity();
            input1.setId(UUID.randomUUID());
            var input2 = new TwinEntity();
            input2.setId(UUID.randomUUID());

            doAnswer(inv -> {
                ((TwinEntity) inv.getArgument(0)).setHeadTwin(head);
                return head;
            }).when(twinService).loadHead(any(TwinEntity.class));

            var item1 = buildInputItem(input1);
            var item2 = buildInputItem(input2);
            var result = multiplier.multiply(new Properties(), List.of(item1, item2), mock(FactoryContext.class));

            assertEquals(2, result.size());
            assertEquals(1, result.get(0).getContextFactoryItemList().size());
            assertSame(item1, result.get(0).getContextFactoryItemList().get(0));
            assertSame(item2, result.get(1).getContextFactoryItemList().get(0));
        }
    }
}
