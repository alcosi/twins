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
import org.twins.core.service.usergroup.UserGroupService;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves recipient users based on their membership in specified user groups within a business account.
 * <p>Result is a query result (not a relation), so this resolver overrides {@link #resolveBatch}
 * directly (no {@link RecipientResolverAtomic}): {@code userGroupIds} are fixed by params, the resolver
 * group shares one domain and its business account ids (both provided by {@link RecipientResolveContext}).
 * It runs ONE bulk query via {@code getUsersForGroupsIn} and distributes the userIds per history.
 * The preload map is a local variable — thread-safe on the singleton bean.
 */
@Component
@Featurer(id = FeaturerTwins.ID_4702,
        name = "User Group–based Recipient Resolver",
        description = "Resolves recipient users based on their membership in specified user groups within a business account.")
public class RecipientResolverUserGroups extends RecipientResolver {

    @FeaturerParam(name = "User group ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet userGroupIds = new FeaturerParamUUIDSetUserId("userGroupIds");

    @Lazy
    @Autowired
    private UserGroupService userGroupService;

    @Override
    public void resolveBatch(RecipientResolveContext context, Properties properties) throws ServiceException {
        Set<UUID> paramUserGroupIds = userGroupIds.extract(properties);
        if (CollectionUtils.isEmpty(paramUserGroupIds)) {
            return;
        }
        if (context.getBusinessAccountIds().isEmpty()) {
            return;
        }
        // one bulk query → Map<businessAccountId, Set<userId>> (domain-level groups already spread to each BA)
        Map<UUID, Set<UUID>> userIdsByBusinessAccount =
                userGroupService.getUsersForGroupsIn(context.getDomainId(), context.getBusinessAccountIds(), paramUserGroupIds);
        // distribute per history
        for (var entry : context.getRecipientIdsByHistory().entrySet()) {
            UUID businessAccountId = entry.getKey().getTwin().getOwnerBusinessAccountId();
            Set<UUID> resolved = businessAccountId == null ? null : userIdsByBusinessAccount.get(businessAccountId);
            if (CollectionUtils.isNotEmpty(resolved)) {
                entry.getValue().addAll(resolved);
            }
        }
    }
}
