package org.twins.core.featurer.twin.validator;

import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;

import static org.junit.jupiter.api.Assertions.*;


class TwinValidatorTest extends BaseUnitTest {

    @Nested
    class BuildResult {

        private final TwinValidatorNotNull validator = new TwinValidatorNotNull();

        @Test
        void buildResult_validNotInverted_returnsValidWithEmptyMessage() {
            var result = validator.buildResult(true, false, "invalid", "inverted");

            assertTrue(result.isValid());
            assertEquals("", result.getMessage());
        }

        @Test
        void buildResult_invalidNotInverted_returnsInvalidWithMessage() {
            var result = validator.buildResult(false, false, "invalid", "inverted");

            assertFalse(result.isValid());
            assertEquals("invalid", result.getMessage());
        }

        @Test
        void buildResult_validInverted_returnsInvalidWithInvertedMessage() {
            var result = validator.buildResult(true, true, "invalid", "inverted");

            assertFalse(result.isValid());
            assertEquals("inverted", result.getMessage());
        }

        @Test
        void buildResult_invalidInverted_returnsValidWithEmptyMessage() {
            var result = validator.buildResult(false, true, "invalid", "inverted");

            assertTrue(result.isValid());
            assertEquals("", result.getMessage());
        }
    }
}
