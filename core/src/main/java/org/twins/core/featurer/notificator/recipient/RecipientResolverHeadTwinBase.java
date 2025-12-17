package org.twins.core.featurer.notificator.recipient;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamTribool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.TwinEntity;
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

    @FeaturerParam(name = "param for twin (creator)", order = 1, optional = true, defaultValue = "null")
    public static final FeaturerParamTribool creatorParam = new FeaturerParamTribool("creatorParam");

    @FeaturerParam(name = "param for twin (assignee)", order = 2, optional = true, defaultValue = "null")
    public static final FeaturerParamTribool assigneeParam = new FeaturerParamTribool("assigneeParam");

    @Lazy
    @Autowired
    private TwinService twinService;

    @Override
    protected void resolve(HistoryEntity history, Set<UUID> userIds, Properties properties) throws ServiceException {
        Boolean extractedCreatorParam = creatorParam.extract(properties);
        Boolean extractedAssigneeParam = assigneeParam.extract(properties);

        TwinEntity twin = history.getTwin();
        if (twin.getHeadTwin() == null) {
            twin.setHeadTwin(twinService.findHeadTwin(twin.getId()));
        }

        if (extractedCreatorParam != null) {
            if (extractedCreatorParam)
                userIds.add(twin.getHeadTwin().getCreatedByUserId());
            else
                userIds.remove(twin.getHeadTwin().getCreatedByUserId());
        }

        if (extractedAssigneeParam != null) {
            UUID assignerUserId = twin.getHeadTwin().getAssignerUserId();
            if (assignerUserId != null) {
                if (extractedAssigneeParam)
                    userIds.add(assignerUserId);
                else
                    userIds.remove(assignerUserId);
            }
        }
    }
}
