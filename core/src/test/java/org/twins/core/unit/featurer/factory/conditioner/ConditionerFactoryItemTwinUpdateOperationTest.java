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
import org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemTwinUpdateOperation;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionerFactoryItemTwinUpdateOperationTest extends BaseUnitTest {

    private ConditionerFactoryItemTwinUpdateOperation conditioner;

    @BeforeEach
    void setUp() {
        conditioner = new ConditionerFactoryItemTwinUpdateOperation();
    }

    private FactoryItem createItem() {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        return new FactoryItem().setOutput(output);
    }

    private FactoryItem updateItem() {
        var db = new TwinEntity().setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC));
        var output = new TwinUpdate();
        output.setDbTwinEntity(db);
        output.setTwinEntity(db);
        return new FactoryItem().setOutput(output);
    }

    @Nested
    class Check {

        @Test
        void check_outputIsUpdate_returnsTrue() throws ServiceException {
            // contract: matches when output is a TwinUpdate (existing-twin operation).
            assertTrue(conditioner.check(new Properties(), updateItem()));
        }

        @Test
        void check_outputIsCreate_returnsFalse() throws ServiceException {
            assertFalse(conditioner.check(new Properties(), createItem()));
        }
    }
}
