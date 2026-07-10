package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemTwinIsInFactoryInputList;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionerFactoryItemTwinIsInFactoryInputListTest extends BaseUnitTest {

    private ConditionerFactoryItemTwinIsInFactoryInputList conditioner;

    @BeforeEach
    void setUp() {
        conditioner = new ConditionerFactoryItemTwinIsInFactoryInputList();
    }

    private FactoryItem item(UUID outputTwinId, TwinEntity... inputTwins) throws Exception {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity().setId(outputTwinId));
        var ctx = mock(FactoryContext.class);
        when(ctx.getInputTwinList()).thenReturn(Arrays.asList(inputTwins));
        return new FactoryItem().setOutput(output).setFactoryContext(ctx);
    }

    private TwinEntity input(UUID id) {
        return new TwinEntity().setId(id);
    }

    @Nested
    class Check {

        @Test
        void check_outputTwinInInputList_returnsTrue() throws Exception {
            // contract: matches when the output twin is one of the original factory-input twins.
            var outputId = UUID.randomUUID();

            assertTrue(conditioner.check(new Properties(),
                    item(outputId, input(UUID.randomUUID()), input(outputId))));
        }

        @Test
        void check_outputTwinNotInInputList_returnsFalse() throws Exception {
            var outputId = UUID.randomUUID();

            assertFalse(conditioner.check(new Properties(),
                    item(outputId, input(UUID.randomUUID()), input(UUID.randomUUID()))));
        }

        @Test
        void check_emptyInputList_returnsFalse() throws Exception {
            assertFalse(conditioner.check(new Properties(), item(UUID.randomUUID())));
        }

        @Test
        void check_onlyOutputIdInList_returnsTrue() throws Exception {
            var outputId = UUID.randomUUID();

            assertTrue(conditioner.check(new Properties(), item(outputId, input(outputId))));
        }

        @Test
        void check_singletonInputListMatch_returnsTrue() throws Exception {
            var outputId = UUID.randomUUID();

            assertTrue(conditioner.check(new Properties(),
                    item(outputId, input(outputId))));
            // sanity: also negative singleton
            assertFalse(conditioner.check(new Properties(),
                    item(outputId, input(UUID.randomUUID()))));
        }
    }
}
