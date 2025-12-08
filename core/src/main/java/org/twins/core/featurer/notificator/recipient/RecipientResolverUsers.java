package org.twins.core.featurer.notificator.recipient;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetUserId;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_4701,
        name = "", //todo name
        description = "")
public class RecipientResolverUsers extends RecipientResolver {

    @FeaturerParam(name = "User ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet userIds = new FeaturerParamUUIDSetUserId("userIds");

    @Override
    protected Set<UUID> resolve(HistoryEntity history, Properties properties) throws ServiceException {
        //todo check impl logic
        return userIds.extract(properties);
    }
}
