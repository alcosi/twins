package org.twins.core.service.twinfield;

import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.twin.TwinFieldBaseEntity;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

public abstract class TwinFieldServiceBase<T extends TwinFieldBaseEntity> extends EntitySecureFindServiceImpl<T> {

    @Lazy
    @Autowired
    protected TwinService twinService;

    @Lazy
    @Autowired
    protected TwinClassFieldService twinClassFieldService;

    @Override
    public Function<T, UUID> entityGetIdFunction() {
        return TwinFieldBaseEntity::getId;
    }

    public void loadTwin(T entity) throws ServiceException {
        loadTwin(Collections.singletonList(entity));
    }

    public void loadTwin(Collection<T> srcCollection) throws ServiceException {
        twinService.load(srcCollection,
                TwinFieldBaseEntity::getTwinId,
                TwinFieldBaseEntity::getTwin,
                TwinFieldBaseEntity::setTwin);
    }

    public void loadTwinClassField(T entity) throws ServiceException {
        loadTwinClassField(Collections.singletonList(entity));
    }

    public void loadTwinClassField(Collection<T> srcCollection) throws ServiceException {
        twinClassFieldService.load(srcCollection,
                TwinFieldBaseEntity::getTwinClassFieldId,
                TwinFieldBaseEntity::getTwinClassField,
                TwinFieldBaseEntity::setTwinClassField);
    }
}
