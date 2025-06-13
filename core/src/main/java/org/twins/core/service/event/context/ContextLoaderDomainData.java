package org.twins.core.service.event.context;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.StringUtils;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.service.event.Event;

public class ContextLoaderDomainData extends ContextLoader<DomainEntity> {
    public static final ContextLoaderDomainData INSTANCE = new ContextLoaderDomainData();

    @Override
    public void load(Event event, DomainEntity domain) throws ServiceException {
        event
                .addContext("domain.id", domain.getId().toString())
                .addContext("domain.name", domain.getName())
                .addContext("domain.name.else.key", StringUtils.isNotEmpty(domain.getName()) ? domain.getName() : domain.getKey());
    }
}
