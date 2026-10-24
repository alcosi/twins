package org.twins.core.service.domain;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainVersionAuditEntity;
import org.twins.core.dao.domain.DomainVersionAuditRepository;
import org.twins.core.service.TwinsEntitySecureFindService;
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
public class DomainVersionAuditService extends TwinsEntitySecureFindService<DomainVersionAuditEntity> {
    private final DomainVersionAuditRepository domainVersionAuditRepository;
    private final UserService userService;
    private final DomainVersionService domainVersionService;

    @Override
    public CrudRepository<DomainVersionAuditEntity, UUID> entityRepository() {
        return domainVersionAuditRepository;
    }

    @Override
    public Function<DomainVersionAuditEntity, UUID> entityGetIdFunction() {
        return DomainVersionAuditEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(DomainVersionAuditEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return checkDomainAccessDenied(entity.getDomainId(), entity.logNormal(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(DomainVersionAuditEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getDomainId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty domainId");
        if (entity.getDomainVersionId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty domainVersionId");
        if (entity.getTableName() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty tableName");
        if (entity.getRowId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty rowId");
        if (entity.getOperation() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty operation");
        return true;
    }

    public void loadActorUser(DomainVersionAuditEntity src) throws ServiceException {
        loadActorUser(Collections.singletonList(src));
    }

    public void loadActorUser(Collection<DomainVersionAuditEntity> srcCollection) throws ServiceException {
        userService.load(srcCollection,
                DomainVersionAuditEntity::getActorUserId,
                DomainVersionAuditEntity::getActorUser,
                DomainVersionAuditEntity::setActorUser);
    }

    public void loadDomainVersion(DomainVersionAuditEntity src) throws ServiceException {
        loadDomainVersion(Collections.singletonList(src));
    }

    public void loadDomainVersion(Collection<DomainVersionAuditEntity> srcCollection) throws ServiceException {
        domainVersionService.load(srcCollection,
                DomainVersionAuditEntity::getDomainVersionId,
                DomainVersionAuditEntity::getDomainVersion,
                DomainVersionAuditEntity::setDomainVersion);
    }
}
