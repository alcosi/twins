package org.twins.core.featurer.notificator.recipient;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.history.context.HistoryContextUserChange;
import org.twins.core.enums.history.HistoryType;

import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;


@Component
@Slf4j
public abstract class RecipientResolverExclude extends RecipientResolver {
    @FeaturerParam(name = "Exclude twin creator", order = 1, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean excludeCreator = new FeaturerParamBoolean("excludeCreator");

    @FeaturerParam(name = "Exclude twin assignee", order = 2, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean excludeAssignee = new FeaturerParamBoolean("excludeAssignee");

    @FeaturerParam(name = "Exclude old twin assignee", order = 3, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean excludeOldAssignee = new FeaturerParamBoolean("excludeOldAssignee");

    @FeaturerParam(name = "Exclude new twin assignee", order = 4, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean excludeNewAssignee = new FeaturerParamBoolean("excludeNewAssignee");

    @FeaturerParam(name = "Exclude history actor", order = 5, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean excludeActor = new FeaturerParamBoolean("excludeActor");

    public Set<UUID> resolve(HistoryEntity history, HashMap<String, String> recipientParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, recipientParams, new HashMap<>());
        Set<UUID> users = resolve(history, properties);
        resolveCreator(history, properties, users);
        resolveActor(history, properties, users);
        resolveAssignee(history, properties, users);
        resolveOldAssignee(history, properties, users);
        resolveNewAssignee(history, properties, users);
        return users;
    }

    protected abstract Set<UUID> resolve(HistoryEntity history, Properties properties) throws ServiceException;

    private static void resolveCreator(HistoryEntity history, Properties properties, Set<UUID> users) {
        if (excludeCreator.extract(properties)) {
            users.remove(history.getTwin().getCreatedByUserId());
        }
    }

    private static void resolveActor(HistoryEntity history, Properties properties, Set<UUID> users) {
        if (excludeActor.extract(properties)) {
            users.remove(history.getActorUserId());
        }
    }

    private static void resolveAssignee(HistoryEntity history, Properties properties, Set<UUID> users) {
        UUID assignerUserId = history.getTwin().getAssignerUserId();
        if (assignerUserId == null)
            return;
        if (excludeAssignee.extract(properties)) {
            users.remove(assignerUserId);
        }
    }

    private static void resolveOldAssignee(HistoryEntity history, Properties properties, Set<UUID> users) {
        if (history.getHistoryType().equals(HistoryType.assigneeChanged)) {
            if (excludeOldAssignee.extract(properties)) {
                HistoryContextUserChange oldAssignee = (HistoryContextUserChange) history.getContext();
                String userId = oldAssignee.getFromUser().getUserId();
                if (userId != null) {
                    users.remove(UUID.fromString(userId));
                }
            }
        }
    }

    private static void resolveNewAssignee(HistoryEntity history, Properties properties, Set<UUID> users) {
        if (history.getHistoryType().equals(HistoryType.assigneeChanged)) {
            if (excludeNewAssignee.extract(properties)) {
                HistoryContextUserChange newAssignee = (HistoryContextUserChange) history.getContext();
                String userId = newAssignee.getToUser().getUserId();
                if (userId != null) {
                    users.remove(UUID.fromString(userId));
                }
            }
        }
    }
}
