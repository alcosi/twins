package org.twins.face.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FaceVariantsService;
import org.twins.face.dao.widget.wt008.FaceWT008Entity;
import org.twins.face.dao.widget.wt008.FaceWT008Repository;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT008Service extends FaceVariantsService<FaceWT008Entity> {

    private final FaceWT008Repository faceWT008Repository;

    @Override
    public CrudRepository<FaceWT008Entity, UUID> entityRepository() {
        return faceWT008Repository;
    }

    @Override
    public Function<FaceWT008Entity, UUID> entityGetIdFunction() {
        return FaceWT008Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT008Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FaceWT008Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceWT008Entity> getVariants(UUID of) {
        return faceWT008Repository.findByFaceId(of);
    }
}
