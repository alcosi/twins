package org.twins.core.service.event.context;

import org.cambium.common.exception.ServiceException;
import org.twins.core.service.event.Event;

public abstract class ContextLoader<T> {
    public abstract void load(Event event, T object) throws ServiceException;
}
