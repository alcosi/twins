package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.enums.factory.FactoryLauncher;
import org.twins.core.featurer.factory.conditioner.ConditionerContextFactoryLauncher;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionerContextFactoryLauncherTest extends BaseUnitTest {

    @Mock
    private FactoryContext factoryContext;

    private ConditionerContextFactoryLauncher conditioner;

    @BeforeEach
    void setUp() {
        conditioner = new ConditionerContextFactoryLauncher();
    }

    private Properties buildProperties(String launcherType) {
        var props = new Properties();
        props.put("factoryLauncherType", launcherType);
        return props;
    }

    private FactoryItem buildItem(FactoryLauncher launcher) {
        var item = mock(FactoryItem.class);
        when(item.getFactoryContext()).thenReturn(factoryContext);
        when(factoryContext.getFactoryLauncher()).thenReturn(launcher);
        return item;
    }

    @Nested
    class Check {

        @Test
        void check_launcherMatches_returnsTrue() throws ServiceException {
            var props = buildProperties("transition");

            assertTrue(conditioner.check(props, buildItem(FactoryLauncher.transition)));
        }

        @Test
        void check_launcherDiffers_returnsFalse() throws ServiceException {
            var props = buildProperties("transition");

            assertFalse(conditioner.check(props, buildItem(FactoryLauncher.factoryPipeline)));
        }

        @Test
        void check_paramCaseInsensitive_returnsTrue() throws ServiceException {
            // contract: comparison is case-insensitive (equalsIgnoreCase)
            var props = buildProperties("TRANSITION");

            assertTrue(conditioner.check(props, buildItem(FactoryLauncher.transition)));
        }

        @Test
        void check_paramMixedCase_returnsTrue() throws ServiceException {
            var props = buildProperties("OnTwinCreate");

            assertTrue(conditioner.check(props, buildItem(FactoryLauncher.onTwinCreate)));
        }
    }
}
