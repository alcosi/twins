package org.twins.core.featurer.notificator.recipient;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
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

    @Override
    protected Set<UUID> resolve(HistoryEntity history, Properties properties) throws ServiceException {
        return new HashSet<>();
    }
}
