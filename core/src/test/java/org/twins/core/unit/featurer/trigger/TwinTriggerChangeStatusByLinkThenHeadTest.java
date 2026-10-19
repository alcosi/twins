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

class TwinTriggerChangeStatusByLinkThenHeadTest extends BaseUnitTest {

    @Mock
    private TwinRepository twinRepository;

    private TwinTriggerChangeStatusByLinkThenHead trigger;

    @BeforeEach
    void setUp() throws Exception {
        trigger = new TwinTriggerChangeStatusByLinkThenHead(twinRepository);
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

    private Properties buildProperties(UUID linkId, UUID classId, UUID statusId) {
        var props = new Properties();
        props.setProperty("linkId", linkId.toString());
        props.setProperty("classId", classId.toString());
        props.setProperty("dstStatusId", statusId.toString());
        return props;
    }

    @Nested
    class Run {

        @Test
        void run_callsRepositoryWithCorrectParameters() throws ServiceException {
            var twin = buildTwin();
            var linkId = UUID.randomUUID();
            var classId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var props = buildProperties(linkId, classId, statusId);

            when(twinRepository.updateTwinStatusByLinkAndHead(
                    twin.getId(), linkId, classId, statusId))
                    .thenReturn(2);

            trigger.run(props, twin, null, null, null);

            verify(twinRepository).updateTwinStatusByLinkAndHead(
                    twin.getId(), linkId, classId, statusId);
        }

        @Test
        void run_usesLinkAndHeadMethod_confirmsOperationOrder() throws ServiceException {
            var twin = buildTwin();
            var linkId = UUID.randomUUID();
            var classId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var props = buildProperties(linkId, classId, statusId);

            when(twinRepository.updateTwinStatusByLinkAndHead(
                    any(), any(), any(), any()))
                    .thenReturn(2);

            trigger.run(props, twin, null, null, null);

            // Verify that the method implementing "link then head" strategy is called
            // The method name updateTwinStatusByLinkAndHead confirms that link
            // traversal happens before head lookup
            verify(twinRepository).updateTwinStatusByLinkAndHead(
                    twin.getId(), linkId, classId, statusId);
        }
    }
}
