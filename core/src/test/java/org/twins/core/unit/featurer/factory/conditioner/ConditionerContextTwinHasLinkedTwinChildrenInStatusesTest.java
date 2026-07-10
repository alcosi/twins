package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.factory.conditioner.ConditionerContextTwinHasLinkedTwinChildrenInStatuses;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionerContextTwinHasLinkedTwinChildrenInStatusesTest extends BaseUnitTest {

    @Mock
    private TwinRepository twinRepository;

    private ConditionerContextTwinHasLinkedTwinChildrenInStatuses conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerContextTwinHasLinkedTwinChildrenInStatuses();
        setField(conditioner, "twinRepository", twinRepository);
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

    // properties store the set as a comma-joined string (FeaturerParamUUIDSet contract)
    private Properties buildProperties(UUID linkId, Set<UUID> statusIds) {
        var props = new Properties();
        props.put("linkId", linkId.toString());
        props.put("statusIds", String.join(",",
                statusIds.stream().map(UUID::toString).toList()));
        return props;
    }

    private FactoryItem buildItem(UUID twinId) {
        var twin = mock(TwinEntity.class);
        when(twin.getId()).thenReturn(twinId);
        var item = mock(FactoryItem.class);
        when(item.getTwin()).thenReturn(twin);
        return item;
    }

    @Nested
    class Check {

        @Test
        void check_childrenExist_returnsTrue() throws ServiceException {
            var twinId = UUID.randomUUID();
            var linkId = UUID.randomUUID();
            var statusIds = new LinkedHashSet<UUID>();
            statusIds.add(UUID.randomUUID());
            when(twinRepository.existsChildrenByBackwardLinkAndStatuses(
                    eq(twinId), eq(linkId), eq(statusIds))).thenReturn(true);

            assertTrue(conditioner.check(buildProperties(linkId, statusIds), buildItem(twinId)));
        }

        @Test
        void check_noChildren_returnsFalse() throws ServiceException {
            var twinId = UUID.randomUUID();
            var linkId = UUID.randomUUID();
            var statusIds = new LinkedHashSet<UUID>();
            statusIds.add(UUID.randomUUID());
            when(twinRepository.existsChildrenByBackwardLinkAndStatuses(
                    eq(twinId), eq(linkId), eq(statusIds))).thenReturn(false);

            assertFalse(conditioner.check(buildProperties(linkId, statusIds), buildItem(twinId)));
        }

        @Test
        void check_factoryItemTwinNull_throws() {
            // contract: a missing twin is a pipeline misconfiguration, not a "false" result
            var linkId = UUID.randomUUID();
            var statusIds = new LinkedHashSet<UUID>();
            statusIds.add(UUID.randomUUID());
            var item = mock(FactoryItem.class);
            when(item.getTwin()).thenReturn(null);

            assertThrows(ServiceException.class,
                    () -> conditioner.check(buildProperties(linkId, statusIds), item));
        }

        @Test
        void check_usesItemTwinIdNotContextTwin() throws ServiceException {
            // contract: children are looked up by the factory ITEM twin's id
            var twinId = UUID.randomUUID();
            var linkId = UUID.randomUUID();
            var statusIds = new LinkedHashSet<UUID>();
            statusIds.add(UUID.randomUUID());
            when(twinRepository.existsChildrenByBackwardLinkAndStatuses(
                    eq(twinId), eq(linkId), eq(statusIds))).thenReturn(true);

            conditioner.check(buildProperties(linkId, statusIds), buildItem(twinId));

            org.mockito.Mockito.verify(twinRepository)
                    .existsChildrenByBackwardLinkAndStatuses(eq(twinId), eq(linkId), eq(statusIds));
        }
    }
}
