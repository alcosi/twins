package org.twins.core.featurer.notificator.recipient;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetUserId;
import org.twins.core.service.user.UserGroupService;

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
    private UserGroupService userGroupService;

    @Override
    protected void resolve(HistoryEntity history, Set<UUID> recipientIds, Properties properties) throws ServiceException {
        recipientIds.addAll(
                userGroupService.getUsersForGroups(
                        history.getTwin().getTwinClass().getDomainId(),
                        history.getTwin().getOwnerBusinessAccountId(),
                        userGroupIds.extract(properties)
                )
        );
    }
}
