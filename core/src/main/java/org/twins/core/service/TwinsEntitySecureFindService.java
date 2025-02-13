package org.twins.core.service;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.service.auth.AuthService;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;


public abstract class TwinsEntitySecureFindService<T> extends EntitySecureFindServiceImpl<T> {
    @Autowired
    AuthService authService;

    public BiFunction<UUID, String, Optional<T>> findByDomainIdAndKeyFunction() throws ServiceException {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED, "Method findByKey is not implemented in service");
    }

    @Override
    public Optional<T> findByKey(String key) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();;
        return findByDomainIdAndKeyFunction().apply(domainId, key);
    }
}
