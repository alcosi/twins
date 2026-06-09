package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassFieldSearchEntity;
import org.twins.core.dao.twinclass.TwinClassFieldSearchRepository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassFieldSearchConfigService extends EntitySecureFindServiceImpl<TwinClassFieldSearchEntity> {
    private final TwinClassFieldSearchRepository fieldSearchRepository;

    @Override
    public CrudRepository<TwinClassFieldSearchEntity, UUID> entityRepository() {
        return fieldSearchRepository;
    }

    @Override
    public Function<TwinClassFieldSearchEntity, UUID> entityGetIdFunction() {
        return TwinClassFieldSearchEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFieldSearchEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return checkDomainAccessDenied(entity.getDomainId(), entity.logNormal(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(TwinClassFieldSearchEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
