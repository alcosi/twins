package org.twins.core.featurer.notificator.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.history.context.HistoryContextPermissionSchemaChange;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_4906,
        name = "Context collector tier from history",
        description = "Collect changes for tier form history")
@Slf4j
public class ContextCollectorHistoryTier extends ContextCollector {

    @FeaturerParam(name = "Collect old tier id", description = "", order = 1, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean collectOldTierId = new FeaturerParamBoolean("collectOldTierId");

    @FeaturerParam(name = "Collect old tier id key", description = "", order = 1, optional = true, defaultValue = "OLD_TIER_ID")
    public static final FeaturerParamString collectOldTierIdKey = new FeaturerParamString("collectOldTierIdKey");

    @FeaturerParam(name = "Collect old tier name", description = "", order = 1, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean collectOldTierName = new FeaturerParamBoolean("collectOldTierName");

    @FeaturerParam(name = "Collect old tier name key", description = "", order = 1, optional = true, defaultValue = "OLD_TIER_NAME")
    public static final FeaturerParamString collectOldTierNameKey = new FeaturerParamString("collectOldTierNameKey");

    @FeaturerParam(name = "Collect new tier id", description = "", order = 1, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean collectNewTierId = new FeaturerParamBoolean("collectNewTierId");

    @FeaturerParam(name = "Collect new tier id key", description = "", order = 1, optional = true, defaultValue = "NEW_TIER_ID")
    public static final FeaturerParamString collectNewTierIdKey = new FeaturerParamString("collectNewTierIdKey");

    @FeaturerParam(name = "Collect new tier name", description = "", order = 1, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean collectNewTierName = new FeaturerParamBoolean("collectNewTierName");

    @FeaturerParam(name = "Collect new tier name key", description = "", order = 1, optional = true, defaultValue = "NEW_TIER_NAME")
    public static final FeaturerParamString collectNewTierNameKey = new FeaturerParamString("collectNewTierNameKey");

    @Override
    protected Map<String, String> collectData(HistoryEntity history, Map<String, String> context, Properties properties) {
        HistoryContextPermissionSchemaChange permissionSchemaChange = (HistoryContextPermissionSchemaChange) history.getContext();
        if (collectOldTierId.extract(properties)) {
            context.put(collectOldTierIdKey.extract(properties), permissionSchemaChange.getFromPermissionSchema().getId().toString());
        }
        if (collectOldTierName.extract(properties)) {
            context.put(collectOldTierNameKey.extract(properties), permissionSchemaChange.getFromPermissionSchema().getName());
        }
        if (collectNewTierId.extract(properties)) {
            context.put(collectNewTierIdKey.extract(properties), permissionSchemaChange.getFromPermissionSchema().getId().toString());
        }
        if (collectNewTierName.extract(properties)) {
            context.put(collectNewTierNameKey.extract(properties), permissionSchemaChange.getFromPermissionSchema().getName());
        }
        return context;

    }
}
