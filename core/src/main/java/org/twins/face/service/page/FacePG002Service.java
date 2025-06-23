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
import org.twins.face.dao.page.pg002.*;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FacePG002Service extends EntitySecureFindServiceImpl<FacePG002Entity> {
    private final FacePG002Repository facePG001Repository;
    private final FacePG002WidgetRepository facePG002widgetRepository;
    private final FaceService faceService;

    @Override
    public CrudRepository<FacePG002Entity, UUID> entityRepository() {
        return facePG001Repository;
    }

    @Override
    public Function<FacePG002Entity, UUID> entityGetIdFunction() {
        return FacePG002Entity::getFaceId;
    }

    @Override
    public boolean isEntityReadDenied(FacePG002Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FacePG002Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadWidgets(FacePG002TabEntity src) {
        loadWidgets(Collections.singletonList(src));
    }

    public void loadWidgets(Collection<FacePG002TabEntity> srcList) {
        if (CollectionUtils.isEmpty(srcList))
            return;
        Kit<FacePG002TabEntity, UUID> needLoad = new Kit<>(FacePG002TabEntity::getId);
        for (var facePG001Entity : srcList)
            if (facePG001Entity.getWidgets() == null) {
                facePG001Entity.setWidgets(new Kit<>(FacePG002WidgetEntity::getId));
                needLoad.add(facePG001Entity);
            }
        if (needLoad.isEmpty())
            return;
        KitGrouped<FacePG002WidgetEntity, UUID, UUID> loadedKit = new KitGrouped<>(
                facePG002widgetRepository.findByFacePagePG002TabIdInAndActiveTrue(needLoad.getIdSet()), FacePG002WidgetEntity::getId, FacePG002WidgetEntity::getFacePagePG002TabId);
        for (var entry : loadedKit.getGroupedMap().entrySet()) {
            needLoad.get(entry.getKey()).getWidgets().addAll(entry.getValue());
        }
    }
}
