package org.twins.face.service.widget;

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
import org.twins.face.dao.widget.wt002.FaceWT002ButtonEntity;
import org.twins.face.dao.widget.wt002.FaceWT002ButtonRepository;
import org.twins.face.dao.widget.wt002.FaceWT002Entity;
import org.twins.face.dao.widget.wt002.FaceWT002Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT002Service extends EntitySecureFindServiceImpl<FaceWT002Entity> {
    private final FaceWT002Repository faceWT002Repository;
    private final FaceWT002ButtonRepository faceWT002ButtonRepository;

    @Override
    public CrudRepository<FaceWT002Entity, UUID> entityRepository() {
        return faceWT002Repository;
    }

    @Override
    public Function<FaceWT002Entity, UUID> entityGetIdFunction() {
        return FaceWT002Entity::getFaceId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT002Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FaceWT002Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadButtons(FaceWT002Entity src) {
        loadButtons(Collections.singletonList(src));
    }

    public void loadButtons(Collection<FaceWT002Entity> srcList) {
        if (CollectionUtils.isEmpty(srcList))
            return;
        Kit<FaceWT002Entity, UUID> needLoad = new Kit<>(FaceWT002Entity::getFaceId);

        for (var faceWT002Entity : srcList)
            if (faceWT002Entity.getButtons() == null) {
                faceWT002Entity.setButtons(new Kit<>(FaceWT002ButtonEntity::getId));
                needLoad.add(faceWT002Entity);
            }

        if (needLoad.isEmpty())
            return;
        KitGrouped<FaceWT002ButtonEntity, UUID, UUID> loadedKit = new KitGrouped<>(
                faceWT002ButtonRepository.findByFaceIdIn(needLoad.getIdSet()), FaceWT002ButtonEntity::getId, FaceWT002ButtonEntity::getFaceId);
        for (var entry : loadedKit.getGroupedMap().entrySet()) {
            needLoad.get(entry.getKey()).getButtons().addAll(entry.getValue());
        }
    }
}
