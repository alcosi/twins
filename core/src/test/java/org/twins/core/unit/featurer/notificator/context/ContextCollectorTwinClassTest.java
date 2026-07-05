package org.twins.core.featurer.notificator.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.service.i18n.I18nService;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class ContextCollectorTwinClassTest extends BaseUnitTest {

    private ContextCollectorTwinClass collector;

    @Mock
    private I18nService i18nService;

    private UUID twinClassId;
    private TwinClassEntity twinClass;
    private TwinEntity twin;
    private HistoryEntity history;

    @BeforeEach
    void setUp() {
        collector = new ContextCollectorTwinClass(i18nService);

        twinClassId = UUID.randomUUID();
        twinClass = new TwinClassEntity();
        twinClass.setId(twinClassId);
        twinClass.setKey("test-class-key");

        twin = new TwinEntity();
        twin.setTwinClass(twinClass);

        history = new HistoryEntity();
        history.setTwin(twin);
    }

    private Properties props(boolean collectId, boolean collectKey, boolean collectName, boolean collectDescription) {
        var props = new Properties();
        props.put("collectId", String.valueOf(collectId));
        props.put("collectIdKey", "TWIN_CLASS_ID");
        props.put("collectKey", String.valueOf(collectKey));
        props.put("collectKeyKey", "TWIN_CLASS_KEY");
        props.put("collectName", String.valueOf(collectName));
        props.put("collectNameKey", "TWIN_CLASS_NAME");
        props.put("collectDescription", String.valueOf(collectDescription));
        props.put("collectDescriptionKey", "TWIN_CLASS_DESCRIPTION");
        return props;
    }

    @Nested
    class CollectId {

        @Test
        void collectData_collectIdTrue_putsTwinClassId() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(true, false, false, false));

            assertEquals(twinClassId.toString(), result.get("TWIN_CLASS_ID"));
        }

        @Test
        void collectData_collectIdFalse_skipsTwinClassId() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, false, false));

            assertNull(result.get("TWIN_CLASS_ID"));
        }

        @Test
        void collectData_customIdKey_usedWhenCollecting() throws Exception {
            var props = props(true, false, false, false);
            props.put("collectIdKey", "CLASS_ID");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props);

            assertEquals(twinClassId.toString(), result.get("CLASS_ID"));
            assertNull(result.get("TWIN_CLASS_ID"));
        }
    }

    @Nested
    class CollectKey {

        @Test
        void collectData_collectKeyTrue_putsTwinClassKey() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, true, false, false));

            assertEquals("test-class-key", result.get("TWIN_CLASS_KEY"));
        }

        @Test
        void collectData_collectKeyFalse_skipsTwinClassKey() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, false, false));

            assertNull(result.get("TWIN_CLASS_KEY"));
        }
    }

    @Nested
    class CollectName {

        @Test
        void collectData_collectNameTrue_withI18nId_putsTranslatedName() throws Exception {
            var nameI18nId = UUID.randomUUID();
            twinClass.setNameI18NId(nameI18nId);
            when(i18nService.translateToLocale(nameI18nId)).thenReturn("Translated Name");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, true, false));

            assertEquals("Translated Name", result.get("TWIN_CLASS_NAME"));
            verify(i18nService).translateToLocale(nameI18nId);
        }

        @Test
        void collectData_collectNameTrue_nullI18nId_skipsName() throws Exception {
            twinClass.setNameI18NId(null);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, true, false));

            assertNull(result.get("TWIN_CLASS_NAME"));
            verifyNoInteractions(i18nService);
        }

        @Test
        void collectData_collectNameFalse_skipsName() throws Exception {
            var nameI18nId = UUID.randomUUID();
            twinClass.setNameI18NId(nameI18nId);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, false, false));

            assertNull(result.get("TWIN_CLASS_NAME"));
            verifyNoInteractions(i18nService);
        }
    }

    @Nested
    class CollectDescription {

        @Test
        void collectData_collectDescriptionTrue_withI18nId_putsTranslatedDescription() throws Exception {
            var descI18nId = UUID.randomUUID();
            twinClass.setDescriptionI18NId(descI18nId);
            when(i18nService.translateToLocale(descI18nId)).thenReturn("Translated Description");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, false, true));

            assertEquals("Translated Description", result.get("TWIN_CLASS_DESCRIPTION"));
            verify(i18nService).translateToLocale(descI18nId);
        }

        @Test
        void collectData_collectDescriptionTrue_nullI18nId_skipsDescription() throws Exception {
            twinClass.setDescriptionI18NId(null);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, false, true));

            assertNull(result.get("TWIN_CLASS_DESCRIPTION"));
            verifyNoInteractions(i18nService);
        }
    }

    @Nested
    class CollectMultiple {

        @Test
        void collectData_allEnabled_collectsAllFields() throws Exception {
            var nameI18nId = UUID.randomUUID();
            var descI18nId = UUID.randomUUID();
            twinClass.setNameI18NId(nameI18nId);
            twinClass.setDescriptionI18NId(descI18nId);
            when(i18nService.translateToLocale(nameI18nId)).thenReturn("Name");
            when(i18nService.translateToLocale(descI18nId)).thenReturn("Desc");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(true, true, true, true));

            assertEquals(4, result.size());
            assertEquals(twinClassId.toString(), result.get("TWIN_CLASS_ID"));
            assertEquals("test-class-key", result.get("TWIN_CLASS_KEY"));
            assertEquals("Name", result.get("TWIN_CLASS_NAME"));
            assertEquals("Desc", result.get("TWIN_CLASS_DESCRIPTION"));
        }

        @Test
        void collectData_noneEnabled_returnsEmptyContext() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, false, false));

            assertTrue(result.isEmpty());
            verifyNoInteractions(i18nService);
        }
    }

    @Nested
    class NullHandling {

        @Test
        void collectData_nullTwinClass_collectsNothingWithoutNpe() throws Exception {
            twin.setTwinClass(null);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(true, true, true, true));

            assertTrue(result.isEmpty());
            verifyNoInteractions(i18nService);
        }

        @Test
        void collectData_nullTwin_collectsNothingWithoutNpe() throws Exception {
            history.setTwin(null);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(true, true, true, true));

            assertTrue(result.isEmpty());
            verifyNoInteractions(i18nService);
        }
    }
}
