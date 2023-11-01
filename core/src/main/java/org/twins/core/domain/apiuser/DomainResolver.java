package org.twins.core.domain.apiuser;

import org.cambium.common.exception.ServiceException;

import java.util.UUID;

public interface DomainResolver {
    UUID resolveCurrentDomainId() throws ServiceException;
}
