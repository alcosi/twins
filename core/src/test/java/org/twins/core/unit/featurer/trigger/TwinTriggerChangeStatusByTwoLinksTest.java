package org.twins.core.featurer.trigger;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinStatusEntity;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinTriggerChangeStatusByTwoLinksTest extends BaseUnitTest {

    @Mock
    private TwinRepository twinRepository;

    private TwinTriggerChangeStatusByTwoLinks trigger;

    @BeforeEach
    void setUp() throws Exception {
        trigger = new TwinTriggerChangeStatusByTwoLinks(twinRepository);
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

    private TwinEntity buildTwin() {
        var twin = new TwinEntity();
        twin.setId(UUID.randomUUID());
        return twin;
    }

    private Properties buildProperties(UUID firstLinkId, UUID secondLinkId, UUID classId, UUID statusId) {
        var props = new Properties();
        props.setProperty("firstLinkId", firstLinkId.toString());
        props.setProperty("secondLinkId", secondLinkId.toString());
        props.setProperty("classId", classId.toString());
        props.setProperty("dstStatusId", statusId.toString());
        return props;
    }

    @Nested
    class Run {

        @Test
        void run_callsRepositoryWithCorrectParameters() throws ServiceException {
            var twin = buildTwin();
            var firstLinkId = UUID.randomUUID();
            var secondLinkId = UUID.randomUUID();
            var classId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var props = buildProperties(firstLinkId, secondLinkId, classId, statusId);

            when(twinRepository.updateTwinStatusByTwoLinks(
                    twin.getId(), firstLinkId, secondLinkId, classId, statusId))
                    .thenReturn(3);

            trigger.run(props, twin, null, null, null);

            verify(twinRepository).updateTwinStatusByTwoLinks(
                    twin.getId(), firstLinkId, secondLinkId, classId, statusId);
        }

        @Test
        void run_preservesLinkIdOrder_firstThenSecond() throws ServiceException {
            var twin = buildTwin();
            var firstLinkId = UUID.randomUUID();
            var secondLinkId = UUID.randomUUID();
            var classId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var props = buildProperties(firstLinkId, secondLinkId, classId, statusId);

            when(twinRepository.updateTwinStatusByTwoLinks(
                    any(), eq(firstLinkId), eq(secondLinkId), any(), any()))
                    .thenReturn(1);

            trigger.run(props, twin, null, null, null);

            var inOrder = inOrder(twinRepository);
            inOrder.verify(twinRepository).updateTwinStatusByTwoLinks(
                    twin.getId(), firstLinkId, secondLinkId, classId, statusId);
        }
    }
}
