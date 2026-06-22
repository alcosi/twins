package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinStatusGroupEntity;
import org.twins.core.dao.twin.TwinStatusGroupRepository;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.domain.DomainService;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinStatusGroupService extends TwinsEntitySecureFindService<TwinStatusGroupEntity> {
    @Getter
    private final TwinStatusGroupRepository repository;
    @Lazy
    private final DomainService domainService;

    @Override
    public CrudRepository<TwinStatusGroupEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinStatusGroupEntity, UUID> entityGetIdFunction() {
        return TwinStatusGroupEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinStatusGroupEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinStatusGroupEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadDomain(TwinStatusGroupEntity src) throws ServiceException {
        loadDomain(Collections.singletonList(src));
    }

    public void loadDomain(Collection<TwinStatusGroupEntity> srcCollection) throws ServiceException {
        domainService.load(srcCollection,
                TwinStatusGroupEntity::getDomainId,
                TwinStatusGroupEntity::getDomain,
                TwinStatusGroupEntity::setDomain);
    }
}
