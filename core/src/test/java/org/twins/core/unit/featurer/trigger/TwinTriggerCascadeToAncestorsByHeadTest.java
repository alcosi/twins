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

class TwinTriggerCascadeToAncestorsByHeadTest extends BaseUnitTest {

    @Mock
    private TwinRepository twinRepository;

    private TwinTriggerCascadeToAncestorsByHead trigger;

    @BeforeEach
    void setUp() throws Exception {
        trigger = new TwinTriggerCascadeToAncestorsByHead(twinRepository);
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
        twin.setHierarchyTree("root.child.grandchild");
        return twin;
    }

    private Properties buildProperties(int depthValue, UUID classId, UUID statusId) {
        var props = new Properties();
        props.setProperty("depth", String.valueOf(depthValue));
        props.setProperty("classId", classId.toString());
        props.setProperty("dstStatusId", statusId.toString());
        return props;
    }

    @Nested
    class Run {

        @Test
        void run_withPositiveDepth_callsRepositoryWithExactDepth() throws ServiceException {
            var twin = buildTwin();
            var classId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var props = buildProperties(2, classId, statusId);

            when(twinRepository.updateTwinStatusByHeadAncestors(
                    twin.getId(), twin.getHierarchyTree(), 2, classId, statusId))
                    .thenReturn(3);

            trigger.run(props, twin, null, null, null);

            verify(twinRepository).updateTwinStatusByHeadAncestors(
                    twin.getId(), twin.getHierarchyTree(), 2, classId, statusId);
        }

        @Test
        void run_withNullDepth_usesNegativeOne() throws ServiceException {
            var twin = buildTwin();
            var classId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var props = buildProperties(-999, classId, statusId);
            props.remove("depth");

            // depth will be null from extract, which then gets set to -1
            when(twinRepository.updateTwinStatusByHeadAncestors(
                    eq(twin.getId()), eq(twin.getHierarchyTree()), eq(-1), eq(classId), eq(statusId)))
                    .thenReturn(0);

            trigger.run(props, twin, null, null, null);

            verify(twinRepository).updateTwinStatusByHeadAncestors(
                    twin.getId(), twin.getHierarchyTree(), -1, classId, statusId);
        }

        @Test
        void run_withNegativeDepth_usesNegativeOne() throws ServiceException {
            var twin = buildTwin();
            var classId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var props = buildProperties(-5, classId, statusId);

            when(twinRepository.updateTwinStatusByHeadAncestors(
                    eq(twin.getId()), eq(twin.getHierarchyTree()), eq(-1), eq(classId), eq(statusId)))
                    .thenReturn(0);

            trigger.run(props, twin, null, null, null);

            verify(twinRepository).updateTwinStatusByHeadAncestors(
                    twin.getId(), twin.getHierarchyTree(), -1, classId, statusId);
        }

        @Test
        void run_withDepthOne_usesExactDepthOne() throws ServiceException {
            var twin = buildTwin();
            var classId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var props = buildProperties(1, classId, statusId);

            when(twinRepository.updateTwinStatusByHeadAncestors(
                    twin.getId(), twin.getHierarchyTree(), 1, classId, statusId))
                    .thenReturn(1);

            trigger.run(props, twin, null, null, null);

            verify(twinRepository).updateTwinStatusByHeadAncestors(
                    twin.getId(), twin.getHierarchyTree(), 1, classId, statusId);
        }
    }
}
