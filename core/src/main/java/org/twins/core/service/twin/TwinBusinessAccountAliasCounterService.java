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
import org.twins.core.dao.twin.TwinBusinessAccountAliasCounterEntity;
import org.twins.core.dao.twin.TwinBusinessAccountAliasCounterRepository;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.businessaccount.BusinessAccountService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinBusinessAccountAliasCounterService extends TwinsEntitySecureFindService<TwinBusinessAccountAliasCounterEntity> {
    @Getter
    private final TwinBusinessAccountAliasCounterRepository repository;
    @Lazy
    private final BusinessAccountService businessAccountService;
    @Lazy
    private final TwinClassService twinClassService;

    @Override
    public CrudRepository<TwinBusinessAccountAliasCounterEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinBusinessAccountAliasCounterEntity, UUID> entityGetIdFunction() {
        return TwinBusinessAccountAliasCounterEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinBusinessAccountAliasCounterEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinBusinessAccountAliasCounterEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadBusinessAccount(TwinBusinessAccountAliasCounterEntity src) throws ServiceException {
        loadBusinessAccount(Collections.singletonList(src));
    }

    public void loadBusinessAccount(Collection<TwinBusinessAccountAliasCounterEntity> srcCollection) throws ServiceException {
        businessAccountService.load(srcCollection,
                TwinBusinessAccountAliasCounterEntity::getBusinessAccountId,
                TwinBusinessAccountAliasCounterEntity::getBusinessAccount,
                TwinBusinessAccountAliasCounterEntity::setBusinessAccount);
    }

    public void loadTwinClass(TwinBusinessAccountAliasCounterEntity src) throws ServiceException {
        loadTwinClass(Collections.singletonList(src));
    }

    public void loadTwinClass(Collection<TwinBusinessAccountAliasCounterEntity> srcCollection) throws ServiceException {
        twinClassService.load(srcCollection,
                TwinBusinessAccountAliasCounterEntity::getTwinClassId,
                TwinBusinessAccountAliasCounterEntity::getTwinClass,
                TwinBusinessAccountAliasCounterEntity::setTwinClass);
    }
}
