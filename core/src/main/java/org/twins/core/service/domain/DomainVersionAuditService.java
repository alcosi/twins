package org.twins.core.service.domain;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainVersionAuditEntity;
import org.twins.core.dao.domain.DomainVersionAuditRepository;
import org.twins.core.service.TwinsEntityAccessService;
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
public class DomainVersionAuditService extends TwinsEntityAccessService<DomainVersionAuditEntity> {
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

    // domain_version_audit is append-only and written by the versioning triggers (TWINS-719) —
    // the whole write family is forbidden on the service
    private static final String AUDIT_WRITE_FORBIDDEN = "domain_version_audit rows are written by DB triggers only";

    @Override
    public DomainVersionAuditEntity saveSafe(DomainVersionAuditEntity entity) throws ServiceException {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED, AUDIT_WRITE_FORBIDDEN);
    }

    @Override
    public Iterable<DomainVersionAuditEntity> saveSafe(Collection<DomainVersionAuditEntity> entities) throws ServiceException {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED, AUDIT_WRITE_FORBIDDEN);
    }

    @Override
    public DomainVersionAuditEntity updateSafe(DomainVersionAuditEntity entity, ChangesHelper changesHelper) throws ServiceException {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED, AUDIT_WRITE_FORBIDDEN);
    }

    @Override
    public Iterable<DomainVersionAuditEntity> updateSafe(ChangesHelperMulti<DomainVersionAuditEntity> changesHelperMulti) throws ServiceException {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED, AUDIT_WRITE_FORBIDDEN);
    }

    @Override
    public void deleteSafe(UUID id) throws ServiceException {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED, AUDIT_WRITE_FORBIDDEN);
    }

    @Override
    public void deleteSafe(Collection<UUID> ids) throws ServiceException {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED, AUDIT_WRITE_FORBIDDEN);
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
