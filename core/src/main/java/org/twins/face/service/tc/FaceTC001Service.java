package org.twins.face.service.tc;

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
import org.twins.core.service.face.FaceService;
import org.twins.core.service.face.FaceVariantsService;
import org.twins.face.dao.tc.tc001.FaceTC001Entity;
import org.twins.face.dao.tc.tc001.FaceTC001OptionEntity;
import org.twins.face.dao.tc.tc001.FaceTC001OptionRepository;
import org.twins.face.dao.tc.tc001.FaceTC001Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTC001Service extends FaceVariantsService<FaceTC001Entity> {
    private final FaceTC001Repository faceTC001Repository;
    private final FaceService faceService;
    private final FaceTC001OptionRepository faceTC001OptionRepository;

    @Override
    public CrudRepository<FaceTC001Entity, UUID> entityRepository() {
        return faceTC001Repository;
    }

    @Override
    public Function<FaceTC001Entity, UUID> entityGetIdFunction() {
        return FaceTC001Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTC001Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FaceTC001Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceTC001Entity> getVariants(UUID of) {
        return faceTC001Repository.findByFaceId(of);
    }

    public void loadOptions(FaceTC001Entity src) {
        loadOptions(Collections.singletonList(src));
    }

    public void loadOptions(Collection<FaceTC001Entity> srcList) {
        if (CollectionUtils.isEmpty(srcList)) {
            return;
        }

        Kit<FaceTC001Entity, UUID> needLoad = new Kit<>(FaceTC001Entity::getId);

        for (FaceTC001Entity entity : srcList ) {
            if (entity.getOptions() == null){
                entity.setOptions(new Kit<>(FaceTC001OptionEntity::getId));
                needLoad.add(entity);
            }
        }
        if (needLoad.isEmpty()) {
            return;
        }

        KitGrouped<FaceTC001OptionEntity, UUID, UUID> loadedKit = new KitGrouped<>(
                faceTC001OptionRepository.findByFaceTC001IdIn(needLoad.getIdSet()), FaceTC001OptionEntity::getId, FaceTC001OptionEntity::getFaceTC001Id);
        for (var entry : loadedKit.getGroupedMap().entrySet()) {
            needLoad.get(entry.getKey()).getOptions().addAll(entry.getValue());
        }
    }
}
