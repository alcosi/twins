package org.twins.core.service.search;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.search.TwinSearchSortEntity;
import org.twins.core.dao.search.TwinSearchSortRepository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
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

    public void sortByOrder(List<TwinSearchSortEntity> sorts) {
        if (CollectionUtils.isNotEmpty(sorts))
            sorts.sort(Comparator.comparing(TwinSearchSortEntity::getOrder, Comparator.nullsLast(Integer::compareTo)));
    }

}
