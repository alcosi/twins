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


class ContextCollectorTwinTest extends BaseUnitTest {

    private final ContextCollectorTwin collector = new ContextCollectorTwin();

    private UUID twinId;
    private UUID businessAccountId;
    private TwinEntity twin;
    private HistoryEntity history;

    @BeforeEach
    void setUp() {
        twinId = UUID.randomUUID();
        businessAccountId = UUID.randomUUID();
        twin = new TwinEntity();
        twin.setId(twinId);
        twin.setName("TestTwin");
        twin.setDescription("Test description");
        twin.setOwnerBusinessAccountId(businessAccountId);
        history = new HistoryEntity();
        history.setTwin(twin);
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
        void collectData_collectIdTrue_putsTwinId() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(true, false, false, false));

            assertEquals(twinId.toString(), result.get("TWIN_ID"));
        }

        @Test
        void collectData_collectIdFalse_skipsTwinId() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, false, false));

            assertNull(result.get("TWIN_ID"));
        }

        @Test
        void collectData_customIdKey_usedWhenCollecting() throws Exception {
            var props = props(true, false, false, false);
            props.put("collectIdKey", "MY_ID");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props);

            assertEquals(twinId.toString(), result.get("MY_ID"));
            assertNull(result.get("TWIN_ID"));
        }
    }

    @Nested
    class CollectName {

        @Test
        void collectData_collectNameTrue_putsTwinName() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, true, false, false));

            assertEquals("TestTwin", result.get("TWIN_NAME"));
        }

        @Test
        void collectData_collectNameFalse_skipsTwinName() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, false, false));

            assertNull(result.get("TWIN_NAME"));
        }

        @Test
        void collectData_customNameKey_usedWhenCollecting() throws Exception {
            var props = props(false, true, false, false);
            props.put("collectNameKey", "MY_NAME");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props);

            assertEquals("TestTwin", result.get("MY_NAME"));
            assertNull(result.get("TWIN_NAME"));
        }
    }

    @Nested
    class CollectDescription {

        @Test
        void collectData_collectDescriptionTrue_putsDescription() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, true, false));

            assertEquals("Test description", result.get("TWIN_DESCRIPTION"));
        }

        @Test
        void collectData_collectDescriptionFalse_skipsDescription() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, false, false));

            assertNull(result.get("TWIN_DESCRIPTION"));
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

        @Test
        void collectData_collectBusinessAccountFalse_skipsBusinessAccountId() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, false, false));

            assertNull(result.get("TWIN_OWNER_BUSINESS_ACCOUNT_ID"));
        }

        @Test
        void collectData_collectBusinessAccountTrue_nullBusinessAccountId_throwsNullPointerException() {
            twin.setOwnerBusinessAccountId(null);
            var context = new HashMap<String, String>();

            assertThrows(NullPointerException.class,
                    () -> collector.collectData(history, context, props(false, false, false, true)));
        }
    }

    @Nested
    class CollectMultiple {

        @Test
        void collectData_allEnabled_collectsAllFields() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(true, true, true, true));

            assertEquals(4, result.size());
            assertEquals(twinId.toString(), result.get("TWIN_ID"));
            assertEquals("TestTwin", result.get("TWIN_NAME"));
            assertEquals("Test description", result.get("TWIN_DESCRIPTION"));
            assertEquals(businessAccountId.toString(), result.get("TWIN_OWNER_BUSINESS_ACCOUNT_ID"));
        }

        @Test
        void collectData_noneEnabled_returnsEmptyContext() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, false, false));

            assertTrue(result.isEmpty());
        }

        @Test
        void collectData_preservesExistingContext() throws Exception {
            var context = new HashMap<String, String>();
            context.put("EXISTING_KEY", "existing_value");

            var result = collector.collectData(history, context, props(true, false, false, false));

            assertEquals("existing_value", result.get("EXISTING_KEY"));
            assertEquals(twinId.toString(), result.get("TWIN_ID"));
        }
    }
}
