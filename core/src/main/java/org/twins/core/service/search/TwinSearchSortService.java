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
import org.twins.core.dao.search.TwinSearchSortEntity;
import org.twins.core.dao.search.TwinSearchSortRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinSearchSortService extends EntitySecureFindServiceImpl<TwinSearchSortEntity> {

    private final TwinSearchSortRepository twinSearchSortRepository;

    @Override
    public CrudRepository<TwinSearchSortEntity, UUID> entityRepository() {
        return twinSearchSortRepository;
    }

    @Override
    public Function<TwinSearchSortEntity, UUID> entityGetIdFunction() {
        return TwinSearchSortEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinSearchSortEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinSearchSortEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadSorts(TwinSearchEntity src) {
        loadSorts(Collections.singletonList(src));
    }

    /**
     * Fills sortKit per search in one query. The repository orders by {@code order} ASC (nulls last),
     * and loadKit preserves that encounter order inside each per-search kit (Kit is LinkedHashMap-backed).
     */
    public void loadSorts(Collection<TwinSearchEntity> srcList) {
        loadKit(srcList,
                TwinSearchEntity::getId,
                TwinSearchEntity::getSortKit,
                TwinSearchEntity::setSortKit,
                twinSearchSortRepository::findByTwinSearchIdInOrderByOrderAsc,
                TwinSearchSortEntity::getId,
                TwinSearchSortEntity::getTwinSearchId,
                TwinSearchSortEntity::setSearch);
    }

}
