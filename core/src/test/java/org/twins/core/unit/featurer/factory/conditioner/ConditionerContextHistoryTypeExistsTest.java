package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.enums.history.HistoryType;
import org.twins.core.featurer.factory.conditioner.ConditionerContextHistoryTypeExists;
import org.twins.core.service.history.HistoryService;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionerContextHistoryTypeExistsTest extends BaseUnitTest {

    @Mock
    private HistoryService historyService;

    private ConditionerContextHistoryTypeExists conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerContextHistoryTypeExists(historyService);
    }

    private Properties buildProperties(String historyType) {
        var props = new Properties();
        props.put("historyType", historyType);
        return props;
    }

    private FactoryItem buildItem(UUID requestId) {
        var item = mock(FactoryItem.class);
        var ctx = mock(FactoryContext.class);
        when(item.getFactoryContext()).thenReturn(ctx);
        when(ctx.getRequestId()).thenReturn(requestId);
        return item;
    }

    @Nested
    class Check {

        @Test
        void check_historyExists_returnsTrue() throws ServiceException {
            var requestId = UUID.randomUUID();
            when(historyService.existsByHistoryBatchIdAndHistoryType(eq(requestId), eq(HistoryType.twinCreated)))
                    .thenReturn(true);

            assertTrue(conditioner.check(buildProperties("twinCreated"), buildItem(requestId)));
        }

        @Test
        void check_historyDoesNotExist_returnsFalse() throws ServiceException {
            var requestId = UUID.randomUUID();
            when(historyService.existsByHistoryBatchIdAndHistoryType(eq(requestId), eq(HistoryType.twinCreated)))
                    .thenReturn(false);

            assertFalse(conditioner.check(buildProperties("twinCreated"), buildItem(requestId)));
        }

        @Test
        void check_usesRequestIdFromFactoryContext() throws ServiceException {
            // contract: existence is checked against the context's requestId (history batch id)
            var requestId = UUID.randomUUID();
            when(historyService.existsByHistoryBatchIdAndHistoryType(eq(requestId), eq(HistoryType.statusChanged)))
                    .thenReturn(true);

            conditioner.check(buildProperties("statusChanged"), buildItem(requestId));

            org.mockito.Mockito.verify(historyService)
                    .existsByHistoryBatchIdAndHistoryType(eq(requestId), eq(HistoryType.statusChanged));
        }
    }
}
