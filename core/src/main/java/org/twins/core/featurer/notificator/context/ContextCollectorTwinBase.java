package org.twins.core.featurer.notificator.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamString;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.TwinEntity;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
public abstract class ContextCollectorTwinBase extends ContextCollector {
    @FeaturerParam(name = "Collect id", description = "", order = 1, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectId = new FeaturerParamBoolean("collectId");

    @FeaturerParam(name = "Collect id key", description = "", order = 2, optional = true, defaultValue = "TWIN_ID")
    public static final FeaturerParamString collectIdKey = new FeaturerParamString("collectIdKey");

    @FeaturerParam(name = "Collect name", description = "", order = 3, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectName = new FeaturerParamBoolean("collectName");

    @FeaturerParam(name = "Collect name key", description = "", order = 4, optional = true, defaultValue = "TWIN_NAME")
    public static final FeaturerParamString collectNameKey = new FeaturerParamString("collectNameKey");

    @FeaturerParam(name = "Collect description", description = "", order = 5, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectDescription = new FeaturerParamBoolean("collectDescription");

    @FeaturerParam(name = "Collect description key", description = "", order = 6, optional = true, defaultValue = "TWIN_DESCRIPTION")
    public static final FeaturerParamString collectDescriptionKey = new FeaturerParamString("collectDescriptionKey");

    @FeaturerParam(name = "Collect owner business account", description = "", order = 7, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectBusinessAccount = new FeaturerParamBoolean("collectBusinessAccount");

    @FeaturerParam(name = "Collect owner business account key", description = "", order = 8, optional = true, defaultValue = "TWIN_OWNER_BUSINESS_ACCOUNT_ID")
    public static final FeaturerParamString collectBusinessAccountKey = new FeaturerParamString("collectBusinessAccountKey");

    @Override
    protected Map<String, String> collectData(HistoryEntity history, Map<String, String> context, Properties properties) throws ServiceException {
        TwinEntity twin = resolveTwin(history);
        if (collectId.extract(properties)) {
            context.put(collectIdKey.extract(properties), twin.getId().toString());
        }
        if (collectName.extract(properties)) {
            context.put(collectNameKey.extract(properties), twin.getName());
        }
        if (collectDescription.extract(properties)) {
            context.put(collectDescriptionKey.extract(properties), twin.getDescription());
        }
        if (collectBusinessAccount.extract(properties)) {
            UUID businessAccountId = twin.getOwnerBusinessAccountId() == null ? history.getTwin().getOwnerBusinessAccountId() : twin.getOwnerBusinessAccountId();
            context.put(collectBusinessAccountKey.extract(properties), businessAccountId.toString());
        }
        return context;
    }

    protected abstract TwinEntity resolveTwin(HistoryEntity history) throws ServiceException;
}
