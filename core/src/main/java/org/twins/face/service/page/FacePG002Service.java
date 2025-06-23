package org.twins.face.service.page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FaceService;
import org.twins.core.service.face.FaceVariantsService;
import org.twins.face.dao.page.pg002.FacePG002Entity;
import org.twins.face.dao.page.pg002.FacePG002Repository;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FacePG002Service extends FaceVariantsService<FacePG002Entity> {
    private final FacePG002Repository facePG001Repository;
    private final FaceService faceService;

    @Override
    public CrudRepository<FacePG002Entity, UUID> entityRepository() {
        return facePG001Repository;
    }

    @Override
    public Function<FacePG002Entity, UUID> entityGetIdFunction() {
        return FacePG002Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FacePG002Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FacePG002Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FacePG002Entity> getVariants(UUID of) {
        return facePG001Repository.findByFaceId(of);
    }
}
