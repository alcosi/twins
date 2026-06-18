package org.twins.core.service.twinflow;

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
import org.twins.core.dao.twinflow.TwinflowRepository;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaRepository;
import org.twins.core.service.user.UserService;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinflowSchemaService  extends EntitySecureFindServiceImpl<TwinflowSchemaEntity> {
    private final TwinflowRepository twinflowRepository;
    private final TwinflowSchemaRepository twinflowSchemaRepository;
    private final UserService userService;

    @Override
    public CrudRepository<TwinflowSchemaEntity, UUID> entityRepository() {return twinflowSchemaRepository;}

    @Override
    public Function<TwinflowSchemaEntity, UUID> entityGetIdFunction() {return TwinflowSchemaEntity::getId;}

    @Override
    public boolean isEntityReadDenied(TwinflowSchemaEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {return false;}

    @Override
    public boolean validateEntity(TwinflowSchemaEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadCreatedByUser(TwinflowSchemaEntity entity) throws ServiceException {
        loadCreatedByUser(Collections.singletonList(entity));
    }

    public void loadCreatedByUser(Collection<TwinflowSchemaEntity> entities) throws ServiceException {
        userService.load(entities,
                TwinflowSchemaEntity::getCreatedByUserId,
                TwinflowSchemaEntity::getCreatedByUser,
                TwinflowSchemaEntity::setCreatedByUser);
    }
}
