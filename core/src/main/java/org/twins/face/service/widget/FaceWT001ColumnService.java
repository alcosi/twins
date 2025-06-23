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
import org.twins.face.dao.widget.wt001.FaceWT001ColumnEntity;
import org.twins.face.dao.widget.wt001.FaceWT001ColumnRepository;
import org.twins.face.dao.widget.wt001.FaceWT001Entity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT001ColumnService extends FaceVariantsService<FaceWT001ColumnEntity> {
    private final FaceWT001ColumnRepository faceWT001ColumnRepository;

    @Override
    public CrudRepository<FaceWT001ColumnEntity, UUID> entityRepository() {
        return faceWT001ColumnRepository;
    }

    @Override
    public Function<FaceWT001ColumnEntity, UUID> entityGetIdFunction() {
        return FaceWT001ColumnEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT001ColumnEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FaceWT001ColumnEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceWT001ColumnEntity> getVariants(UUID faceWT001Id) {
        return faceWT001ColumnRepository.findByFaceWT001Id(faceWT001Id);
    }

    public void loadColumns(FaceWT001Entity src) {
        loadColumns(Collections.singletonList(src));
    }

    public void loadColumns(Collection<FaceWT001Entity> srcList) {
        if (CollectionUtils.isEmpty(srcList))
            return;
        Kit<FaceWT001Entity, UUID> needLoad = new Kit<>(FaceWT001Entity::getId);
        for (var faceWT001Entity : srcList)
            if (faceWT001Entity.getColumns() == null) {
                faceWT001Entity.setColumns(new Kit<>(FaceWT001ColumnEntity::getId));
                needLoad.add(faceWT001Entity);
            }
        if (needLoad.isEmpty())
            return;
        KitGrouped<FaceWT001ColumnEntity, UUID, UUID> loadedKit = new KitGrouped<>(
                faceWT001ColumnRepository.findByFaceWT001IdIn(needLoad.getIdSet()), FaceWT001ColumnEntity::getId, FaceWT001ColumnEntity::getFaceWT001Id);
        for (var entry : loadedKit.getGroupedMap().entrySet()) {
            needLoad.get(entry.getKey()).getColumns().addAll(entry.getValue());
        }
    }
}
