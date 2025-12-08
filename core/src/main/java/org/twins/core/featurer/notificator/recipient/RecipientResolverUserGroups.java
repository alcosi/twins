package org.twins.core.featurer.notificator.recipient;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.user.UserGroupMapType2Repository;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetUserId;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_4702,
        name = "User Group–based Recipient Resolver",
        description = "Resolves recipient users based on their membership in specified user groups within a business account.")
public class RecipientResolverUserGroups extends RecipientResolver {

    @FeaturerParam(name = "User group ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet userGroupIds = new FeaturerParamUUIDSetUserId("userGroupIds");

    @Lazy
    @Autowired
    private UserGroupMapType2Repository userGroupMapType2Repository;

    @Override
    protected Set<UUID> resolve(HistoryEntity history, Properties properties) throws ServiceException {
        return getUsers(history.getTwin().getOwnerBusinessAccountId(), userGroupIds.extract(properties));
    }

    private Set<UUID> getUsers(UUID businessAccountId, Set<UUID> userGroupIds) {
        return userGroupMapType2Repository.findUserIdsByBusinessAccountIdAndUserGroupIds(businessAccountId, userGroupIds);
    }
}
