package org.twins.core.featurer.notificator.recipient;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.SetUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_4705,
        name = "Recipient Resolver Head Twin Base",
        description = "The underlying data from head twin that will be added/deleted form result set")
public class RecipientResolverHeadTwinBase extends RecipientResolver {
    @FeaturerParam(name = "resolve history twin creator user", order = 2, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean resolveHeadTwinCreator = new FeaturerParamBoolean("resolveHeadTwinCreator");

    @FeaturerParam(name = "resolve history twin assignee user", order = 3, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean resolveHeadTwinAssignee = new FeaturerParamBoolean("resolveHeadTwinAssignee");

    @Lazy
    @Autowired
    private TwinService twinService;

    @Override
    protected void resolve(HistoryEntity history, Set<UUID> userIds, Properties properties) throws ServiceException {
        var twin = history.getTwin();
        if (twin.getHeadTwin() == null) {
            twin.setHeadTwin(twinService.findHeadTwin(twin.getId()));
        }
        if (twin.getHeadTwin() == null) {
            return;
        }
        var headTwin = twin.getHeadTwin();
        if (resolveHeadTwinCreator.extract(properties)) {
            SetUtils.safeAdd(userIds, headTwin.getCreatedByUserId());
        }

        if (resolveHeadTwinAssignee.extract(properties)) {
            SetUtils.safeAdd(userIds, headTwin.getAssignerUserId());
        }
    }
}
