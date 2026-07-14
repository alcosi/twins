package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerHeadFromTemplateTwinHead;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FillerHeadFromTemplateTwinHeadTest extends BaseUnitTest {

    private final FillerHeadFromTemplateTwinHead filler = new FillerHeadFromTemplateTwinHead();

    private FactoryItem buildFactoryItem() {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        return new FactoryItem().setOutput(output);
    }

    @Nested
    class Fill {

        @Test
        void fill_templateWithHead_setsOutputHeadFromTemplateHead() throws ServiceException {
            // NAME promises: output twin head = template twin's head.
            var headId = UUID.randomUUID();
            var headTwin = new TwinEntity().setId(headId);
            var templateTwin = new TwinEntity().setHeadTwin(headTwin).setHeadTwinId(headId);
            var factoryItem = buildFactoryItem();

            filler.fill(new Properties(), List.of(factoryItem), templateTwin, false);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            assertSame(headTwin, outputTwin.getHeadTwin());
            assertEquals(headId, outputTwin.getHeadTwinId());
        }

        @Test
        void fill_nullTemplate_throwsStepError() {
            var factoryItem = buildFactoryItem();

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(new Properties(), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_templateWithoutHead_throwsStepError() {
            var templateTwin = new TwinEntity(); // headTwinId == null
            var factoryItem = buildFactoryItem();

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(new Properties(), List.of(factoryItem), templateTwin, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }
    }
}
