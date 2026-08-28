package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemTwinOfClass;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionerFactoryItemTwinOfClassTest extends BaseUnitTest {

    private ConditionerFactoryItemTwinOfClass conditioner;

    @BeforeEach
    void setUp() {
        conditioner = new ConditionerFactoryItemTwinOfClass();
    }

    private Properties props(UUID classId) {
        var p = new Properties();
        p.put("ofTwinClassId", classId.toString());
        return p;
    }

    private FactoryItem item(UUID twinClassId) {
        var twin = new TwinEntity().setTwinClassId(twinClassId);
        var output = new TwinCreate();
        output.setTwinEntity(twin);
        return new FactoryItem().setOutput(output);
    }

    @Nested
    class Check {

        @Test
        void check_outputTwinClassEqualsConfigured_returnsTrue() throws ServiceException {
            // contract: matches when the OUTPUT twin's class id equals the configured class id.
            var classId = UUID.randomUUID();

            assertTrue(conditioner.check(props(classId), item(classId)));
        }

        @Test
        void check_outputTwinClassDiffers_returnsFalse() throws ServiceException {
            var classId = UUID.randomUUID();

            assertFalse(conditioner.check(props(classId), item(UUID.randomUUID())));
        }
    }
}
