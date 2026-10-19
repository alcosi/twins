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
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerBasicsAssigneeFromContextTwinOfClassAssignee;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FillerBasicsAssigneeFromContextTwinOfClassAssigneeTest extends BaseUnitTest {

    private FillerBasicsAssigneeFromContextTwinOfClassAssignee filler;

    private static final UUID TWIN_CLASS_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        filler = new FillerBasicsAssigneeFromContextTwinOfClassAssignee();
    }

    private Properties props() {
        var p = new Properties();
        p.setProperty("twinClassId", TWIN_CLASS_ID.toString());
        return p;
    }

    // the filler looks the twin of the configured class up in the item's context — build a real
    // context chain instead of stubbing the lookup
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
        void fill_foundTwinOfClass_setsItsAssignee() throws ServiceException {
            var assigneeId = UUID.randomUUID();
            var assignee = new UserEntity().setId(assigneeId);
            var contextTwin = new TwinEntity()
                    .setTwinClassId(TWIN_CLASS_ID)
                    .setAssignerUser(assignee)
                    .setAssignerUserId(assigneeId);
            var factoryItem = buildFactoryItem(contextTwin);

            filler.fill(props(), factoryItem, null);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            // NAME promises: assignee FROM the context twin of the given class.
            assertSame(assignee, outputTwin.getAssignerUser());
            assertEquals(assigneeId, outputTwin.getAssignerUserId());
        }

        @Test
        void fill_noTwinOfClassFound_throwsStepError() {
            var wrongClassTwin = new TwinEntity().setTwinClassId(UUID.randomUUID());
            var factoryItem = buildFactoryItem(wrongClassTwin);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_foundTwinWithNullAssignee_setsNull() throws ServiceException {
            var contextTwin = new TwinEntity().setTwinClassId(TWIN_CLASS_ID); // assignee null
            var factoryItem = buildFactoryItem(contextTwin);

            filler.fill(props(), factoryItem, null);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            assertNull(outputTwin.getAssignerUser());
            assertNull(outputTwin.getAssignerUserId());
        }
    }
}
