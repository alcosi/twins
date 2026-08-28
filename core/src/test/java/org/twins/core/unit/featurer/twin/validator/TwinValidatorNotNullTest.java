package org.twins.core.featurer.twin.validator;

import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;


class TwinValidatorNotNullTest extends BaseUnitTest {

    private final TwinValidatorNotNull validator = new TwinValidatorNotNull();

    @Nested
    class IsValidSingle {

        @Test
        void isValid_twinNotNull_returnsValid() throws ServiceException {
            var twin = new TwinEntity();

            ValidationResult result = validator.isValid(new HashMap<>(), twin, false);

            assertTrue(result.isValid());
        }

        @Test
        void isValid_twinNull_returnsInvalid() throws ServiceException {
            ValidationResult result = validator.isValid(new HashMap<>(), (TwinEntity) null, false);

            assertFalse(result.isValid());
        }

        @Test
        void isValid_twinNotNull_inverted_returnsInvalid() throws ServiceException {
            var twin = new TwinEntity();

            ValidationResult result = validator.isValid(new HashMap<>(), twin, true);

            assertFalse(result.isValid());
        }

        @Test
        void isValid_twinNull_inverted_returnsValid() throws ServiceException {
            ValidationResult result = validator.isValid(new HashMap<>(), (TwinEntity) null, true);

            assertTrue(result.isValid());
        }
    }

    @Nested
    class Nullable {

        @Test
        void nullable_returnsTrue() {
            assertTrue(validator.nullable());
        }
    }
}
