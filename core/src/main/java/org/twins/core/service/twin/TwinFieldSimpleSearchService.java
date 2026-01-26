package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldSimpleNoRelationsProjection;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinFieldSimpleSearchService extends EntitySecureFindServiceImpl<TwinFieldSimpleEntity> {

    private final AuthService authService;
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;

    @Override
    public CrudRepository<TwinFieldSimpleEntity, UUID> entityRepository() {
        return twinFieldSimpleRepository;
    }

    @Override
    public Function<TwinFieldSimpleEntity, UUID> entityGetIdFunction() {
        return TwinFieldSimpleEntity::getId;
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
