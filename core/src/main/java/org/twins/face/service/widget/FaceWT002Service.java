package org.twins.face.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FaceVariantsService;
import org.twins.face.dao.widget.wt002.FaceWT002ButtonRepository;
import org.twins.face.dao.widget.wt002.FaceWT002Entity;
import org.twins.face.dao.widget.wt002.FaceWT002Repository;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT002Service extends FaceVariantsService<FaceWT002Entity> {
    private final FaceWT002Repository faceWT002Repository;
    private final FaceWT002ButtonRepository faceWT002ButtonRepository;

    @Override
    public CrudRepository<FaceWT002Entity, UUID> entityRepository() {
        return faceWT002Repository;
    }

    @Override
    public Function<FaceWT002Entity, UUID> entityGetIdFunction() {
        return FaceWT002Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT002Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FaceWT002Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceWT002Entity> getVariants(UUID of) {
        return faceWT002Repository.findByFaceId(of);
    }
}
