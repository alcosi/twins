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
import org.twins.core.featurer.factory.filler.FillerBasicsAssigneeFromContextTwinAssignee;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FillerBasicsAssigneeFromContextTwinAssigneeTest extends BaseUnitTest {

    private FillerBasicsAssigneeFromContextTwinAssignee filler;

    @BeforeEach
    void setUp() {
        filler = new FillerBasicsAssigneeFromContextTwinAssignee();
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
        void fill_copiesContextTwinAssigneeToOutput() throws ServiceException {
            var assigneeId = UUID.randomUUID();
            var assignee = new UserEntity().setId(assigneeId);
            var contextTwin = new TwinEntity()
                    .setAssignerUser(assignee)
                    .setAssignerUserId(assigneeId);
            var factoryItem = buildFactoryItem(contextTwin);

            filler.fill(new Properties(), factoryItem, null);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            // NAME promises: assignee copied FROM the context twin's assignee (assigner).
            assertSame(assignee, outputTwin.getAssignerUser());
            assertEquals(assigneeId, outputTwin.getAssignerUserId());
        }

        @Test
        void fill_withNullContextAssignee_setsNull() throws ServiceException {
            var contextTwin = new TwinEntity(); // assigner user/id both null
            var factoryItem = buildFactoryItem(contextTwin);

            filler.fill(new Properties(), factoryItem, null);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            assertNull(outputTwin.getAssignerUser());
            assertNull(outputTwin.getAssignerUserId());
        }
    }
}
