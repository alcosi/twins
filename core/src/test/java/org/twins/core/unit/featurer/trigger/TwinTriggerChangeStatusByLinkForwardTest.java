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

class TwinTriggerChangeStatusByLinkForwardTest extends BaseUnitTest {

    @Mock
    private TwinRepository twinRepository;

    private TwinTriggerChangeStatusByLinkForward trigger;

    @BeforeEach
    void setUp() throws Exception {
        trigger = new TwinTriggerChangeStatusByLinkForward(twinRepository);
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

    private Properties buildProperties(UUID linkId, UUID statusId) {
        var props = new Properties();
        props.setProperty("linkId", linkId.toString());
        props.setProperty("dstStatusId", statusId.toString());
        return props;
    }

    @Nested
    class Run {

        @Test
        void run_callsRepositoryWithCorrectParameters() throws ServiceException {
            var twin = buildTwin();
            var linkId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var props = buildProperties(linkId, statusId);

            when(twinRepository.updateTwinStatusBySrcTwinIdAndLinkId(
                    twin.getId(), linkId, null, statusId))
                    .thenReturn(1);

            trigger.run(props, twin, null, null, null);

            verify(twinRepository).updateTwinStatusBySrcTwinIdAndLinkId(
                    twin.getId(), linkId, null, statusId);
        }
    }
}
