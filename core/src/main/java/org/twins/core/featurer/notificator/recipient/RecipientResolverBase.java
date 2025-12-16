package org.twins.core.featurer.notificator.recipient;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.history.context.HistoryContextUserChange;
import org.twins.core.enums.history.HistoryType;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_4704,
        name = "Base Recipient Resolver",
        description = "")
public class RecipientResolverBase extends RecipientResolver {
    @FeaturerParam(name = "include twin creator", order = 1, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean includeCreator = new FeaturerParamBoolean("includeCreator");

    @FeaturerParam(name = "include twin assignee", order = 2, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean includeAssignee = new FeaturerParamBoolean("includeAssignee");

    @FeaturerParam(name = "include old twin assignee", order = 3, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean includeOldAssignee = new FeaturerParamBoolean("includeOldAssignee");

    @FeaturerParam(name = "include new twin assignee", order = 4, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean includeNewAssignee = new FeaturerParamBoolean("includeNewAssignee");

    @FeaturerParam(name = "include history actor", order = 5, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean includeActor = new FeaturerParamBoolean("includeActor");
    
    public Set<UUID> resolve(HistoryEntity history, Properties properties) throws ServiceException {
        Set<UUID> users = new HashSet<>();
        resolveCreator(history, properties, users);
        resolveActor(history, properties, users);
        resolveAssignee(history, properties, users);
        resolveOldAssignee(history, properties, users);
        resolveNewAssignee(history, properties, users);
        return users;
    }

    private static void resolveCreator(HistoryEntity history, Properties properties, Set<UUID> users) {
        if (includeCreator.extract(properties)) {
            users.add(history.getTwin().getCreatedByUserId());
        }
    }

    private static void resolveActor(HistoryEntity history, Properties properties, Set<UUID> users) {
        if (includeActor.extract(properties)) {
            users.add(history.getActorUserId());
        }
    }

    private static void resolveAssignee(HistoryEntity history, Properties properties, Set<UUID> users) {
        UUID assignerUserId = history.getTwin().getAssignerUserId();
        if (assignerUserId == null)
            return;
        if (includeAssignee.extract(properties)) {
            users.add(assignerUserId);
        }
    }

    private static void resolveOldAssignee(HistoryEntity history, Properties properties, Set<UUID> users) {
        if (history.getHistoryType().equals(HistoryType.assigneeChanged)) {
            if (includeOldAssignee.extract(properties)) {
                HistoryContextUserChange oldAssignee = (HistoryContextUserChange) history.getContext();
                String userId = oldAssignee.getFromUser().getUserId();
                if (userId != null) {
                    users.add(UUID.fromString(userId));
                }
            }
        }
    }

    private static void resolveNewAssignee(HistoryEntity history, Properties properties, Set<UUID> users) {
        if (history.getHistoryType().equals(HistoryType.assigneeChanged)) {
            if (includeNewAssignee.extract(properties)) {
                HistoryContextUserChange newAssignee = (HistoryContextUserChange) history.getContext();
                String userId = newAssignee.getToUser().getUserId();
                if (userId != null) {
                    users.add(UUID.fromString(userId));
                }
            }
        }
    }
}
