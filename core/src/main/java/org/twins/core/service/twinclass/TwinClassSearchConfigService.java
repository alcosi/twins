package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassSearchEntity;
import org.twins.core.dao.twinclass.TwinClassSearchRepository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassSearchConfigService extends EntitySecureFindServiceImpl<TwinClassSearchEntity> {
    private final TwinClassSearchRepository classSearchRepository;

    @Override
    public CrudRepository<TwinClassSearchEntity, UUID> entityRepository() {
        return classSearchRepository;
    }

    @Override
    public Function<TwinClassSearchEntity, UUID> entityGetIdFunction() {
        return TwinClassSearchEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassSearchEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return checkDomainAccessDenied(entity.getDomainId(), entity.logNormal(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(TwinClassSearchEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
