package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.filler.FillerBasicsAssigneeNull;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FillerBasicsAssigneeNullTest extends BaseUnitTest {

    private FillerBasicsAssigneeNull filler;

    @BeforeEach
    void setUp() {
        filler = new FillerBasicsAssigneeNull();
    }

    private FactoryItem buildFactoryItem() {
        var outputTwin = new TwinEntity();
        outputTwin.setAssignerUser(new UserEntity().setId(UUID.randomUUID()));
        outputTwin.setAssignerUserId(UUID.randomUUID());

        var output = new TwinCreate();
        output.setTwinEntity(outputTwin);
        var factoryItem = new FactoryItem().setOutput(output);
        return factoryItem;
    }

    @Nested
    class Fill {

        @Test
        void fill_clearsBothAssignerUserAndId() throws ServiceException {
            var factoryItem = buildFactoryItem();
            var outputTwin = factoryItem.getOutput().getTwinEntity();

            filler.fill(new Properties(), factoryItem, null);

            assertNull(outputTwin.getAssignerUser());
            assertNull(outputTwin.getAssignerUserId());
        }

        @Test
        void fill_alwaysNullifies_regardlessOfPriorValue() throws ServiceException {
            var factoryItem = buildFactoryItem();
            var outputTwin = factoryItem.getOutput().getTwinEntity();
            assertNotNull(outputTwin.getAssignerUserId());

            filler.fill(new Properties(), factoryItem, null);

            assertNull(outputTwin.getAssignerUserId());
        }
    }
}
