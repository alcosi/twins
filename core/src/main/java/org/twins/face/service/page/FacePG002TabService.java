package org.twins.face.service.page;

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
import org.twins.face.dao.page.pg002.FacePG002Entity;
import org.twins.face.dao.page.pg002.FacePG002TabEntity;
import org.twins.face.dao.page.pg002.FacePG002TabRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FacePG002TabService extends FaceVariantsService<FacePG002TabEntity> {
    private final FacePG002TabRepository facePG002TabRepository;
    private final FaceService faceService;

    @Override
    public CrudRepository<FacePG002TabEntity, UUID> entityRepository() {
        return facePG002TabRepository;
    }

    @Override
    public Function<FacePG002TabEntity, UUID> entityGetIdFunction() {
        return FacePG002TabEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FacePG002TabEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FacePG002TabEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FacePG002TabEntity> getVariants(UUID of) {
        return facePG002TabRepository.findByFacePG002Id(of);
    }

    public void loadTabs(FacePG002Entity src) {
        loadTabs(Collections.singletonList(src));
    }

    public void loadTabs(Collection<FacePG002Entity> srcList) {
        if (CollectionUtils.isEmpty(srcList))
            return;
        Kit<FacePG002Entity, UUID> needLoad = new Kit<>(FacePG002Entity::getId);
        for (var facePG002Entity : srcList)
            if (facePG002Entity.getTabs() == null) {
                facePG002Entity.setTabs(new Kit<>(FacePG002TabEntity::getId));
                needLoad.add(facePG002Entity);
            }
        if (needLoad.isEmpty())
            return;
        KitGrouped<FacePG002TabEntity, UUID, UUID> loadedKit = new KitGrouped<>(
                facePG002TabRepository.findByFacePG002IdIn(needLoad.getIdSet()), FacePG002TabEntity::getId, FacePG002TabEntity::getFacePG002Id);
        for (var entry : loadedKit.getGroupedMap().entrySet()) {
            needLoad.get(entry.getKey()).getTabs().addAll(entry.getValue());
        }
    }
}
