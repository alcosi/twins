package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.enums.status.StatusType;
import org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemDbTwinInStatuses;

import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionerFactoryItemDbTwinInStatusesTest extends BaseUnitTest {

    private ConditionerFactoryItemDbTwinInStatuses conditioner;

    @BeforeEach
    void setUp() {
        conditioner = new ConditionerFactoryItemDbTwinInStatuses();
    }

    // FeaturerParamUUIDSet parses a CSV string under key "statusIds".
    private Properties props(Set<UUID> statusIds) {
        var p = new Properties();
        p.put("statusIds", String.join(",", statusIds.stream().map(UUID::toString).toList()));
        return p;
    }

    // TwinUpdate.setDbTwinEntity calls TwinEntity.isSketch() -> twinStatus.getType() -> needs a status.
    private TwinEntity dbTwin(UUID statusId) {
        return new TwinEntity()
                .setTwinStatusId(statusId)
                .setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC));
    }

    private FactoryItem updateItem(TwinEntity dbTwin) {
        var output = new TwinUpdate();
        output.setDbTwinEntity(dbTwin);
        // output twinEntity is the new version; not relevant to the db-twin check, but must be non-null
        output.setTwinEntity(dbTwin);
        return new FactoryItem().setOutput(output);
    }

    private FactoryItem createItem() {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        return new FactoryItem().setOutput(output);
    }

    @Nested
    class Check {

        @Test
        void check_dbTwinStatusInSet_returnsTrue() throws ServiceException {
            // contract: a TwinUpdate whose persisted (db) twin status is in the configured set matches.
            var statusId = UUID.randomUUID();
            var statuses = new LinkedHashSet<UUID>();
            statuses.add(statusId);

            assertTrue(conditioner.check(props(statuses), updateItem(dbTwin(statusId))));
        }

        @Test
        void check_dbTwinStatusNotInSet_returnsFalse() throws ServiceException {
            var configured = UUID.randomUUID();
            var statuses = new LinkedHashSet<UUID>();
            statuses.add(configured);

            assertFalse(conditioner.check(props(statuses), updateItem(dbTwin(UUID.randomUUID()))));
        }

        @Test
        void check_createOperation_returnsFalse() throws ServiceException {
            // contract: a TwinCreate has no persisted db twin to check -> never matches.
            var statusId = UUID.randomUUID();
            var statuses = new LinkedHashSet<UUID>();
            statuses.add(statusId);

            assertFalse(conditioner.check(props(statuses), createItem()));
        }

        @Test
        void check_emptyStatusSet_returnsFalse() throws ServiceException {
            // contract: an empty configured set contains nothing.
            assertFalse(conditioner.check(props(new LinkedHashSet<>()),
                    updateItem(dbTwin(UUID.randomUUID()))));
        }

        @Test
        void check_multipleStatuses_oneMatches_returnsTrue() throws ServiceException {
            var match = UUID.randomUUID();
            var statuses = new LinkedHashSet<UUID>();
            statuses.add(UUID.randomUUID());
            statuses.add(match);

            assertTrue(conditioner.check(props(statuses), updateItem(dbTwin(match))));
        }
    }
}
