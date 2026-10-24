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
import org.twins.core.dao.DomainConfigEntity;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.dao.domain.DomainVersionRepository;
import org.twins.core.enums.domain.DomainVersionStatus;
import org.twins.core.service.TwinsEntityAccessService;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class DomainVersionService extends TwinsEntityAccessService<DomainVersionEntity> {
    private final DomainVersionRepository domainVersionRepository;

    @Override
    public CrudRepository<DomainVersionEntity, UUID> entityRepository() {
        return domainVersionRepository;
    }

    @Override
    public Function<DomainVersionEntity, UUID> entityGetIdFunction() {
        return DomainVersionEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(DomainVersionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return checkDomainAccessDenied(entity.getDomainId(), entity.logNormal(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(DomainVersionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getDomainId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty domainId");
        if (entity.getVersion() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty version");
        if (entity.getStatus() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty status");
        return true;
    }

    /**
     * The single OPEN (draft) window of the domain — at most one exists per domain
     * (partial unique index {@code domain_version_one_open_per_domain}).
     */
    public Optional<DomainVersionEntity> findOpenVersion(UUID domainId) {
        return domainVersionRepository.findByDomainIdAndStatus(domainId, DomainVersionStatus.OPEN);
    }

    /**
     * Shared Variant-A loader (see docs/load_method_pattern.md) for the {@code domainVersion}
     * runtime field declared on {@link DomainConfigEntity}: any versioned config entity
     * (twin_class, data_list, …) can be passed in. Parent services should expose their own
     * {@code loadDomainVersion} wrapper delegating here when a mapper needs the field.
     */
    public <E extends DomainConfigEntity> void loadDomainVersion(Collection<E> srcCollection) throws ServiceException {
        load(srcCollection,
                DomainConfigEntity::getDomainVersionId,
                DomainConfigEntity::getDomainVersion,
                DomainConfigEntity::setDomainVersion);
    }
}
