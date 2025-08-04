package org.twins.face.service.widget;

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
import org.twins.core.service.face.FaceVariantsService;
import org.twins.face.dao.widget.wt002.FaceWT002ButtonEntity;
import org.twins.face.dao.widget.wt002.FaceWT002ButtonRepository;
import org.twins.face.dao.widget.wt002.FaceWT002Entity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT002ButtonService extends FaceVariantsService<FaceWT002ButtonEntity> {
    private final FaceWT002ButtonRepository faceWT002ButtonRepository;

    @Override
    public CrudRepository<FaceWT002ButtonEntity, UUID> entityRepository() {
        return faceWT002ButtonRepository;
    }

    @Override
    public Function<FaceWT002ButtonEntity, UUID> entityGetIdFunction() {
        return FaceWT002ButtonEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT002ButtonEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FaceWT002ButtonEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadButtons(FaceWT002Entity src) {
        loadButtons(Collections.singletonList(src));
    }

    public void loadButtons(Collection<FaceWT002Entity> srcList) {
        if (CollectionUtils.isEmpty(srcList))
            return;
        Kit<FaceWT002Entity, UUID> needLoad = new Kit<>(FaceWT002Entity::getId);

        for (var faceWT002Entity : srcList)
            if (faceWT002Entity.getButtons() == null) {
                faceWT002Entity.setButtons(new Kit<>(FaceWT002ButtonEntity::getId));
                needLoad.add(faceWT002Entity);
            }

        if (needLoad.isEmpty())
            return;
        KitGrouped<FaceWT002ButtonEntity, UUID, UUID> loadedKit = new KitGrouped<>(
                faceWT002ButtonRepository.findByFaceWT002IdIn(needLoad.getIdSet()), FaceWT002ButtonEntity::getId, FaceWT002ButtonEntity::getFaceWT002Id);
        for (var entry : loadedKit.getGroupedMap().entrySet()) {
            needLoad.get(entry.getKey()).getButtons().addAll(entry.getValue());
        }
    }

    @Override
    public List<FaceWT002ButtonEntity> getVariants(UUID faceWT002Id) {
        return faceWT002ButtonRepository.findByFaceWT002Id(faceWT002Id);
    }
}