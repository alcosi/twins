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
import org.twins.core.service.i18n.I18nService;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_4911,
        name = "Twin Class Context Collector",
        description = "Collects twin class information (id, key, name, description).")
@RequiredArgsConstructor
public class ContextCollectorTwinClass extends ContextCollector {

    @FeaturerParam(name = "Collect id", description = "", order = 1, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectId = new FeaturerParamBoolean("collectId");

    @FeaturerParam(name = "Collect id key", description = "", order = 2, optional = true, defaultValue = "TWIN_CLASS_ID")
    public static final FeaturerParamString collectIdKey = new FeaturerParamString("collectIdKey");

    @FeaturerParam(name = "Collect key", description = "", order = 3, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectKey = new FeaturerParamBoolean("collectKey");

    @FeaturerParam(name = "Collect key key", description = "", order = 4, optional = true, defaultValue = "TWIN_CLASS_KEY")
    public static final FeaturerParamString collectKeyKey = new FeaturerParamString("collectKeyKey");

    @FeaturerParam(name = "Collect name", description = "", order = 5, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectName = new FeaturerParamBoolean("collectName");

    @FeaturerParam(name = "Collect name key", description = "", order = 6, optional = true, defaultValue = "TWIN_CLASS_NAME")
    public static final FeaturerParamString collectNameKey = new FeaturerParamString("collectNameKey");

    @FeaturerParam(name = "Collect description", description = "", order = 7, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectDescription = new FeaturerParamBoolean("collectDescription");

    @FeaturerParam(name = "Collect description key", description = "", order = 8, optional = true, defaultValue = "TWIN_CLASS_DESCRIPTION")
    public static final FeaturerParamString collectDescriptionKey = new FeaturerParamString("collectDescriptionKey");

    private final I18nService i18nService;

    @Override
    protected Map<String, String> collectData(HistoryEntity history, Map<String, String> context, Properties properties) throws ServiceException {
        var twinClass = history.getTwin().getTwinClass();

        if (collectId.extract(properties)) {
            context.put(collectIdKey.extract(properties), twinClass.getId().toString());
        }
        if (collectKey.extract(properties)) {
            context.put(collectKeyKey.extract(properties), twinClass.getKey());
        }
        if (collectName.extract(properties) && twinClass.getNameI18NId() != null) {
            context.put(collectNameKey.extract(properties), i18nService.translateToLocale(twinClass.getNameI18NId()));
        }
        if (collectDescription.extract(properties) && twinClass.getDescriptionI18NId() != null) {
            context.put(collectDescriptionKey.extract(properties), i18nService.translateToLocale(twinClass.getDescriptionI18NId()));
        }

        return context;
    }
}
