package org.twins.core.featurer.notificator.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.TwinEntity;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class ContextCollectorTwinBaseTest extends BaseUnitTest {

    /**
     * Minimal concrete subclass to test ContextCollectorTwinBase directly.
     */
    private static class TestableTwinBaseCollector extends ContextCollectorTwinBase {
        private final TwinEntity resolvedTwin;

        TestableTwinBaseCollector(TwinEntity resolvedTwin) {
            this.resolvedTwin = resolvedTwin;
        }

        @Override
        protected TwinEntity resolveTwin(HistoryEntity history) {
            return resolvedTwin;
        }
    }

    private UUID twinId;
    private UUID businessAccountId;
    private TwinEntity twin;
    private TestableTwinBaseCollector collector;
    private HistoryEntity history;

    @BeforeEach
    void setUp() {
        twinId = UUID.randomUUID();
        businessAccountId = UUID.randomUUID();

        twin = new TwinEntity();
        twin.setId(twinId);
        twin.setName("TestTwin");
        twin.setDescription("A test twin");
        twin.setOwnerBusinessAccountId(businessAccountId);

        collector = new TestableTwinBaseCollector(twin);
        history = new HistoryEntity();
    }

    private Properties props(boolean collectId, boolean collectName, boolean collectDescription, boolean collectBusinessAccount) {
        var props = new Properties();
        props.put("collectId", String.valueOf(collectId));
        props.put("collectIdKey", "TWIN_ID");
        props.put("collectName", String.valueOf(collectName));
        props.put("collectNameKey", "TWIN_NAME");
        props.put("collectDescription", String.valueOf(collectDescription));
        props.put("collectDescriptionKey", "TWIN_DESCRIPTION");
        props.put("collectBusinessAccount", String.valueOf(collectBusinessAccount));
        props.put("collectBusinessAccountKey", "TWIN_OWNER_BUSINESS_ACCOUNT_ID");
        return props;
    }

    @Nested
    class CollectId {

        @Test
        void collectData_collectIdTrue_putsId() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(true, false, false, false));

            assertEquals(twinId.toString(), result.get("TWIN_ID"));
        }

        @Test
        void collectData_collectIdTrue_customKey_putsIdUnderCustomKey() throws Exception {
            var props = props(true, false, false, false);
            props.put("collectIdKey", "CUSTOM_ID");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props);

            assertEquals(twinId.toString(), result.get("CUSTOM_ID"));
            assertNull(result.get("TWIN_ID"));
        }
    }

    @Nested
    class CollectName {

        @Test
        void collectData_collectNameTrue_putsName() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, true, false, false));

            assertEquals("TestTwin", result.get("TWIN_NAME"));
        }
    }

    @Nested
    class CollectDescription {

        @Test
        void collectData_collectDescriptionTrue_putsDescription() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, true, false));

            assertEquals("A test twin", result.get("TWIN_DESCRIPTION"));
        }
    }

    @Nested
    class CollectBusinessAccount {

        @Test
        void collectData_collectBusinessAccountTrue_putsBusinessAccountId() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, false, true));

            assertEquals(businessAccountId.toString(), result.get("TWIN_OWNER_BUSINESS_ACCOUNT_ID"));
        }
    }

    @Nested
    class CollectAll {

        @Test
        void collectData_allTrue_collectsEverything() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(true, true, true, true));

            assertEquals(4, result.size());
            assertEquals(twinId.toString(), result.get("TWIN_ID"));
            assertEquals("TestTwin", result.get("TWIN_NAME"));
            assertEquals("A test twin", result.get("TWIN_DESCRIPTION"));
            assertEquals(businessAccountId.toString(), result.get("TWIN_OWNER_BUSINESS_ACCOUNT_ID"));
        }

        @Test
        void collectData_allFalse_returnsEmptyContext() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, false, false));

            assertTrue(result.isEmpty());
        }
    }
}
