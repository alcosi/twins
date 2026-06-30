package org.twins.core.service.eraseflow;

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
import org.twins.core.dao.eraseflow.EraseflowLinkCascadeEntity;
import org.twins.core.dao.eraseflow.EraseflowLinkCascadeRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class EraseflowLinkCascadeService extends EntitySecureFindServiceImpl<EraseflowLinkCascadeEntity> {
    private final EraseflowLinkCascadeRepository eraseflowLinkCascadeRepository;
    @Lazy
    private final EraseflowService eraseflowService;

    @Override
    public CrudRepository<EraseflowLinkCascadeEntity, UUID> entityRepository() {
        return eraseflowLinkCascadeRepository;
    }

    @Override
    public Function<EraseflowLinkCascadeEntity, UUID> entityGetIdFunction() {
        return EraseflowLinkCascadeEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(EraseflowLinkCascadeEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(EraseflowLinkCascadeEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadEraseflow(EraseflowLinkCascadeEntity src) throws ServiceException {
        loadEraseflow(Collections.singletonList(src));
    }

    public void loadEraseflow(Collection<EraseflowLinkCascadeEntity> srcCollection) throws ServiceException {
        eraseflowService.load(srcCollection,
                EraseflowLinkCascadeEntity::getEraseflowId,
                EraseflowLinkCascadeEntity::getEraseflow,
                EraseflowLinkCascadeEntity::setEraseflow);
    }
}
