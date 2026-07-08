package org.twins.core.featurer.notificator.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class ContextCollectorHeadTwinTest extends BaseUnitTest {

    private final ContextCollectorHeadTwin collector = new ContextCollectorHeadTwin();

    @Mock
    private TwinService twinService;

    private UUID headTwinId;
    private UUID twinId;
    private TwinEntity twin;
    private TwinEntity headTwin;
    private HistoryEntity history;

    @BeforeEach
    void setUp() throws Exception {
        headTwinId = UUID.randomUUID();
        twinId = UUID.randomUUID();
        headTwin = new TwinEntity();
        headTwin.setId(headTwinId);
        headTwin.setName("HeadTwin");
        headTwin.setDescription("Head description");

        twin = new TwinEntity();
        twin.setId(twinId);
        twin.setHeadTwinId(headTwinId);

        history = new HistoryEntity();
        history.setTwin(twin);

        setField(collector, "twinService", twinService);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try { return clazz.getDeclaredField(fieldName); }
            catch (NoSuchFieldException e) { clazz = clazz.getSuperclass(); }
        }
        throw new RuntimeException("Field not found: " + fieldName);
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
    class HeadTwinFromEntity {

        @Test
        void collectData_headTwinLoadedOnEntity_usesEntityHeadTwin() throws Exception {
            twin.setHeadTwin(headTwin);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(true, true, false, false));

            assertEquals(headTwinId.toString(), result.get("TWIN_ID"));
            assertEquals("HeadTwin", result.get("TWIN_NAME"));
            verifyNoInteractions(twinService);
        }
    }

    @Nested
    class HeadTwinFromService {

        @Test
        void collectData_headTwinNullOnEntity_fetchesFromService() throws Exception {
            when(twinService.findEntitySafe(headTwinId)).thenReturn(headTwin);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(true, true, false, false));

            assertEquals(headTwinId.toString(), result.get("TWIN_ID"));
            assertEquals("HeadTwin", result.get("TWIN_NAME"));
            verify(twinService).findEntitySafe(headTwinId);
        }
    }

    @Nested
    class CollectAllFields {

        @Test
        void collectData_allEnabled_collectsFromHeadTwin() throws Exception {
            var businessAccountId = UUID.randomUUID();
            headTwin.setOwnerBusinessAccountId(businessAccountId);
            twin.setHeadTwin(headTwin);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(true, true, true, true));

            assertEquals(4, result.size());
            assertEquals(headTwinId.toString(), result.get("TWIN_ID"));
            assertEquals("HeadTwin", result.get("TWIN_NAME"));
            assertEquals("Head description", result.get("TWIN_DESCRIPTION"));
            assertEquals(businessAccountId.toString(), result.get("TWIN_OWNER_BUSINESS_ACCOUNT_ID"));
        }
    }

    @Nested
    class NullHandling {

        @Test
        void collectData_nullHeadTwinId_throwsNullPointerException() {
            twin.setHeadTwinId(null);
            var context = new HashMap<String, String>();

            assertThrows(NullPointerException.class,
                    () -> collector.collectData(history, context, props(true, true, true, true)));
        }

        @Test
        void collectData_nullTwin_throwsNullPointerException() {
            history.setTwin(null);
            var context = new HashMap<String, String>();

            assertThrows(NullPointerException.class,
                    () -> collector.collectData(history, context, props(true, true, true, true)));
        }
    }
}
