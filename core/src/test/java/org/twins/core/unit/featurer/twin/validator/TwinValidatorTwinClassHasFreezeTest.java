package org.twins.core.featurer.twin.validator;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class TwinValidatorTwinClassHasFreezeTest extends BaseUnitTest {

    private final TwinValidatorTwinClassHasFreeze validator = new TwinValidatorTwinClassHasFreeze();

    private TwinEntity twinWithFreeze(UUID freezeId) {
        var twinClass = new TwinClassEntity();
        twinClass.setTwinClassFreezeId(freezeId);
        var twin = new TwinEntity();
        twin.setId(UUID.randomUUID());
        twin.setTwinClass(twinClass);
        return twin;
    }

    @Nested
    class IsValid {

        @Test
        void isValid_freezeInSet_returnsValid() throws ServiceException {
            var freezeId = UUID.randomUUID();
            var twin = twinWithFreeze(freezeId);

            var props = new Properties();
            props.put("freezeIds", freezeId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_freezeNotInSet_returnsInvalid() throws ServiceException {
            var twin = twinWithFreeze(UUID.randomUUID());

            var props = new Properties();
            props.put("freezeIds", UUID.randomUUID().toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_noFreeze_returnsInvalid() throws ServiceException {
            var twinClass = new TwinClassEntity();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            twin.setTwinClass(twinClass);

            var props = new Properties();
            props.put("freezeIds", UUID.randomUUID().toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_emptyFreezeSet_twinHasFreeze_returnsValid() throws ServiceException {
            var freezeId = UUID.randomUUID();
            var twin = twinWithFreeze(freezeId);

            var props = new Properties();

            var result = validator.isValid(props, List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_emptyFreezeSet_noFreeze_returnsInvalid() throws ServiceException {
            var twinClass = new TwinClassEntity();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            twin.setTwinClass(twinClass);

            var props = new Properties();

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_freezeInSet_inverted_returnsInvalid() throws ServiceException {
            var freezeId = UUID.randomUUID();
            var twin = twinWithFreeze(freezeId);

            var props = new Properties();
            props.put("freezeIds", freezeId.toString());

            var result = validator.isValid(props, List.of(twin), true);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }
    }
}
