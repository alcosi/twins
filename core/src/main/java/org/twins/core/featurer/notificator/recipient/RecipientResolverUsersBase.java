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
import org.twins.core.service.user.UserService;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_4701,
        name = "User Recipient Resolver",
        description = "")
public class RecipientResolverUsersBase extends RecipientResolverAtomic {

    @FeaturerParam(name = "User ids", description = "", order = 1, optional = true)
    public static final FeaturerParamUUIDSet userIds = new FeaturerParamUUIDSetUserId("userIds");

    @Lazy
    @Autowired
    private UserService userService;

    @Override
    protected void resolve(HistoryEntity history, Set<UUID> recipientIds, Properties properties) throws ServiceException {
        // domainId taken from the history's twin directly (chunk is single-domain anyway),
        // removing the thread-local ApiUser dependency so this resolver is batch-safe
        recipientIds.addAll(
                userService.filterUsersByBusinessAccountAndDomain(
                        userIds.extract(properties),
                        history.getTwin().getOwnerBusinessAccountId(),
                        history.getTwin().getTwinClass().getDomainId()
                )
        );
    }
}
