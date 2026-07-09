package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinOperation;
import org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemTwinOperationLauncher;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionerFactoryItemTwinOperationLauncherTest extends BaseUnitTest {

    private ConditionerFactoryItemTwinOperationLauncher conditioner;

    @BeforeEach
    void setUp() {
        conditioner = new ConditionerFactoryItemTwinOperationLauncher();
    }

    private FactoryItem item(TwinOperation.Launcher launcher) {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        output.setLauncher(launcher);
        return new FactoryItem().setOutput(output);
    }

    private Properties props(String launcherValue) {
        var p = new Properties();
        if (launcherValue != null) {
            p.put("twinOperationLauncher", launcherValue);
        }
        return p;
    }

    @Nested
    class Check {

        @Test
        void check_launcherMatches_returnsTrue() throws ServiceException {
            // contract: matches when the extracted launcher equals the output twin's launcher.
            assertTrue(conditioner.check(props("direct"), item(TwinOperation.Launcher.direct)));
        }

        @Test
        void check_launcherDiffers_returnsFalse() throws ServiceException {
            // output defaults to factory; configured = direct -> mismatch.
            assertFalse(conditioner.check(props("direct"), item(TwinOperation.Launcher.factory)));
        }

        @Test
        void check_paramAbsent_defaultsToDirect_matchesDirectOutput() throws ServiceException {
            // contract: a missing param defaults to Launcher.direct -> matches a direct-launched output.
            assertTrue(conditioner.check(props(null), item(TwinOperation.Launcher.direct)));
        }
    }
}
