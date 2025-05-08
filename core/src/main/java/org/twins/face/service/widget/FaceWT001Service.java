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
import org.twins.core.service.face.FaceService;
import org.twins.face.dao.widget.wt001.FaceWT001ColumnEntity;
import org.twins.face.dao.widget.wt001.FaceWT001ColumnRepository;
import org.twins.face.dao.widget.wt001.FaceWT001Entity;
import org.twins.face.dao.widget.wt001.FaceWT001Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT001Service extends EntitySecureFindServiceImpl<FaceWT001Entity> {
    private final FaceWT001Repository faceWT001Repository;
    private final FaceWT001ColumnRepository faceWT001ColumnRepository;
    private final FaceService faceService;

    @Override
    public CrudRepository<FaceWT001Entity, UUID> entityRepository() {
        return faceWT001Repository;
    }

    @Override
    public Function<FaceWT001Entity, UUID> entityGetIdFunction() {
        return FaceWT001Entity::getFaceId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT001Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FaceWT001Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadColumns(FaceWT001Entity src) {
        loadColumns(Collections.singletonList(src));
    }

    public void loadColumns(Collection<FaceWT001Entity> srcList) {
        if (CollectionUtils.isEmpty(srcList))
            return;
        Kit<FaceWT001Entity, UUID> needLoad = new Kit<>(FaceWT001Entity::getFaceId);
        for (var faceWT001Entity : srcList)
            if (faceWT001Entity.getColumns() == null) {
                faceWT001Entity.setColumns(new Kit<>(FaceWT001ColumnEntity::getId));
                needLoad.add(faceWT001Entity);
            }
        if (needLoad.isEmpty())
            return;
        KitGrouped<FaceWT001ColumnEntity, UUID, UUID> loadedKit = new KitGrouped<>(
                faceWT001ColumnRepository.findByFaceIdIn(needLoad.getIdSet()), FaceWT001ColumnEntity::getId, FaceWT001ColumnEntity::getFaceId);
        for (var entry : loadedKit.getGroupedMap().entrySet()) {
            needLoad.get(entry.getKey()).getColumns().addAll(entry.getValue());
        }
    }
}
