package org.twins.core.featurer.tokenhandler;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.domain.Channel;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;


@FeaturerType(id = FeaturerTwins.TYPE_19,
        name = "TokenHandler",
        description = "")
@Slf4j
public abstract class TokenHandler extends FeaturerTwins {
    public Result resolveUserIdAndBusinessAccountId(HashMap<String, String> initiatorParams, String token, DomainEntity domain, Channel channel) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, initiatorParams, new HashMap<>());
        return resolveUserIdAndBusinessAccountId(properties, token);
    }

    protected abstract Result resolveUserIdAndBusinessAccountId(Properties properties, String token) throws ServiceException;

    @Data
    @Accessors(chain = true)
    public static class Result {
        UUID userId;
        UUID businessAccountId;
    }
}
