package org.twins.core.featurer.notificator.recipient;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetUserId;
import org.twins.core.service.user.UserService;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves a fixed set of users (params) filtered by the twin's business account + domain.
 * <p>Result is a query result (not a relation), so this resolver overrides {@link #resolveBatch}
 * directly (no {@link RecipientResolverAtomic}): {@code userIds} are fixed by params, the resolver
 * group shares one domain and its business account ids (both provided by {@link RecipientResolveBatch}).
 * It runs ONE bulk filter via {@code filterUsersByBusinessAccountAndDomainIn} and distributes the
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
    public void resolveBatch(RecipientResolveBatch batch, Properties properties) throws ServiceException {
        Set<UUID> paramUserIds = userIds.extract(properties);
        if (CollectionUtils.isEmpty(paramUserIds)) {
            return;
        }
        if (batch.getBusinessAccountIds().isEmpty()) {
            return;
        }
        // one bulk filter → Map<businessAccountId, Set<userId>>
        Map<UUID, Set<UUID>> userIdsByBusinessAccount =
                userService.filterUsersByBusinessAccountAndDomainIn(paramUserIds, batch.getBusinessAccountIds(), batch.getDomainId());
        // distribute per history
        for (var entry : batch.getRecipientIdsByHistory().entrySet()) {
            UUID businessAccountId = entry.getKey().getTwin().getOwnerBusinessAccountId();
            Set<UUID> filtered = businessAccountId == null ? null : userIdsByBusinessAccount.get(businessAccountId);
            if (CollectionUtils.isNotEmpty(filtered)) {
                entry.getValue().addAll(filtered);
            }
        }
    }
}
