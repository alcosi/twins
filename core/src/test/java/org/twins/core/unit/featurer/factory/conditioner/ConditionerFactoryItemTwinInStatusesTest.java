package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemTwinInStatuses;

import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionerFactoryItemTwinInStatusesTest extends BaseUnitTest {

    private ConditionerFactoryItemTwinInStatuses conditioner;

    @BeforeEach
    void setUp() {
        conditioner = new ConditionerFactoryItemTwinInStatuses();
    }

    private Properties props(Set<UUID> statusIds) {
        var p = new Properties();
        p.put("statusIds", String.join(",", statusIds.stream().map(UUID::toString).toList()));
        return p;
    }

    private FactoryItem item(UUID outputStatusId) {
        var twin = new TwinEntity().setTwinStatusId(outputStatusId);
        var output = new TwinCreate();
        output.setTwinEntity(twin);
        return new FactoryItem().setOutput(output);
    }

    @Nested
    class Check {

        @Test
        void check_outputTwinStatusInSet_returnsTrue() throws ServiceException {
            // contract: matches when the OUTPUT twin's status is in the configured set.
            var statusId = UUID.randomUUID();
            var statuses = new LinkedHashSet<UUID>();
            statuses.add(statusId);

            assertTrue(conditioner.check(props(statuses), item(statusId)));
        }

        @Test
        void check_outputTwinStatusNotInSet_returnsFalse() throws ServiceException {
            var configured = UUID.randomUUID();
            var statuses = new LinkedHashSet<UUID>();
            statuses.add(configured);

            assertFalse(conditioner.check(props(statuses), item(UUID.randomUUID())));
        }

        @Test
        void check_emptyStatusSet_returnsFalse() throws ServiceException {
            assertFalse(conditioner.check(props(new LinkedHashSet<>()), item(UUID.randomUUID())));
        }

        @Test
        void check_multipleStatuses_oneMatches_returnsTrue() throws ServiceException {
            var match = UUID.randomUUID();
            var statuses = new LinkedHashSet<UUID>();
            statuses.add(UUID.randomUUID());
            statuses.add(match);

            assertTrue(conditioner.check(props(statuses), item(match)));
        }
    }
}
