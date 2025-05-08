package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.dao.twinclass.TwinClassSchemaEntity;
import org.twins.core.dao.twinclass.TwinClassSchemaRepository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class TwinClassSchemaService extends EntitySecureFindServiceImpl<TwinClassSchemaEntity> {
    private final TwinClassSchemaRepository twinClassSchemaRepository;

    @Override
    public CrudRepository<TwinClassSchemaEntity, UUID> entityRepository() {return twinClassSchemaRepository;}

    @Override
    public Function<TwinClassSchemaEntity, UUID> entityGetIdFunction() {return TwinClassSchemaEntity::getId;}

    @Override
    public boolean isEntityReadDenied(TwinClassSchemaEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {return false;}

    @Override
    public boolean validateEntity(TwinClassSchemaEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}