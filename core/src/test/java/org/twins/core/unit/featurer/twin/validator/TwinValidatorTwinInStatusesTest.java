package org.twins.core.featurer.twin.validator;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.twin.TwinService;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class TwinValidatorTwinInStatusesTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private TwinValidatorTwinInStatuses validator;

    @BeforeEach
    void setUp() {
        validator = new TwinValidatorTwinInStatuses(twinService);
    }

    @Nested
    class IsValid {

        @Test
        void isValid_statusInSet_returnsValid() throws ServiceException {
            var statusId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            twin.setTwinStatusId(statusId);

            when(twinService.getTwinStatusId(twin)).thenReturn(statusId);

            var props = new Properties();
            props.put("statusIds", statusId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_statusNotInSet_returnsInvalid() throws ServiceException {
            var statusId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());

            when(twinService.getTwinStatusId(twin)).thenReturn(statusId);

            var props = new Properties();
            props.put("statusIds", UUID.randomUUID().toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_statusInSet_inverted_returnsInvalid() throws ServiceException {
            var statusId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());

            when(twinService.getTwinStatusId(twin)).thenReturn(statusId);

            var props = new Properties();
            props.put("statusIds", statusId.toString());

            var result = validator.isValid(props, List.of(twin), true);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_multipleStatuses_oneMatches_returnsValid() throws ServiceException {
            var statusId1 = UUID.randomUUID();
            var statusId2 = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());

            when(twinService.getTwinStatusId(twin)).thenReturn(statusId2);

            var props = new Properties();
            props.put("statusIds", statusId1 + ", " + statusId2);

            var result = validator.isValid(props, List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twin.getId()).isValid());
        }
    }
}
