package org.twins.core.service.twinclass;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassDynamicMarkerEntity;
import org.twins.core.dao.twinclass.TwinClassDynamicMarkerRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassDynamicMarkerService extends EntitySecureFindServiceImpl<TwinClassDynamicMarkerEntity> {
    private final TwinClassDynamicMarkerRepository twinClassDynamicMarkerRepository;
    private final AuthService authService;

    @Override
    public CrudRepository<TwinClassDynamicMarkerEntity, UUID> entityRepository() {
        return twinClassDynamicMarkerRepository;
    }


    @Override
    public Function<TwinClassDynamicMarkerEntity, UUID> entityGetIdFunction() {
        return TwinClassDynamicMarkerEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassDynamicMarkerEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return entity.getTwinClass().getDomainId() != null && !entity.getTwinClass().getDomainId().equals(authService.getApiUser().getDomainId());
    }

    @Override
    public boolean validateEntity(TwinClassDynamicMarkerEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public List<TwinClassDynamicMarkerEntity> findByTwinClassIdIn(Collection<UUID> twinClassIds) {
        return twinClassDynamicMarkerRepository.findByTwinClassIdIn(twinClassIds);
    }
}
