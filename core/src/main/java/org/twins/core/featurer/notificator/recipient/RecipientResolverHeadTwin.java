package org.twins.core.featurer.notificator.recipient;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinService;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_4705,
        name = "Head twin–based Recipient Resolver",
        description = "Resolves recipient users from head twin)")
public class RecipientResolverHeadTwin extends RecipientResolverExclude {

    @FeaturerParam(name = "Include twin id (when head user is twin)", order = 1, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean includeId = new FeaturerParamBoolean("includeId");

    @FeaturerParam(name = "Include twin creator", order = 2, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean includeCreator = new FeaturerParamBoolean("includeCreator");

    @FeaturerParam(name = "Include twin assignee", order = 3, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean includeAssignee = new FeaturerParamBoolean("includeAssignee");

    @Lazy
    @Autowired
    private TwinService twinService;

    @Override
    protected Set<UUID> resolve(HistoryEntity history, Properties properties) throws ServiceException {
        TwinEntity twin = history.getTwin();
        if (twin.getHeadTwin() == null) {
            twin.setHeadTwin(twinService.findHeadTwin(twin.getId()));
        }
        Set<UUID> userIds = new HashSet<>();
        if (includeId.extract(properties)) {
            userIds.add(twin.getId());//user is twin
        }
        if (includeCreator.extract(properties)) {
            if (twin.getHeadTwin().getCreatedByUserId() != null) {
                userIds.add(twin.getHeadTwin().getCreatedByUserId());
            }
        }
        if (includeAssignee.extract(properties)) {
            if (twin.getHeadTwin().getAssignerUserId() != null) {
                userIds.add(twin.getHeadTwin().getAssignerUserId());
            }
        }
        return userIds;
    }
}
