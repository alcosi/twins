package org.twins.core.featurer.tokenhandler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.domain.Channel;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.identityprovider.TokenMetaData;

import java.util.HashMap;
import java.util.Properties;

@Deprecated //todo delete me after
@FeaturerType(id = FeaturerTwins.TYPE_19,
        name = "TokenHandler",
        description = "")
@Slf4j
public abstract class TokenHandler extends FeaturerTwins {
    public TokenMetaData resolveUserIdAndBusinessAccountId(HashMap<String, String> initiatorParams, String token, DomainEntity domain, Channel channel) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, initiatorParams, new HashMap<>());
        return resolveUserIdAndBusinessAccountId(properties, token);
    }

    protected abstract TokenMetaData resolveUserIdAndBusinessAccountId(Properties properties, String token) throws ServiceException;

}
