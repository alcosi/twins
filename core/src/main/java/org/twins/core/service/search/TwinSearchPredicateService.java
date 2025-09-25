package org.twins.core.service.search;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.search.*;

import java.util.*;
import java.util.function.Function;

@Service
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

    public void loadPredicates(TwinSearchEntity entity) throws ServiceException {
        loadPredicates(List.of(entity));
    }

    public void loadPredicates(Collection<TwinSearchEntity> entities) throws ServiceException {
        Kit<TwinSearchEntity, UUID> needLoad = new Kit<>(TwinSearchEntity::getId);
        for (TwinSearchEntity entity : entities)
            if (entity.getSortKit() == null)
                needLoad.add(entity);

        if (needLoad.isEmpty()) return;

        KitGrouped<TwinSearchPredicateEntity, UUID, UUID> predicatesKit = new KitGrouped<>(
                twinSearchPredicateRepository.findByTwinSearchIdIn(needLoad.getIdSet()),
                TwinSearchPredicateEntity::getId,
                TwinSearchPredicateEntity::getTwinSearchId);

        for (Map.Entry<UUID, TwinSearchEntity> entry : needLoad.getMap().entrySet()) {
            List<TwinSearchPredicateEntity> grouped = predicatesKit.getGrouped(entry.getKey());
            entry.getValue().setSearchPredicateKit(new Kit<>(grouped, TwinSearchPredicateEntity::getId));
        }
    }

}
