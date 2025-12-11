package org.twins.core.featurer.notificator.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamString;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;

@FeaturerType(id = FeaturerTwins.ID_4901,
        name = "Context collector user",
        description = "Collect from user (id, name, email, avatar)")
@Slf4j
public abstract class ContextCollectorUser extends ContextCollector {

    @FeaturerParam(name = "Collect id", description = "", order = 1)
    public static final FeaturerParamBoolean collectId = new FeaturerParamBoolean("collectId");

    @FeaturerParam(name = "Collect id key", description = "", order = 2, optional = true, defaultValue = "USER_ID")
    public static final FeaturerParamString collectIdKey = new FeaturerParamString("collectIdKey");

    @FeaturerParam(name = "Collect name", description = "", order = 3)
    public static final FeaturerParamBoolean collectName = new FeaturerParamBoolean("collectName");

    @FeaturerParam(name = "Collect name key", description = "", order = 4, optional = true, defaultValue = "USER_NAME")
    public static final FeaturerParamString collectNameKey = new FeaturerParamString("collectNameKey");

    @FeaturerParam(name = "Collect email", description = "", order = 5)
    public static final FeaturerParamBoolean collectEmail = new FeaturerParamBoolean("collectEmail");

    @FeaturerParam(name = "Collect email key", description = "", order = 6, optional = true, defaultValue = "USER_EMAIL")
    public static final FeaturerParamString collectEmailKey = new FeaturerParamString("collectEmailKey");

    @FeaturerParam(name = "Collect avatar", description = "", order = 7)
    public static final FeaturerParamBoolean collectAvatar = new FeaturerParamBoolean("collectAvatar");

    @FeaturerParam(name = "Collect avatar key", description = "", order = 8, optional = true, defaultValue = "USER_AVATAR")
    public static final FeaturerParamString collectAvatarKey = new FeaturerParamString("collectAvatarKey");

    @Override
    protected Map<String, String> collectData(HistoryEntity history, Map<String, String> context, Properties properties) {
        UserEntity user = getUser(history, properties);

        if (collectId.extract(properties)) {
            context.put(collectIdKey.getKey(), user.getId().toString());
        }
        if (collectName.extract(properties)) {
            context.put(collectNameKey.getKey(), user.getName());
        }
        if (collectEmail.extract(properties)) {
            context.put(collectEmailKey.getKey(), user.getEmail());
        }
        if (collectAvatar.extract(properties)) {
            context.put(collectAvatarKey.getKey(), user.getAvatar());
        }
        return context;
    }

    protected abstract UserEntity getUser(HistoryEntity history, Properties properties);

}
