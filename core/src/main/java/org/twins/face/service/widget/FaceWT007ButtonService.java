package org.twins.face.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FaceVariantsService;
import org.twins.core.service.resource.ResourceService;
import org.twins.core.service.twinflow.TwinflowTransitionService;
import org.twins.face.dao.widget.wt007.FaceWT007ButtonEntity;
import org.twins.face.dao.widget.wt007.FaceWT007ButtonRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT007ButtonService extends FaceVariantsService<FaceWT007ButtonEntity> {
    private final FaceWT007ButtonRepository faceWT007ButtonRepository;
    private final ResourceService resourceService;
    private final TwinflowTransitionService twinflowTransitionService;

    @Override
    public CrudRepository<FaceWT007ButtonEntity, UUID> entityRepository() {
        return faceWT007ButtonRepository;
    }

    @Override
    public Function<FaceWT007ButtonEntity, UUID> entityGetIdFunction() {
        return FaceWT007ButtonEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT007ButtonEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FaceWT007ButtonEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceWT007ButtonEntity> getVariants(UUID of) {
        return faceWT007ButtonRepository.findByFaceWT007Id(of);
    }

    public void loadTransitions(FaceWT007ButtonEntity src) throws ServiceException {
        loadTransitions(Collections.singletonList(src));
    }

    public void loadTransitions(Collection<FaceWT007ButtonEntity> srcCollection) throws ServiceException {
        twinflowTransitionService.load(srcCollection,
                FaceWT007ButtonEntity::getTransitionId,
                FaceWT007ButtonEntity::getTransition,
                FaceWT007ButtonEntity::setTransition);
    }

    public void loadIconResources(FaceWT007ButtonEntity src) throws ServiceException {
        loadIconResources(Collections.singletonList(src));
    }

    public void loadIconResources(Collection<FaceWT007ButtonEntity> srcCollection) throws ServiceException {
        resourceService.load(srcCollection,
                FaceWT007ButtonEntity::getIconResourceId,
                FaceWT007ButtonEntity::getIconResource,
                FaceWT007ButtonEntity::setIconResource);
    }
}