package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.factory.conditioner.ConditionerContextTwinOfClass;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionerContextTwinOfClassTest extends BaseUnitTest {

    private ConditionerContextTwinOfClass conditioner;

    @BeforeEach
    void setUp() {
        conditioner = new ConditionerContextTwinOfClass();
    }

    private Properties buildProperties(UUID classId) {
        var props = new Properties();
        props.put("ofTwinClassId", classId.toString());
        return props;
    }

    private FactoryItem buildItemWithSingleContext(UUID contextTwinClassId) throws ServiceException {
        var contextTwin = mock(TwinEntity.class);
        when(contextTwin.getTwinClassId()).thenReturn(contextTwinClassId);
        var contextItem = mock(FactoryItem.class);
        when(contextItem.getTwin()).thenReturn(contextTwin);
        var factoryItem = mock(FactoryItem.class);
        when(factoryItem.checkNotMultiplyContextItem()).thenReturn(contextItem);
        return factoryItem;
    }

    private FactoryItem buildItemWithEmptyContext() throws ServiceException {
        var factoryItem = mock(FactoryItem.class);
        when(factoryItem.checkNotMultiplyContextItem()).thenReturn(null);
        return factoryItem;
    }

    @Nested
    class Check {

        @Test
        void check_immediateContextTwinMatchesClass_returnsTrue() throws ServiceException {
            var targetClassId = UUID.randomUUID();

            assertTrue(conditioner.check(
                    buildProperties(targetClassId),
                    buildItemWithSingleContext(targetClassId)));
        }

        @Test
        void check_immediateContextTwinDiffers_returnsFalse() throws ServiceException {
            var targetClassId = UUID.randomUUID();

            assertFalse(conditioner.check(
                    buildProperties(targetClassId),
                    buildItemWithSingleContext(UUID.randomUUID())));
        }

        @Test
        void check_noContextItem_returnsFalse() throws ServiceException {
            var targetClassId = UUID.randomUUID();

            assertFalse(conditioner.check(
                    buildProperties(targetClassId),
                    buildItemWithEmptyContext()));
        }

        @Test
        void check_doesNotRecurseBeyondImmediateContext() throws ServiceException {
            // contract: non-deep variant checks only the immediate single context twin (depth 1).
            // Counter starts at 1, so after a mismatch on the immediate context the recursion
            // counter is exhausted and the deeper context is never inspected.
            var targetClassId = UUID.randomUUID();
            var immediateTwin = mock(TwinEntity.class);
            when(immediateTwin.getTwinClassId()).thenReturn(UUID.randomUUID());
            var immediateContext = mock(FactoryItem.class);
            when(immediateContext.getTwin()).thenReturn(immediateTwin);
            var factoryItem = mock(FactoryItem.class);
            when(factoryItem.checkNotMultiplyContextItem()).thenReturn(immediateContext);

            assertFalse(conditioner.check(buildProperties(targetClassId), factoryItem));
        }
    }
}
