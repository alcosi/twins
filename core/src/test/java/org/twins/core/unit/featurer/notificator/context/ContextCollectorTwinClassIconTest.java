package org.twins.core.featurer.notificator.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.service.resource.ResourceService;
import org.twins.core.service.twin.TwinService;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class ContextCollectorTwinClassIconTest extends BaseUnitTest {

    private ContextCollectorTwinClassIcon collector;

    @Mock
    private ResourceService resourceService;

    @Mock
    private TwinService twinService;

    private TwinClassEntity twinClass;
    private TwinEntity twin;
    private HistoryEntity history;

    @BeforeEach
    void setUp() {
        collector = new ContextCollectorTwinClassIcon(resourceService, twinService);

        twinClass = new TwinClassEntity();
        twinClass.setId(UUID.randomUUID());

        twin = new TwinEntity();
        twin.setTwinClass(twinClass);

        history = new HistoryEntity();
        history.setTwin(twin);
    }

    private ContextCollectorBatch newBatch() {
        return new ContextCollectorBatch(UUID.randomUUID()).add(history);
    }

    private Properties lightProps() {
        var props = new Properties();
        props.put("collectKey", "TWIN_CLASS_ICON_URL");
        props.put("useDarkIcon", "false");
        return props;
    }

    private Properties darkProps() {
        var props = new Properties();
        props.put("collectKey", "TWIN_CLASS_ICON_URL");
        props.put("useDarkIcon", "true");
        return props;
    }

    @Nested
    class LightIcon {

        @Test
        void collectDataBatch_lightIcon_withResource_putsUrl() throws Exception {
            var lightResource = new ResourceEntity();
            twinClass.setIconLightResource(lightResource);
            when(resourceService.getResourceUri(lightResource)).thenReturn("http://cdn/icon-light.png");
            var batch = newBatch();

            collector.collectDataBatch(batch, lightProps());

            assertEquals("http://cdn/icon-light.png", batch.getContextByHistory().get(history).get("TWIN_CLASS_ICON_URL"));
            // bulk preload of icon resources happens once per batch (beforeCollect hook)
            verify(resourceService).loadIconResources(List.of(twinClass));
        }

        @Test
        void collectDataBatch_lightIcon_nullResource_returnsEmptyContext() throws Exception {
            twinClass.setIconLightResource(null);
            var batch = newBatch();

            collector.collectDataBatch(batch, lightProps());

            assertTrue(batch.getContextByHistory().get(history).isEmpty());
        }

        @Test
        void collectDataBatch_lightIcon_resourceWithNullUri_returnsEmptyContext() throws Exception {
            var lightResource = new ResourceEntity();
            twinClass.setIconLightResource(lightResource);
            when(resourceService.getResourceUri(lightResource)).thenReturn(null);
            var batch = newBatch();

            collector.collectDataBatch(batch, lightProps());

            assertTrue(batch.getContextByHistory().get(history).isEmpty());
        }
    }

    @Nested
    class DarkIcon {

        @Test
        void collectDataBatch_darkIcon_withResource_putsUrl() throws Exception {
            var darkResource = new ResourceEntity();
            twinClass.setIconDarkResource(darkResource);
            when(resourceService.getResourceUri(darkResource)).thenReturn("http://cdn/icon-dark.png");
            var batch = newBatch();

            collector.collectDataBatch(batch, darkProps());

            assertEquals("http://cdn/icon-dark.png", batch.getContextByHistory().get(history).get("TWIN_CLASS_ICON_URL"));
        }

        @Test
        void collectDataBatch_darkIcon_nullResource_returnsEmptyContext() throws Exception {
            twinClass.setIconDarkResource(null);
            var batch = newBatch();

            collector.collectDataBatch(batch, darkProps());

            assertTrue(batch.getContextByHistory().get(history).isEmpty());
        }

        @Test
        void collectDataBatch_darkIcon_resourceWithNullUri_returnsEmptyContext() throws Exception {
            var darkResource = new ResourceEntity();
            twinClass.setIconDarkResource(darkResource);
            when(resourceService.getResourceUri(darkResource)).thenReturn(null);
            var batch = newBatch();

            collector.collectDataBatch(batch, darkProps());

            assertTrue(batch.getContextByHistory().get(history).isEmpty());
        }
    }

    @Nested
    class CustomKey {

        @Test
        void collectDataBatch_customKey_usedForCollection() throws Exception {
            var props = new Properties();
            props.put("collectKey", "MY_ICON");
            props.put("useDarkIcon", "false");
            var lightResource = new ResourceEntity();
            twinClass.setIconLightResource(lightResource);
            when(resourceService.getResourceUri(lightResource)).thenReturn("http://cdn/icon.png");
            var batch = newBatch();

            collector.collectDataBatch(batch, props);

            var result = batch.getContextByHistory().get(history);
            assertEquals("http://cdn/icon.png", result.get("MY_ICON"));
            assertNull(result.get("TWIN_CLASS_ICON_URL"));
        }
    }

    @Nested
    class PreserveContext {

        @Test
        void collectDataBatch_preservesExistingContext() throws Exception {
            var lightResource = new ResourceEntity();
            twinClass.setIconLightResource(lightResource);
            when(resourceService.getResourceUri(lightResource)).thenReturn("http://cdn/icon.png");
            var batch = newBatch();
            batch.getContextByHistory().get(history).put("EXISTING", "value");

            collector.collectDataBatch(batch, lightProps());

            var result = batch.getContextByHistory().get(history);
            assertEquals("value", result.get("EXISTING"));
            assertEquals("http://cdn/icon.png", result.get("TWIN_CLASS_ICON_URL"));
        }
    }
}
