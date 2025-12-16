package org.twins.core.featurer.notificator.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamString;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.user.UserEntity;

import java.util.Map;
import java.util.Properties;

@Slf4j
public abstract class ContextCollectorUser extends ContextCollector {

    @FeaturerParam(name = "Collect id", description = "", order = 1, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectId = new FeaturerParamBoolean("collectId");

    @FeaturerParam(name = "Collect id key", description = "", order = 2, optional = true, defaultValue = "USER_ID")
    public static final FeaturerParamString collectIdKey = new FeaturerParamString("collectIdKey");

    @FeaturerParam(name = "Collect name", description = "", order = 3, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectName = new FeaturerParamBoolean("collectName");

    @FeaturerParam(name = "Collect name key", description = "", order = 4, optional = true, defaultValue = "USER_NAME")
    public static final FeaturerParamString collectNameKey = new FeaturerParamString("collectNameKey");

    @FeaturerParam(name = "Collect email", description = "", order = 5, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectEmail = new FeaturerParamBoolean("collectEmail");

    @FeaturerParam(name = "Collect email key", description = "", order = 6, optional = true, defaultValue = "USER_EMAIL")
    public static final FeaturerParamString collectEmailKey = new FeaturerParamString("collectEmailKey");

    @FeaturerParam(name = "Collect avatar", description = "", order = 7, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectAvatar = new FeaturerParamBoolean("collectAvatar");

    @FeaturerParam(name = "Collect avatar key", description = "", order = 8, optional = true, defaultValue = "USER_AVATAR")
    public static final FeaturerParamString collectAvatarKey = new FeaturerParamString("collectAvatarKey");

    @Override
    protected Map<String, String> collectData(HistoryEntity history, Map<String, String> context, Properties properties) {
        UserEntity user = getUser(history, properties);

        if (collectId.extract(properties)) {
            context.put(collectIdKey.extract(properties), user.getId().toString());
        }
        if (collectName.extract(properties)) {
            if (user.getName() != null) //todo logic if null
                context.put(collectNameKey.extract(properties), user.getName());
        }
        if (collectEmail.extract(properties)) {
            if (user.getEmail() != null) //todo logic if null
                context.put(collectEmailKey.extract(properties), user.getEmail());
        }
        if (collectAvatar.extract(properties)) {
            if (user.getAvatar() != null) //todo logic if null
                context.put(collectAvatarKey.extract(properties), user.getAvatar());
        }
        return context;
    }

    protected abstract UserEntity getUser(HistoryEntity history, Properties properties);

}
