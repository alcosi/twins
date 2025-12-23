package org.twins.core.service.search;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.search.TwinSearchEntity;
import org.twins.core.dao.search.TwinSearchSortEntity;
import org.twins.core.dao.search.TwinSearchSortRepository;

import java.util.*;
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

    public void sortByOrder(List<TwinSearchSortEntity> sorts) {
        if (CollectionUtils.isNotEmpty(sorts))
            sorts.sort(Comparator.comparing(TwinSearchSortEntity::getOrder, Comparator.nullsLast(Integer::compareTo)));
    }

    public void loadSorts(TwinSearchEntity entity) throws ServiceException {
        loadSorts(List.of(entity));
    }

    public void loadSorts(Collection<TwinSearchEntity> entities) throws ServiceException {
        Kit<TwinSearchEntity, UUID> needLoad = new Kit<>(TwinSearchEntity::getId);
        for (TwinSearchEntity entity : entities) {
            if (entity.getSortKit() == null) {
                needLoad.add(entity);
            }
        }

        if (needLoad.isEmpty()) {
            return;
        }

        KitGrouped<TwinSearchSortEntity, UUID, UUID> sortKit = new KitGrouped<>(
                twinSearchSortRepository.findByTwinSearchIdIn(needLoad.getIdSet()),
                TwinSearchSortEntity::getId,
                TwinSearchSortEntity::getTwinSearchId);

        for (Map.Entry<UUID, TwinSearchEntity> entry : needLoad.getMap().entrySet()) {
            List<TwinSearchSortEntity> grouped = sortKit.getGrouped(entry.getKey());
            sortByOrder(grouped);
            entry.getValue().setSortKit(new Kit<>(grouped, TwinSearchSortEntity::getId));
        }
    }

}
