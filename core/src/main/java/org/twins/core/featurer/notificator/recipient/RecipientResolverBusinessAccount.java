package org.twins.core.featurer.notificator.recipient;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.businessaccount.BusinessAccountUserRepository;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_4706,
        name = "Business Account Recipient Resolver",
        description = "Resolves all users within the business account as notification recipients.")
public class RecipientResolverBusinessAccount extends RecipientResolver {

    @Autowired
    private BusinessAccountUserRepository businessAccountUserRepository;

    @Override
    protected void resolve(HistoryEntity history, Set<UUID> recipientIds, Properties properties) throws ServiceException {
        UUID businessAccountId = history.getTwin().getOwnerBusinessAccountId();
        if (businessAccountId == null) {
            return;
        }
        businessAccountUserRepository
                .findByBusinessAccountId(businessAccountId)
                .forEach(bau -> recipientIds.add(bau.getUserId()));
    }
}
