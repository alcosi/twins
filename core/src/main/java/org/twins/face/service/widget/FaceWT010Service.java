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
import org.twins.core.service.resource.ResourceService;
import org.twins.face.dao.widget.wt010.FaceWT010Entity;
import org.twins.face.dao.widget.wt010.FaceWT010Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT010Service extends FacePointedService<FaceWT010Entity> {

    private final FaceWT010Repository faceWT010Repository;
    private final FaceService faceService;
    private final ResourceService resourceService;


    @Override
    public List<FaceWT010Entity> getVariants(UUID of) {
        return faceWT010Repository.findByFaceId(of);
    }

    @Override
    public CrudRepository<FaceWT010Entity, UUID> entityRepository() {
        return faceWT010Repository;
    }

    @Override
    public Function<FaceWT010Entity, UUID> entityGetIdFunction() {
        return FaceWT010Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT010Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        if (entity.getFace() == null)
            entity.setFace(findFaceForReadCheck(entity.getFaceId()));
        return faceService.isEntityReadDenied(entity.getFace(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(FaceWT010Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadIconResources(FaceWT010Entity src) throws ServiceException {
        loadIconResources(Collections.singletonList(src));
    }

    public void loadIconResources(Collection<FaceWT010Entity> srcCollection) throws ServiceException {
        resourceService.load(srcCollection,
                FaceWT010Entity::getIconResourceId,
                FaceWT010Entity::getIconResource,
                FaceWT010Entity::setIconResource);
    }
}
