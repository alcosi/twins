package org.twins.face.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FacePointedService;
import org.twins.core.service.face.FaceService;
import org.twins.face.dao.widget.wt007.FaceWT007ButtonEntity;
import org.twins.face.dao.widget.wt007.FaceWT007ButtonRepository;
import org.twins.face.dao.widget.wt007.FaceWT007Entity;
import org.twins.face.dao.widget.wt007.FaceWT007Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT007Service extends FacePointedService<FaceWT007Entity> {
    private final FaceWT007Repository faceWT007Repository;
    private final FaceWT007ButtonRepository faceWT007ButtonRepository;
    private final FaceService faceService;

    @Override
    public CrudRepository<FaceWT007Entity, UUID> entityRepository() {
        return faceWT007Repository;
    }

    @Override
    public Function<FaceWT007Entity, UUID> entityGetIdFunction() {
        return FaceWT007Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT007Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FaceWT007Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceWT007Entity> getVariants(UUID of) {
        return faceWT007Repository.findByFaceId(of);
    }

    public void loadButtons(FaceWT007Entity src) {
        loadButtons(Collections.singletonList(src));
    }

    public void loadButtons(Collection<FaceWT007Entity> srcList) {
        loadKit(srcList,
                FaceWT007Entity::getId,
                FaceWT007Entity::getButtons,
                FaceWT007Entity::setButtons,
                faceWT007ButtonRepository::findByFaceWT007IdIn,
                FaceWT007ButtonEntity::getId,
                FaceWT007ButtonEntity::getFaceWT007Id,
                FaceWT007ButtonEntity::setFaceWT007);
    }
}