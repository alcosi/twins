package org.twins.core.featurer.notificator.recipient;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.SetUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
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
        description = "Resolve users from history and history twin")
public class RecipientResolverTwinBase extends RecipientResolver {

    @FeaturerParam(name = "resolve history actor user", order = 1, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean resolveActor = new FeaturerParamBoolean("resolveActor");

    @FeaturerParam(name = "resolve history twin creator user", order = 2, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean resolveCreator = new FeaturerParamBoolean("resolveCreator");

    @FeaturerParam(name = "resolve history twin assignee user", order = 3, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean resolveAssignee = new FeaturerParamBoolean("resolveAssignee");

    @FeaturerParam(name = "resolve history twin olg assignee user", order = 4, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean resolveOldAssignee = new FeaturerParamBoolean("resolveOldAssignee");

    @FeaturerParam(name = "resolve history twin new assignee user", order = 4, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean resolveNewAssignee = new FeaturerParamBoolean("resolveNewAssignee");

    @Override
    protected void resolve(HistoryEntity history, Set<UUID> userIds, Properties properties) throws ServiceException {
        if (resolveActor.extract(properties)) {
            SetUtils.safeAdd(userIds, history.getActorUserId());
        }

        if (resolveCreator.extract(properties)) {
            SetUtils.safeAdd(userIds, history.getTwin().getCreatedByUserId());
        }

        if (resolveAssignee.extract(properties)) {
            SetUtils.safeAdd(userIds, history.getTwin().getAssignerUserId());
        }

        if (history.getHistoryType().equals(HistoryType.assigneeChanged)) {
            HistoryContextUserChange historyContext = (HistoryContextUserChange) history.getContext();
            if (resolveOldAssignee.extract(properties)) {
                String userId = historyContext.getFromUser().getUserId();
                if (userId != null) {
                    userIds.add(UUID.fromString(userId));
                }
            }
            if (resolveNewAssignee.extract(properties)) {
                String userId = historyContext.getToUser().getUserId();
                if (userId != null) {
                    userIds.add(UUID.fromString(userId));
                }
            }
        }
    }
}
