package org.twins.face.service.twidget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FacePointedService;
import org.twins.core.service.face.FaceService;
import org.twins.face.dao.twidget.tw006.FaceTW006ActionEntity;
import org.twins.face.dao.twidget.tw006.FaceTW006ActionRepository;
import org.twins.face.dao.twidget.tw006.FaceTW006Entity;
import org.twins.face.dao.twidget.tw006.FaceTW006Repository;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTW006Service extends FacePointedService<FaceTW006Entity> {
    private final FaceTW006Repository faceTW006Repository;
    private final FaceTW006ActionRepository faceTW006ActionRepository;
    private final FaceService faceService;

    @Override
    public CrudRepository<FaceTW006Entity, UUID> entityRepository() {
        return faceTW006Repository;
    }

    @Override
    public Function<FaceTW006Entity, UUID> entityGetIdFunction() {
        return FaceTW006Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTW006Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FaceTW006Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceTW006Entity> getVariants(UUID faceId) {
        return faceTW006Repository.findByFaceId(faceId);
    }

    public void loadActions(FaceTW006Entity entity) {
        loadActions(Collections.singletonList(entity));
    }

    public void loadActions(Collection<FaceTW006Entity> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }

        Kit<FaceTW006Entity, UUID> needLoad = new Kit<>(FaceTW006Entity::getId);
        for (var entity : entities) {
            if (entity.getActions() == null) {
                entity.setActions(new Kit<>(FaceTW006ActionEntity::getId));
                needLoad.add(entity);
            }
        }

        if (needLoad.isEmpty()) {
            return;
        }

        List<FaceTW006ActionEntity> actions = faceTW006ActionRepository.findByFaceTW006IdIn(needLoad.getIdSet());
        KitGrouped<FaceTW006ActionEntity, UUID, UUID> actionsGrouped = new KitGrouped<>(
                actions, FaceTW006ActionEntity::getId, FaceTW006ActionEntity::getFaceTW006Id
        );

        for (var entry : actionsGrouped.getGroupedMap().entrySet()) {
           needLoad.get(entry.getKey()).getActions().addAll(entry.getValue());
        }
    }
}
