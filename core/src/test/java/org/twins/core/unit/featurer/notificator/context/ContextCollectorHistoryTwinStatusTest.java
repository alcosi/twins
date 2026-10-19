package org.twins.core.featurer.notificator.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.history.context.HistoryContextStatusChange;
import org.twins.core.dao.history.context.snapshot.StatusSnapshot;

import java.util.HashMap;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;


class ContextCollectorHistoryTwinStatusTest extends BaseUnitTest {

    private final ContextCollectorHistoryTwinStatus collector = new ContextCollectorHistoryTwinStatus();

    private HistoryEntity history;
    private StatusSnapshot fromStatus;
    private StatusSnapshot toStatus;

    @BeforeEach
    void setUp() {
        fromStatus = new StatusSnapshot();
        fromStatus.setName("Draft");
        fromStatus.setBackgroundColor("#FF0000");
        fromStatus.setFontColor("#FFFFFF");

        toStatus = new StatusSnapshot();
        toStatus.setName("Published");
        toStatus.setBackgroundColor("#00FF00");
        toStatus.setFontColor("#000000");

        var statusChange = new HistoryContextStatusChange();
        statusChange.setFromStatus(fromStatus);
        statusChange.setToStatus(toStatus);

        history = new HistoryEntity();
        history.setContext(statusChange);
    }

    private Properties fullProps() {
        var props = new Properties();
        props.put("collectSrcStatus", "true");
        props.put("collectSrcStatusKey", "TWIN_SRC_STATUS_NAME");
        props.put("collectDstStatus", "true");
        props.put("collectDstStatusKey", "TWIN_DST_STATUS_NAME");
        props.put("collectSrcStatusBackgroundColor", "true");
        props.put("collectSrcStatusBackgroundColorKey", "TWIN_SRC_STATUS_BACKGROUND_COLOR");
        props.put("collectDstStatusBackgroundColor", "true");
        props.put("collectDstStatusBackgroundColorKey", "TWIN_DST_STATUS_BACKGROUND_COLOR");
        props.put("collectSrcStatusFontColor", "true");
        props.put("collectSrcStatusFontColorKey", "TWIN_SRC_STATUS_FONT_COLOR");
        props.put("collectDstStatusFontColor", "true");
        props.put("collectDstStatusFontColorKey", "TWIN_DST_STATUS_FONT_COLOR");
        return props;
    }

    private Properties emptyProps() {
        var props = new Properties();
        props.put("collectSrcStatus", "false");
        props.put("collectSrcStatusKey", "TWIN_SRC_STATUS_NAME");
        props.put("collectDstStatus", "false");
        props.put("collectDstStatusKey", "TWIN_DST_STATUS_NAME");
        props.put("collectSrcStatusBackgroundColor", "false");
        props.put("collectSrcStatusBackgroundColorKey", "TWIN_SRC_STATUS_BACKGROUND_COLOR");
        props.put("collectDstStatusBackgroundColor", "false");
        props.put("collectDstStatusBackgroundColorKey", "TWIN_DST_STATUS_BACKGROUND_COLOR");
        props.put("collectSrcStatusFontColor", "false");
        props.put("collectSrcStatusFontColorKey", "TWIN_SRC_STATUS_FONT_COLOR");
        props.put("collectDstStatusFontColor", "false");
        props.put("collectDstStatusFontColorKey", "TWIN_DST_STATUS_FONT_COLOR");
        return props;
    }

    @Nested
    class CollectSrcStatus {

        @Test
        void collectData_collectSrcStatusTrue_putsSrcStatusName() throws Exception {
            var props = emptyProps();
            props.put("collectSrcStatus", "true");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props);

            assertEquals("Draft", result.get("TWIN_SRC_STATUS_NAME"));
        }

        @Test
        void collectData_collectSrcStatusFalse_skipsSrcStatusName() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, emptyProps());

            assertNull(result.get("TWIN_SRC_STATUS_NAME"));
        }

        @Test
        void collectData_customSrcStatusKey_usedForCollection() throws Exception {
            var props = emptyProps();
            props.put("collectSrcStatus", "true");
            props.put("collectSrcStatusKey", "FROM_STATUS");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props);

            assertEquals("Draft", result.get("FROM_STATUS"));
            assertNull(result.get("TWIN_SRC_STATUS_NAME"));
        }
    }

    @Nested
    class CollectDstStatus {

        @Test
        void collectData_collectDstStatusTrue_putsDstStatusName() throws Exception {
            var props = emptyProps();
            props.put("collectDstStatus", "true");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props);

            assertEquals("Published", result.get("TWIN_DST_STATUS_NAME"));
        }

        @Test
        void collectData_collectDstStatusFalse_skipsDstStatusName() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, emptyProps());

            assertNull(result.get("TWIN_DST_STATUS_NAME"));
        }
    }

    @Nested
    class CollectSrcStatusBackgroundColor {

        @Test
        void collectData_collectSrcStatusBackgroundColorTrue_putsSrcBackgroundColor() throws Exception {
            var props = emptyProps();
            props.put("collectSrcStatusBackgroundColor", "true");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props);

            assertEquals("#FF0000", result.get("TWIN_SRC_STATUS_BACKGROUND_COLOR"));
        }

        @Test
        void collectData_collectSrcStatusBackgroundColorFalse_skipsSrcBackgroundColor() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, emptyProps());

            assertNull(result.get("TWIN_SRC_STATUS_BACKGROUND_COLOR"));
        }
    }

    @Nested
    class CollectSrcStatusFontColor {

        @Test
        void collectData_collectSrcStatusFontColorTrue_putsSrcFontColor() throws Exception {
            var props = emptyProps();
            props.put("collectSrcStatusFontColor", "true");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props);

            assertEquals("#FFFFFF", result.get("TWIN_SRC_STATUS_FONT_COLOR"));
        }
    }

    @Nested
    class CollectDstStatusBackgroundColor {

        @Test
        void collectData_collectDstStatusBackgroundColorTrue_putsDstBackgroundColor() throws Exception {
            var props = emptyProps();
            props.put("collectDstStatusBackgroundColor", "true");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props);

            assertEquals("#00FF00", result.get("TWIN_DST_STATUS_BACKGROUND_COLOR"));
        }
    }

    @Nested
    class CollectDstStatusFontColor {

        @Test
        void collectData_collectDstStatusFontColorTrue_putsDstFontColor() throws Exception {
            var props = emptyProps();
            props.put("collectDstStatusFontColor", "true");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props);

            assertEquals("#000000", result.get("TWIN_DST_STATUS_FONT_COLOR"));
        }
    }

    @Nested
    class CollectAll {

        @Test
        void collectData_allEnabled_collectsAllFields() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, fullProps());

            assertEquals(6, result.size());
            assertEquals("Draft", result.get("TWIN_SRC_STATUS_NAME"));
            assertEquals("Published", result.get("TWIN_DST_STATUS_NAME"));
            assertEquals("#FF0000", result.get("TWIN_SRC_STATUS_BACKGROUND_COLOR"));
            assertEquals("#FFFFFF", result.get("TWIN_SRC_STATUS_FONT_COLOR"));
            assertEquals("#00FF00", result.get("TWIN_DST_STATUS_BACKGROUND_COLOR"));
            assertEquals("#000000", result.get("TWIN_DST_STATUS_FONT_COLOR"));
        }

        @Test
        void collectData_noneEnabled_returnsEmptyContext() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, emptyProps());

            assertTrue(result.isEmpty());
        }

        @Test
        void collectData_preservesExistingContext() throws Exception {
            var context = new HashMap<String, String>();
            context.put("EXISTING", "value");

            var result = collector.collectData(history, context, fullProps());

            assertEquals("value", result.get("EXISTING"));
            assertEquals(7, result.size());
        }
    }

    @Nested
    class NullAndWrongTypeHandling {

        @Test
        void collectData_nullContext_throwsNullPointerException() {
            history.setContext(null);
            var context = new HashMap<String, String>();

            assertThrows(NullPointerException.class,
                    () -> collector.collectData(history, context, fullProps()));
        }

        @Test
        void collectData_wrongTypeContext_throwsClassCastException() {
            var wrongContext = new org.twins.core.dao.history.context.HistoryContextComment();
            history.setContext(wrongContext);
            var context = new HashMap<String, String>();

            assertThrows(ClassCastException.class,
                    () -> collector.collectData(history, context, fullProps()));
        }

        @Test
        void collectData_nullFromStatus_throwsNullPointerException() {
            var statusChange = new HistoryContextStatusChange();
            statusChange.setFromStatus(null);
            statusChange.setToStatus(toStatus);
            history.setContext(statusChange);

            var context = new HashMap<String, String>();

            assertThrows(NullPointerException.class,
                    () -> collector.collectData(history, context, fullProps()));
        }

        @Test
        void collectData_nullToStatus_throwsNullPointerException() {
            var statusChange = new HistoryContextStatusChange();
            statusChange.setFromStatus(fromStatus);
            statusChange.setToStatus(null);
            history.setContext(statusChange);

            var context = new HashMap<String, String>();

            assertThrows(NullPointerException.class,
                    () -> collector.collectData(history, context, fullProps()));
        }
    }
}
