package org.twins.face.service.page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FaceService;
import org.twins.face.dao.page.pg001.FacePG001Entity;
import org.twins.face.dao.page.pg001.FacePG001Repository;
import org.twins.face.dao.page.pg001.FacePG001WidgetEntity;
import org.twins.face.dao.page.pg001.FacePG001WidgetRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FacePG001Service extends EntitySecureFindServiceImpl<FacePG001Entity> {
    private final FacePG001Repository facePG001Repository;
    private final FacePG001WidgetRepository facePG001WidgetRepository;
    private final FaceService faceService;

    @Override
    public CrudRepository<FacePG001Entity, UUID> entityRepository() {
        return facePG001Repository;
    }

    @Override
    public Function<FacePG001Entity, UUID> entityGetIdFunction() {
        return FacePG001Entity::getFaceId;
    }

    @Override
    public boolean isEntityReadDenied(FacePG001Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FacePG001Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadWidgets(FacePG001Entity src) {
        loadWidgets(Collections.singletonList(src));
    }

    public void loadWidgets(Collection<FacePG001Entity> srcList) {
        if (CollectionUtils.isEmpty(srcList))
            return;
        Kit<FacePG001Entity, UUID> needLoad = new Kit<>(FacePG001Entity::getFaceId);
        for (var facePG001Entity : srcList)
            if (facePG001Entity.getWidgets() == null) {
                facePG001Entity.setWidgets(new Kit<>(FacePG001WidgetEntity::getId));
                needLoad.add(facePG001Entity);
            }
        if (needLoad.isEmpty())
            return;
        KitGrouped<FacePG001WidgetEntity, UUID, UUID> loadedKit = new KitGrouped<>(
                facePG001WidgetRepository.findByFaceIdIn(needLoad.getIdSet()), FacePG001WidgetEntity::getId, FacePG001WidgetEntity::getFaceId);
        for (var entry : loadedKit.getGroupedMap().entrySet()) {
            needLoad.get(entry.getKey()).getWidgets().addAll(entry.getValue());
        }
    }
}
