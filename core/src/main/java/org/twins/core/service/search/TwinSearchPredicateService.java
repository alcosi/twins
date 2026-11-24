package org.twins.core.service.search;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.search.TwinSearchEntity;
import org.twins.core.dao.search.TwinSearchPredicateEntity;
import org.twins.core.dao.search.TwinSearchPredicateRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinSearchPredicateService extends EntitySecureFindServiceImpl<TwinSearchPredicateEntity> {

    private final TwinSearchPredicateRepository twinSearchPredicateRepository;

    @Override
    public CrudRepository<TwinSearchPredicateEntity, UUID> entityRepository() {
        return twinSearchPredicateRepository;
    }

    @Override
    public Function<TwinSearchPredicateEntity, UUID> entityGetIdFunction() {
        return TwinSearchPredicateEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinSearchPredicateEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinSearchPredicateEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadPredicates(TwinSearchEntity src) {
        loadPredicates(Collections.singletonList(src));
    }

    public void loadPredicates(Collection<TwinSearchEntity> srcList) {
        loadKit(srcList,
                TwinSearchEntity::getId,
                TwinSearchEntity::getSearchPredicateKit,
                TwinSearchEntity::setSearchPredicateKit,
                twinSearchPredicateRepository::findByTwinSearchIdIn,
                TwinSearchPredicateEntity::getId,
                TwinSearchPredicateEntity::getTwinSearchId,
                TwinSearchPredicateEntity::setSearch);
    }

}
