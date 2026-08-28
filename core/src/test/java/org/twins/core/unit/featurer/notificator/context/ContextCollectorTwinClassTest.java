package org.twins.core.featurer.notificator.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class ContextCollectorTwinClassTest extends BaseUnitTest {

    private ContextCollectorTwinClass collector;

    private UUID twinClassId;
    private TwinClassEntity twinClass;
    private TwinEntity twin;
    private HistoryEntity history;

    @BeforeEach
    void setUp() {
        collector = new ContextCollectorTwinClass();

        twinClassId = UUID.randomUUID();
        twinClass = new TwinClassEntity();
        twinClass.setId(twinClassId);
        twinClass.setKey("test-class-key");

        twin = new TwinEntity();
        twin.setTwinClass(twinClass);

        history = new HistoryEntity();
        history.setTwin(twin);
    }

    private ContextCollectorBatch newBatch() {
        return new ContextCollectorBatch(UUID.randomUUID()).add(history);
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
        void collectDataBatch_collectIdTrue_putsTwinClassId() throws Exception {
            var batch = newBatch();

            collector.collectDataBatch(batch, props(true, false, false, false));

            assertEquals(twinClassId.toString(), batch.getContextByHistory().get(history).get("TWIN_CLASS_ID"));
        }

        @Test
        void collectDataBatch_collectIdFalse_skipsTwinClassId() throws Exception {
            var batch = newBatch();

            collector.collectDataBatch(batch, props(false, false, false, false));

            assertNull(batch.getContextByHistory().get(history).get("TWIN_CLASS_ID"));
        }

        @Test
        void collectDataBatch_customIdKey_usedWhenCollecting() throws Exception {
            var props = props(true, false, false, false);
            props.put("collectIdKey", "CLASS_ID");
            var batch = newBatch();

            collector.collectDataBatch(batch, props);

            var result = batch.getContextByHistory().get(history);
            assertEquals(twinClassId.toString(), result.get("CLASS_ID"));
            assertNull(result.get("TWIN_CLASS_ID"));
        }
    }

    @Nested
    class CollectKey {

        @Test
        void collectDataBatch_collectKeyTrue_putsTwinClassKey() throws Exception {
            var batch = newBatch();

            collector.collectDataBatch(batch, props(false, true, false, false));

            assertEquals("test-class-key", batch.getContextByHistory().get(history).get("TWIN_CLASS_KEY"));
        }

        @Test
        void collectDataBatch_collectKeyFalse_skipsTwinClassKey() throws Exception {
            var batch = newBatch();

            collector.collectDataBatch(batch, props(false, false, false, false));

            assertNull(batch.getContextByHistory().get(history).get("TWIN_CLASS_KEY"));
        }
    }

    @Nested
    class CollectName {

        @Test
        void collectDataBatch_collectNameTrue_withI18nId_putsPlaceholderAndRegistersId() throws Exception {
            var nameI18nId = UUID.randomUUID();
            twinClass.setNameI18NId(nameI18nId);
            var batch = newBatch();

            collector.collectDataBatch(batch, props(false, false, true, false));

            // two-phase i18n: the context holds the #i18n placeholder, the caller materializes it
            // with the per-locale translation in bulk afterwards
            assertEquals(ContextCollectorBatch.i18nPlaceholder(nameI18nId),
                    batch.getContextByHistory().get(history).get("TWIN_CLASS_NAME"));
            assertTrue(batch.getI18nIds().contains(nameI18nId));
        }

        @Test
        void collectDataBatch_collectNameTrue_nullI18nId_skipsName() throws Exception {
            twinClass.setNameI18NId(null);
            var batch = newBatch();

            collector.collectDataBatch(batch, props(false, false, true, false));

            assertNull(batch.getContextByHistory().get(history).get("TWIN_CLASS_NAME"));
            assertTrue(batch.getI18nIds().isEmpty());
        }

        @Test
        void collectDataBatch_collectNameFalse_skipsName() throws Exception {
            var nameI18nId = UUID.randomUUID();
            twinClass.setNameI18NId(nameI18nId);
            var batch = newBatch();

            collector.collectDataBatch(batch, props(false, false, false, false));

            assertNull(batch.getContextByHistory().get(history).get("TWIN_CLASS_NAME"));
            assertTrue(batch.getI18nIds().isEmpty());
        }
    }

    @Nested
    class CollectDescription {

        @Test
        void collectDataBatch_collectDescriptionTrue_withI18nId_putsPlaceholderAndRegistersId() throws Exception {
            var descI18nId = UUID.randomUUID();
            twinClass.setDescriptionI18NId(descI18nId);
            var batch = newBatch();

            collector.collectDataBatch(batch, props(false, false, false, true));

            assertEquals(ContextCollectorBatch.i18nPlaceholder(descI18nId),
                    batch.getContextByHistory().get(history).get("TWIN_CLASS_DESCRIPTION"));
            assertTrue(batch.getI18nIds().contains(descI18nId));
        }

        @Test
        void collectDataBatch_collectDescriptionTrue_nullI18nId_skipsDescription() throws Exception {
            twinClass.setDescriptionI18NId(null);
            var batch = newBatch();

            collector.collectDataBatch(batch, props(false, false, false, true));

            assertNull(batch.getContextByHistory().get(history).get("TWIN_CLASS_DESCRIPTION"));
            assertTrue(batch.getI18nIds().isEmpty());
        }
    }

    @Nested
    class CollectMultiple {

        @Test
        void collectDataBatch_allEnabled_collectsAllFields() throws Exception {
            var nameI18nId = UUID.randomUUID();
            var descI18nId = UUID.randomUUID();
            twinClass.setNameI18NId(nameI18nId);
            twinClass.setDescriptionI18NId(descI18nId);
            var batch = newBatch();

            collector.collectDataBatch(batch, props(true, true, true, true));

            // id + key land in the context directly; name + description are i18n placeholders
            var result = batch.getContextByHistory().get(history);
            assertEquals(4, result.size());
            assertEquals(twinClassId.toString(), result.get("TWIN_CLASS_ID"));
            assertEquals("test-class-key", result.get("TWIN_CLASS_KEY"));
            assertEquals(ContextCollectorBatch.i18nPlaceholder(nameI18nId), result.get("TWIN_CLASS_NAME"));
            assertEquals(ContextCollectorBatch.i18nPlaceholder(descI18nId), result.get("TWIN_CLASS_DESCRIPTION"));
            assertEquals(2, batch.getI18nIds().size());
            assertTrue(batch.getI18nIds().contains(nameI18nId));
            assertTrue(batch.getI18nIds().contains(descI18nId));
        }

        @Test
        void collectDataBatch_noneEnabled_returnsEmptyContext() throws Exception {
            var batch = newBatch();

            collector.collectDataBatch(batch, props(false, false, false, false));

            assertTrue(batch.getContextByHistory().get(history).isEmpty());
            assertTrue(batch.getI18nIds().isEmpty());
        }
    }

    @Nested
    class NullHandling {

        @Test
        void collectDataBatch_nullTwinClass_collectsNothingWithoutNpe() throws Exception {
            twin.setTwinClass(null);
            var batch = newBatch();

            collector.collectDataBatch(batch, props(true, true, true, true));

            assertTrue(batch.getContextByHistory().get(history).isEmpty());
            assertTrue(batch.getI18nIds().isEmpty());
        }
    }
}
