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
import org.twins.face.dao.widget.wt009.FaceWT009ActionEntity;
import org.twins.face.dao.widget.wt009.FaceWT009ActionRepository;
import org.twins.face.dao.widget.wt009.FaceWT009Entity;
import org.twins.face.dao.widget.wt009.FaceWT009Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT009Service extends FacePointedService<FaceWT009Entity> {
    private final FaceWT009Repository faceWT009Repository;
    private final FaceWT009ActionRepository faceWT009ActionRepository;
    private final FaceService faceService;

    @Override
    public CrudRepository<FaceWT009Entity, UUID> entityRepository() {
        return faceWT009Repository;
    }

    @Override
    public Function<FaceWT009Entity, UUID> entityGetIdFunction() {
        return FaceWT009Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT009Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        if (entity.getFace() == null)
            entity.setFace(findFaceForReadCheck(entity.getFaceId()));
        return faceService.isEntityReadDenied(entity.getFace(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(FaceWT009Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceWT009Entity> getVariants(UUID faceId) {
        return faceWT009Repository.findByFaceId(faceId);
    }

    public void loadActions(FaceWT009Entity entity) {
        loadActions(Collections.singletonList(entity));
    }

    public void loadActions(Collection<FaceWT009Entity> entities) {
        loadKit(entities,
                FaceWT009Entity::getId,
                FaceWT009Entity::getActions,
                FaceWT009Entity::setActions,
                faceWT009ActionRepository::findByFaceWT009IdIn,
                FaceWT009ActionEntity::getId,
                FaceWT009ActionEntity::getFaceWT009Id,
                FaceWT009ActionEntity::setFaceWT009);
    }
}
