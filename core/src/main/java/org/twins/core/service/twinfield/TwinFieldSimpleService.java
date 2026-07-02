package org.twins.core.service.twinfield;

import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldSimpleNoRelationsProjection;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.domain.ApiUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@Lazy
public class TwinFieldSimpleService extends TwinFieldServiceBase<TwinFieldSimpleEntity> {
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;

    public TwinFieldSimpleService(TwinFieldSimpleRepository twinFieldSimpleRepository) {
        this.twinFieldSimpleRepository = twinFieldSimpleRepository;
    }

    @Override
    public CrudRepository<TwinFieldSimpleEntity, UUID> entityRepository() {
        return twinFieldSimpleRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinFieldSimpleEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinFieldSimpleEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public List<TwinFieldSimpleNoRelationsProjection> findTwinFieldsSimple(Collection<UUID> headerTwinIdList, Collection<UUID> twinIdExcludeList, Collection<UUID> statusIdList) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if(null == apiUser.getDomainId() || null == headerTwinIdList || null == statusIdList || null == twinIdExcludeList)
            return new ArrayList<>();
        return twinFieldSimpleRepository.findTwinFieldSimpleEntityProjected(apiUser.getDomainId(), headerTwinIdList, twinIdExcludeList, statusIdList);
    }
}
