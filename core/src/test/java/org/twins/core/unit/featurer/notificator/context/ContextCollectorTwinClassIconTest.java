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

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class ContextCollectorTwinClassIconTest extends BaseUnitTest {

    private ContextCollectorTwinClassIcon collector;

    @Mock
    private ResourceService resourceService;

    private TwinClassEntity twinClass;
    private TwinEntity twin;
    private HistoryEntity history;

    @BeforeEach
    void setUp() {
        collector = new ContextCollectorTwinClassIcon(resourceService);

        twinClass = new TwinClassEntity();
        twinClass.setId(UUID.randomUUID());

        twin = new TwinEntity();
        twin.setTwinClass(twinClass);

        history = new HistoryEntity();
        history.setTwin(twin);
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
        void collectData_lightIcon_withResource_putsUrl() throws Exception {
            var lightResource = new ResourceEntity();
            twinClass.setIconLightResource(lightResource);
            when(resourceService.getResourceUri(lightResource)).thenReturn("http://cdn/icon-light.png");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, lightProps());

            assertEquals("http://cdn/icon-light.png", result.get("TWIN_CLASS_ICON_URL"));
            verify(resourceService).loadIconResources(twinClass);
        }

        @Test
        void collectData_lightIcon_nullResource_returnsEmptyContext() throws Exception {
            twinClass.setIconLightResource(null);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, lightProps());

            assertTrue(result.isEmpty());
        }

        @Test
        void collectData_lightIcon_resourceWithNullUri_returnsEmptyContext() throws Exception {
            var lightResource = new ResourceEntity();
            twinClass.setIconLightResource(lightResource);
            when(resourceService.getResourceUri(lightResource)).thenReturn(null);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, lightProps());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class DarkIcon {

        @Test
        void collectData_darkIcon_withResource_putsUrl() throws Exception {
            var darkResource = new ResourceEntity();
            twinClass.setIconDarkResource(darkResource);
            when(resourceService.getResourceUri(darkResource)).thenReturn("http://cdn/icon-dark.png");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, darkProps());

            assertEquals("http://cdn/icon-dark.png", result.get("TWIN_CLASS_ICON_URL"));
        }

        @Test
        void collectData_darkIcon_nullResource_returnsEmptyContext() throws Exception {
            twinClass.setIconDarkResource(null);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, darkProps());

            assertTrue(result.isEmpty());
        }

        @Test
        void collectData_darkIcon_resourceWithNullUri_returnsEmptyContext() throws Exception {
            var darkResource = new ResourceEntity();
            twinClass.setIconDarkResource(darkResource);
            when(resourceService.getResourceUri(darkResource)).thenReturn(null);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, darkProps());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class CustomKey {

        @Test
        void collectData_customKey_usedForCollection() throws Exception {
            var props = new Properties();
            props.put("collectKey", "MY_ICON");
            props.put("useDarkIcon", "false");
            var lightResource = new ResourceEntity();
            twinClass.setIconLightResource(lightResource);
            when(resourceService.getResourceUri(lightResource)).thenReturn("http://cdn/icon.png");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props);

            assertEquals("http://cdn/icon.png", result.get("MY_ICON"));
            assertNull(result.get("TWIN_CLASS_ICON_URL"));
        }
    }

    @Nested
    class PreserveContext {

        @Test
        void collectData_preservesExistingContext() throws Exception {
            var lightResource = new ResourceEntity();
            twinClass.setIconLightResource(lightResource);
            when(resourceService.getResourceUri(lightResource)).thenReturn("http://cdn/icon.png");
            var context = new HashMap<String, String>();
            context.put("EXISTING", "value");

            var result = collector.collectData(history, context, lightProps());

            assertEquals("value", result.get("EXISTING"));
            assertEquals("http://cdn/icon.png", result.get("TWIN_CLASS_ICON_URL"));
        }
    }
}
