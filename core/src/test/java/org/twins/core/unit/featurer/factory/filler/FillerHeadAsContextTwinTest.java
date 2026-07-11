package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerHeadAsContextTwin;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FillerHeadAsContextTwinTest extends BaseUnitTest {

    private final FillerHeadAsContextTwin filler = new FillerHeadAsContextTwin();

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
        void fill_singleContext_setsOutputHeadToContextTwin() throws ServiceException {
            // NAME promises: output twin head = the single context twin (id + entity).
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = buildFactoryItem(contextTwin);

            filler.fill(new Properties(), factoryItem, null);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            assertSame(contextTwin, outputTwin.getHeadTwin());
            assertEquals(contextTwin.getId(), outputTwin.getHeadTwinId());
        }

        @Test
        void fill_noContext_throwsStepError() {
            // No context twin -> checkSingleContextTwin throws FACTORY_PIPELINE_STEP_ERROR.
            var output = new TwinCreate();
            output.setTwinEntity(new TwinEntity());
            var factoryItem = new FactoryItem().setOutput(output);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(new Properties(), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_multipleContexts_throwsStepError() {
            var output = new TwinCreate();
            output.setTwinEntity(new TwinEntity());
            var contextOutput = new TwinCreate();
            contextOutput.setTwinEntity(new TwinEntity());
            var factoryItem = new FactoryItem()
                    .setOutput(output)
                    .setContextFactoryItemList(List.of(
                            new FactoryItem().setOutput(contextOutput),
                            new FactoryItem().setOutput(new TwinCreate())));

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(new Properties(), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }
    }

    @Test
    void canBeOptional_returnsFalse() {
        // NAME has no "optional" wording and head is mandatory; canBeOptional must be false.
        assertFalse(filler.canBeOptional());
    }
}
