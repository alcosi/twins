package org.twins.core.featurer.notificator.recipient;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetUserId;
import org.twins.core.service.user.UserService;

import java.util.*;

/**
 * Resolves a fixed set of users (params) filtered by the twin's business account + domain.
 * <p>Result is a query result (not a relation), so this resolver overrides {@link #resolveBatch}
 * directly (no {@link RecipientResolverAtomic}): {@code userIds} are fixed by params, the chunk shares
 * one domain, only business accounts vary per history. It collects the chunk's business account ids,
 * runs ONE bulk filter via {@code filterUsersByBusinessAccountAndDomainIn}, and distributes the
 * userIds per history. The preload map is a local variable — thread-safe on the singleton bean.
 */
@Component
@Featurer(id = FeaturerTwins.ID_4701,
        name = "User Recipient Resolver",
        description = "")
public class RecipientResolverUsersBase extends RecipientResolver {

    @FeaturerParam(name = "User ids", description = "", order = 1, optional = true)
    public static final FeaturerParamUUIDSet userIds = new FeaturerParamUUIDSetUserId("userIds");

    @Lazy
    @Autowired
    private UserService userService;

    @Override
    public void resolveBatch(Map<HistoryEntity, Set<UUID>> recipientIdsByHistory, Properties properties) throws ServiceException {
        Set<UUID> paramUserIds = userIds.extract(properties);
        if (CollectionUtils.isEmpty(paramUserIds)) {
            return;
        }
        // domainId is shared across the chunk (chunk = one domain); userIds are fixed by params.
        // only business accounts vary per history → collect distinct ones for one bulk filter.
        UUID domainId = null;
        Set<UUID> businessAccountIds = new HashSet<>();
        for (HistoryEntity history : recipientIdsByHistory.keySet()) {
            UUID businessAccountId = history.getTwin().getOwnerBusinessAccountId();
            if (businessAccountId != null) {
                businessAccountIds.add(businessAccountId);
            }
            if (domainId == null) {
                domainId = history.getTwin().getTwinClass().getDomainId();
            }
        }
        if (domainId == null || businessAccountIds.isEmpty()) {
            return;
        }
        // one bulk filter → Map<businessAccountId, Set<userId>>
        Map<UUID, Set<UUID>> userIdsByBusinessAccount =
                userService.filterUsersByBusinessAccountAndDomainIn(paramUserIds, businessAccountIds, domainId);
        // distribute per history
        for (Map.Entry<HistoryEntity, Set<UUID>> entry : recipientIdsByHistory.entrySet()) {
            UUID businessAccountId = entry.getKey().getTwin().getOwnerBusinessAccountId();
            Set<UUID> filtered = businessAccountId == null ? null : userIdsByBusinessAccount.get(businessAccountId);
            if (CollectionUtils.isNotEmpty(filtered)) {
                entry.getValue().addAll(filtered);
            }
        }
    }
}
