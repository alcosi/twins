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
import org.twins.core.featurer.factory.filler.FillerBasicsAssigneeFromContextTwinCreatedBy;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FillerBasicsAssigneeFromContextTwinCreatedByTest extends BaseUnitTest {

    private FillerBasicsAssigneeFromContextTwinCreatedBy filler;

    @BeforeEach
    void setUp() {
        filler = new FillerBasicsAssigneeFromContextTwinCreatedBy();
    }

    private FactoryItem buildFactoryItem(TwinEntity contextTwin) {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        var contextOutput = new TwinCreate();
        contextOutput.setTwinEntity(contextTwin);
        var contextItem = new FactoryItem().setOutput(contextOutput);
        return new FactoryItem().setOutput(output).setContextFactoryItemList(List.of(contextItem));
    }

    @Nested
    class Fill {

        @Test
        void fill_copiesContextTwinCreatedBy_asOutputAssignee() throws ServiceException {
            var creatorId = UUID.randomUUID();
            var creator = new UserEntity().setId(creatorId);
            // context twin has a DISTINCT assignee that must NOT be used
            var assignee = new UserEntity().setId(UUID.randomUUID());
            var contextTwin = new TwinEntity()
                    .setCreatedByUser(creator)
                    .setCreatedByUserId(creatorId)
                    .setAssignerUser(assignee)
                    .setAssignerUserId(assignee.getId());
            var factoryItem = buildFactoryItem(contextTwin);

            filler.fill(new Properties(), List.of(factoryItem), null, false);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            // NAME promises: assignee comes FROM createdBy, NOT from assignee.
            assertSame(creator, outputTwin.getAssignerUser());
            assertEquals(creatorId, outputTwin.getAssignerUserId());
        }

        @Test
        void fill_doesNotReadContextTwinAssignee() throws ServiceException {
            var creatorId = UUID.randomUUID();
            var contextTwin = new TwinEntity()
                    .setCreatedByUserId(creatorId);
            // assignee on context is set to something the filler must ignore
            contextTwin.setAssignerUserId(UUID.randomUUID());
            var factoryItem = buildFactoryItem(contextTwin);

            filler.fill(new Properties(), List.of(factoryItem), null, false);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            assertEquals(creatorId, outputTwin.getAssignerUserId());
            assertNotEquals(contextTwin.getAssignerUserId(), outputTwin.getAssignerUserId());
        }

        @Test
        void fill_withNullCreatedBy_setsNull() throws ServiceException {
            var contextTwin = new TwinEntity(); // createdBy null
            var factoryItem = buildFactoryItem(contextTwin);

            filler.fill(new Properties(), List.of(factoryItem), null, false);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            assertNull(outputTwin.getAssignerUser());
            assertNull(outputTwin.getAssignerUserId());
        }
    }
}
