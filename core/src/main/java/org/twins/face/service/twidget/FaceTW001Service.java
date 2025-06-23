package org.twins.face.service.twidget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FaceService;
import org.twins.core.service.face.FaceVariantsService;
import org.twins.face.dao.twidget.tw001.FaceTW001Entity;
import org.twins.face.dao.twidget.tw001.FaceTW001Repository;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTW001Service extends FaceVariantsService<FaceTW001Entity> {
    private final FaceTW001Repository faceTW001Repository;
    private final FaceService faceService;

    @Override
    public CrudRepository<FaceTW001Entity, UUID> entityRepository() {
        return faceTW001Repository;
    }

    @Override
    public Function<FaceTW001Entity, UUID> entityGetIdFunction() {
        return FaceTW001Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTW001Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FaceTW001Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceTW001Entity> getVariants(UUID of) {
        return faceTW001Repository.findByFaceId(of);
    }
}