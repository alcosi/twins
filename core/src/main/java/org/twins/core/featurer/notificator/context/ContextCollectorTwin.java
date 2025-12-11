package org.twins.core.featurer.notificator.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamString;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;

@FeaturerType(id = FeaturerTwins.ID_4902,
        name = "Context collector twin",
        description = "Collect form twin (id, name, description)")
@Slf4j
public class ContextCollectorTwin extends ContextCollector {

    @FeaturerParam(name = "Collect id", description = "", order = 1, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectId = new FeaturerParamBoolean("collectId");

    @FeaturerParam(name = "Collect id key", description = "", order = 2, optional = true, defaultValue = "TWIN_ID")
    public static final FeaturerParamString collectIdKey = new FeaturerParamString("collectIdKey");

    @FeaturerParam(name = "Collect name", description = "", order = 1, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectName = new FeaturerParamBoolean("collectName");

    @FeaturerParam(name = "Collect name key", description = "", order = 2, optional = true, defaultValue = "TWIN_NAME")
    public static final FeaturerParamString collectNameKey = new FeaturerParamString("collectNameKey");

    @FeaturerParam(name = "Collect description", description = "", order = 3, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectDescription = new FeaturerParamBoolean("collectDescription");

    @FeaturerParam(name = "Collect description key", description = "", order = 4, optional = true, defaultValue = "TWIN_DESCRIPTION")
    public static final FeaturerParamString collectDescriptionKey = new FeaturerParamString("collectDescriptionKey");

    @Override
    protected Map<String, String> collectData(HistoryEntity history, Map<String, String> context, Properties properties) {
        TwinEntity twin = history.getTwin();
        if (collectId.extract(properties)) {
            context.put(collectIdKey.extract(properties), twin.getId().toString());
        }
        if (collectName.extract(properties)) {
            context.put(collectNameKey.extract(properties), twin.getName());
        }
        if (collectDescription.extract(properties)) {
            context.put(collectDescriptionKey.extract(properties), twin.getDescription());
        }
        return context;
    }
}
