package org.twins.core.featurer.twin.validator;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.service.twin.TwinSearchServiceV2;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class TwinValidatorTwinHasChildrenInStatusesTest extends BaseUnitTest {

    @Mock
    private TwinSearchServiceV2 twinSearchService;

    private TwinValidatorTwinHasChildrenInStatuses validator;

    @BeforeEach
    void setUp() {
        validator = new TwinValidatorTwinHasChildrenInStatuses(twinSearchService);
    }

    @Nested
    class IsValid {

        @Test
        void isValid_hasChildrenInStatuses_returnsValid() throws ServiceException {
            var statusId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(twinId);

            when(twinSearchService.countByGroupFields(any(BasicSearch.class), eq(TwinEntity.BasicField.HEAD_TWIN_ID)))
                    .thenReturn(Map.of(twinId, 2L));

            var props = new Properties();
            props.put("statusIds", statusId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twinId).isValid());
        }

        @Test
        void isValid_noChildrenInStatuses_returnsInvalid() throws ServiceException {
            var statusId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(twinId);

            when(twinSearchService.countByGroupFields(any(BasicSearch.class), eq(TwinEntity.BasicField.HEAD_TWIN_ID)))
                    .thenReturn(Collections.emptyMap());

            var props = new Properties();
            props.put("statusIds", statusId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twinId).isValid());
        }

        @Test
        void isValid_zeroChildrenInStatuses_returnsInvalid() throws ServiceException {
            var statusId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(twinId);

            when(twinSearchService.countByGroupFields(any(BasicSearch.class), eq(TwinEntity.BasicField.HEAD_TWIN_ID)))
                    .thenReturn(Map.of(twinId, 0L));

            var props = new Properties();
            props.put("statusIds", statusId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twinId).isValid());
        }

        @Test
        void isValid_hasChildrenInStatuses_inverted_returnsInvalid() throws ServiceException {
            var statusId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(twinId);

            when(twinSearchService.countByGroupFields(any(BasicSearch.class), eq(TwinEntity.BasicField.HEAD_TWIN_ID)))
                    .thenReturn(Map.of(twinId, 2L));

            var props = new Properties();
            props.put("statusIds", statusId.toString());

            var result = validator.isValid(props, List.of(twin), true);

            assertFalse(result.getTwinsResults().get(twinId).isValid());
        }
    }
}
