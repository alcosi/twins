package org.twins.core.featurer.notificator.context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.resource.ResourceService;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_4909,
        name = "Twin Class Icon Context Collector",
        description = "Collects twin class icon URL (dark or light theme).")
@RequiredArgsConstructor
public class ContextCollectorTwinClassIcon extends ContextCollector {

    @FeaturerParam(name = "Collect icon url key", description = "", order = 1, optional = true, defaultValue = "TWIN_CLASS_ICON_URL")
    public static final FeaturerParamString collectKey = new FeaturerParamString("collectKey");

    @FeaturerParam(name = "Use dark icon", description = "If true, collects dark theme icon. Otherwise collects light theme icon.", order = 2, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean useDarkIcon = new FeaturerParamBoolean("useDarkIcon");

    private final ResourceService resourceService;

    @Override
    protected Map<String, String> collectData(HistoryEntity history, Map<String, String> context, Properties properties) throws ServiceException {
        String key = collectKey.extract(properties);
        boolean dark = useDarkIcon.extract(properties);

        var twinClass = history.getTwin().getTwinClass();
        resourceService.loadIconResources(twinClass);

        if (dark && twinClass.getIconDarkResource() != null) {
            String url = resourceService.getResourceUri(twinClass.getIconDarkResource());
            if (url != null) {
                context.put(key, url);
            }
        } else if (!dark && twinClass.getIconLightResource() != null) {
            String url = resourceService.getResourceUri(twinClass.getIconLightResource());
            if (url != null) {
                context.put(key, url);
            }
        }

        return context;
    }
}
