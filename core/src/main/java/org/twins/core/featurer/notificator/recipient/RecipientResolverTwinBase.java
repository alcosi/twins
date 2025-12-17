package org.twins.core.featurer.notificator.recipient;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamTribool;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.history.context.HistoryContextUserChange;
import org.twins.core.enums.history.HistoryType;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_4704,
        name = "Recipient Resolver Twin Base",
        description = "The underlying data that will be added/deleted from result set")
public class RecipientResolverTwinBase extends RecipientResolver {

    @FeaturerParam(name = "param for history (actor)", order = 1, optional = true, defaultValue = "null")
    public static final FeaturerParamTribool actorParam = new FeaturerParamTribool("actorParam");

    @FeaturerParam(name = "param for twin [creator]", order = 2, optional = true, defaultValue = "null")
    public static final FeaturerParamTribool creatorParam = new FeaturerParamTribool("creatorParam");

    @FeaturerParam(name = "param for twin [assignee]", order = 3, optional = true, defaultValue = "null")
    public static final FeaturerParamTribool assigneeParam = new FeaturerParamTribool("assigneeParam");

    @FeaturerParam(name = "param for history [old twin assignee]", order = 4, optional = true, defaultValue = "null")
    public static final FeaturerParamTribool oldAssigneeParam = new FeaturerParamTribool("oldAssigneeParam");

    @FeaturerParam(name = "param for history [new twin assignee]", order = 5, optional = true, defaultValue = "null")
    public static final FeaturerParamTribool newAssigneeParam = new FeaturerParamTribool("newAssigneeParam");

    @Override
    protected void resolve(HistoryEntity history, Set<UUID> userIds, Properties properties) throws ServiceException {
        Boolean extractedActorParam = actorParam.extract(properties);
        Boolean extractedCreatorParam = creatorParam.extract(properties);
        Boolean extractedAssigneeParam = assigneeParam.extract(properties);
        Boolean extractedOldAssigneeParam = oldAssigneeParam.extract(properties);
        Boolean extractedNewAssigneeParam = newAssigneeParam.extract(properties);

        if (extractedActorParam != null) {
            if (extractedActorParam)
                userIds.add(history.getActorUserId());
            else
                userIds.remove(history.getActorUserId());
        }

        if (extractedCreatorParam != null) {
            if (extractedCreatorParam)
                userIds.add(history.getTwin().getCreatedByUserId());
            else
                userIds.remove(history.getTwin().getCreatedByUserId());
        }

        if (extractedAssigneeParam != null) {
            UUID assignerUserId = history.getTwin().getAssignerUserId();
            if (assignerUserId != null) {
                if (extractedAssigneeParam)
                    userIds.add(assignerUserId);
                else
                    userIds.remove(assignerUserId);
            }
        }

        if (history.getHistoryType().equals(HistoryType.assigneeChanged)) {
            if (extractedOldAssigneeParam != null) {
                HistoryContextUserChange oldAssignee = (HistoryContextUserChange) history.getContext();
                String userId = oldAssignee.getFromUser().getUserId();
                if (userId != null) {
                    if (extractedOldAssigneeParam)
                        userIds.add(UUID.fromString(userId));
                    else
                        userIds.remove(UUID.fromString(userId));
                }
            }
            if (extractedNewAssigneeParam != null) {
                HistoryContextUserChange newAssignee = (HistoryContextUserChange) history.getContext();
                String userId = newAssignee.getToUser().getUserId();
                if (userId != null) {
                    if (extractedNewAssigneeParam)
                        userIds.add(UUID.fromString(userId));
                    else
                        userIds.remove(UUID.fromString(userId));
                }
            }
        }
    }
}
