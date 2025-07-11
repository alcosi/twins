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
import org.twins.face.dao.tc.tc002.FaceTC002Entity;
import org.twins.face.dao.tc.tc002.FaceTC002OptionEntity;
import org.twins.face.dao.tc.tc002.FaceTC002OptionRepository;
import org.twins.face.dao.tc.tc002.FaceTC002Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTC002Service extends FaceVariantsService<FaceTC002Entity> {
    private final FaceTC002Repository faceTC002Repository;
    private final FaceService faceService;
    private final FaceTC002OptionRepository faceTC002OptionRepository;

    @Override
    public CrudRepository<FaceTC002Entity, UUID> entityRepository() {
        return faceTC002Repository;
    }

    @Override
    public Function<FaceTC002Entity, UUID> entityGetIdFunction() {
        return FaceTC002Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTC002Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FaceTC002Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceTC002Entity> getVariants(UUID of) {
        return faceTC002Repository.findByFaceId(of);
    }

    public void loadOptions(FaceTC002Entity src) {
        loadOptions(Collections.singletonList(src));
    }

    public void loadOptions(Collection<FaceTC002Entity> srcList) {
        if (CollectionUtils.isEmpty(srcList)) {
            return;
        }

        Kit<FaceTC002Entity, UUID> needLoad = new Kit<>(FaceTC002Entity::getId);

        for (FaceTC002Entity entity : srcList ) {
            if (entity.getOptions() == null){
                entity.setOptions(new Kit<>(FaceTC002OptionEntity::getId));
                needLoad.add(entity);
            }
        }
        if (needLoad.isEmpty()) {
            return;
        }

        KitGrouped<FaceTC002OptionEntity, UUID, UUID> loadedKit = new KitGrouped<>(
                faceTC002OptionRepository.findByFaceTC002IdIn(needLoad.getIdSet()), FaceTC002OptionEntity::getId, FaceTC002OptionEntity::getFaceTC002Id);
        for (var entry : loadedKit.getGroupedMap().entrySet()) {
            needLoad.get(entry.getKey()).getOptions().addAll(entry.getValue());
        }
    }
}
