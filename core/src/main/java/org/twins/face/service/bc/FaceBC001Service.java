package org.twins.face.service.bc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.face.FaceTwinPointerService;
import org.twins.core.service.face.FaceVariantsService;
import org.twins.face.dao.bc.FaceBC001Entity;
import org.twins.face.dao.bc.FaceBC001ItemEntity;
import org.twins.face.dao.bc.FaceBC001ItemRepository;
import org.twins.face.dao.bc.FaceBC001Repository;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceBC001Service extends FaceVariantsService<FaceBC001Entity> {

    private final FaceBC001Repository faceBC001Repository;
    private final FaceBC001ItemRepository faceBC001ItemRepository;
    private final FaceTwinPointerService faceTwinPointerService;

    @Override
    public CrudRepository<FaceBC001Entity, UUID> entityRepository() {
        return faceBC001Repository;
    }

    @Override
    public Function<FaceBC001Entity, UUID> entityGetIdFunction() {
        return FaceBC001Entity::getFaceId;
    }

    @Override
    public boolean isEntityReadDenied(FaceBC001Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }


    @Override
    public List<FaceBC001Entity> getVariants(UUID faceId) {
        return faceBC001Repository.findByFaceId(faceId);
    }

    @Override
    public boolean validateEntity(FaceBC001Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return false;
    }

    public void loadBreadCrumbsItems(FaceBC001Entity src) {
        loadBreadCrumbsItems(Collections.singletonList(src));
    }

    public void loadBreadCrumbsItems(Collection<FaceBC001Entity> srcList) {
        if (CollectionUtils.isEmpty(srcList)) {
            return;
        }

        Kit<FaceBC001Entity, UUID> needLoad = new Kit<>(FaceBC001Entity::getId);
        for (var faceBC001Entity : srcList) {
            if (faceBC001Entity.getItems() == null) {
                faceBC001Entity.setItems(new Kit<>(FaceBC001ItemEntity::getId));
                needLoad.add(faceBC001Entity);
            }
        }

        if (needLoad.isEmpty()) {
            return;
        }

        KitGrouped<FaceBC001ItemEntity, UUID, UUID> loadedKit = new KitGrouped<>(
                faceBC001ItemRepository.findAllByFaceBC001IdIn(needLoad.getIdSet()), FaceBC001ItemEntity::getId, FaceBC001ItemEntity::getFaceBC001Id);

        for (var entry : loadedKit.getGroupedMap().entrySet()) {
            needLoad.get(entry.getKey()).getItems().addAll(entry.getValue());
        }
    }

    public List<Pair<FaceBC001ItemEntity, TwinEntity>> getBC001ItemToTwinPairs(FaceBC001Entity entity) throws ServiceException {
        loadBreadCrumbsItems(entity);

        List<Pair<FaceBC001ItemEntity, TwinEntity>> ret = new ArrayList<>();
        for (var i : entity.getItems()) {
            ret.add(Pair.of(i, faceTwinPointerService.getPointer(i.getTwinPointerId())));
        }

        return ret;
    }
}
