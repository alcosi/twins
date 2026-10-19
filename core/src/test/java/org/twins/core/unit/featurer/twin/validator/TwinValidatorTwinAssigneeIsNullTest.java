package org.twins.core.featurer.twin.validator;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class TwinValidatorTwinAssigneeIsNullTest extends BaseUnitTest {

    private final TwinValidatorTwinAssigneeIsNull validator = new TwinValidatorTwinAssigneeIsNull();

    @Nested
    class IsValid {

        @Test
        void isValid_assigneeNull_returnsValid() throws ServiceException {
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());

            var result = validator.isValid(new Properties(), List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_assigneeSet_returnsInvalid() throws ServiceException {
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            twin.setAssignerUserId(UUID.randomUUID());

            var result = validator.isValid(new Properties(), List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_assigneeNull_inverted_returnsInvalid() throws ServiceException {
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());

            var result = validator.isValid(new Properties(), List.of(twin), true);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_assigneeSet_inverted_returnsValid() throws ServiceException {
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            twin.setAssignerUserId(UUID.randomUUID());

            var result = validator.isValid(new Properties(), List.of(twin), true);

            assertTrue(result.getTwinsResults().get(twin.getId()).isValid());
        }
    }
}
