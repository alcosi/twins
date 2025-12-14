package org.twins.core.featurer.notificator.recipient;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.history.context.HistoryContextUserChange;
import org.twins.core.enums.history.HistoryType;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;


@FeaturerType(id = FeaturerTwins.TYPE_47,
        name = "Recipient resolver",
        description = "")
@Slf4j
public abstract class RecipientResolver extends FeaturerTwins {

    @FeaturerParam(name = "Exclude twin creator", description = "", order = 1, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean excludeCreator = new FeaturerParamBoolean("excludeCreator");

    @FeaturerParam(name = "Exclude old twin assignee", description = "", order = 2, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean excludeOldAssignee = new FeaturerParamBoolean("excludeOldAssignee");

    @FeaturerParam(name = "Exclude new twin assignee", description = "", order = 3, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean excludeNewAssignee = new FeaturerParamBoolean("excludeNewAssignee");

    @FeaturerParam(name = "Exclude history actor", description = "User who is an author of history record", order = 4, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean excludeActor = new FeaturerParamBoolean("excludeActor");

    public Set<UUID> resolve(HistoryEntity history, HashMap<String, String> recipientParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, recipientParams, new HashMap<>());
        Set<UUID> users = resolve(history, properties);
        if (excludeCreator.extract(properties)) {
            users.remove(history.getTwin().getCreatedByUserId());
        }
        if (history.getHistoryType().equals(HistoryType.assigneeChanged) && excludeOldAssignee.extract(properties)) {
            HistoryContextUserChange contextUser = (HistoryContextUserChange) history.getContext();
            String oldAssignee = contextUser.getFromUser().getUserId();
            if (!oldAssignee.isBlank()) {
                users.remove(UUID.fromString(oldAssignee));
            }
        }
        if (history.getHistoryType().equals(HistoryType.assigneeChanged) && excludeNewAssignee.extract(properties)) {
            HistoryContextUserChange contextUser = (HistoryContextUserChange) history.getContext();
            String newAssignee = contextUser.getToUser().getUserId();
            if (!newAssignee.isBlank()) {
                users.remove(UUID.fromString(newAssignee));
            }
        }
        if (excludeActor.extract(properties)) {
            users.remove(history.getActorUserId());
        }
        return users;
    }

    protected abstract Set<UUID> resolve(HistoryEntity history, Properties properties) throws ServiceException;
}
