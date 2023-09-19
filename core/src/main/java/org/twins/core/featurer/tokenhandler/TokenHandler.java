package org.twins.core.featurer.tokenhandler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.Channel;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;


@FeaturerType(id = 19,
        name = "TokenHandler",
        description = "")
@Slf4j
public abstract class TokenHandler extends Featurer {
    public ApiUser resolveApiUser(HashMap<String, String> initiatorParams, String token, DomainEntity domain, Channel channel) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, initiatorParams, new HashMap<>());
        return resolveApiUser(properties, token)
                .setDomain(domain)
                .setChannel(channel);
    }

    protected abstract ApiUser resolveApiUser(Properties properties, String token) throws ServiceException;
}
