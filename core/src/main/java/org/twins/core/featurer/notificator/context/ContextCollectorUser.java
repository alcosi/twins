package org.twins.core.featurer.notificator.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;

@FeaturerType(id = FeaturerTwins.ID_4901,
        name = "Context collector user",
        description = "")
@Slf4j
public class ContextCollectorUser extends ContextCollector {

    @FeaturerParam(name = "Collect id", description = "", order = 1)
    public static final FeaturerParamBoolean collectId = new FeaturerParamBoolean("collectId");

    @FeaturerParam(name = "Collect name", description = "", order = 2)
    public static final FeaturerParamBoolean collectName = new FeaturerParamBoolean("collectName");

    @FeaturerParam(name = "Collect email", description = "", order = 3)
    public static final FeaturerParamBoolean collectEmail = new FeaturerParamBoolean("collectEmail");

    @FeaturerParam(name = "Collect avatar", description = "", order = 4)
    public static final FeaturerParamBoolean collectAvatar = new FeaturerParamBoolean("collectAvatar");


    @Override
    protected Map<String, String> collect(HistoryEntity history, Map<String, String> context, Properties properties) throws ServiceException {
        return context;
    }
}
