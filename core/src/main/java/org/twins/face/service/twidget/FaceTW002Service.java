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
import org.twins.face.dao.twidget.tw002.FaceTW002Entity;
import org.twins.face.dao.twidget.tw002.FaceTW002Repository;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTW002Service extends FaceVariantsService<FaceTW002Entity> {
    private final FaceTW002Repository faceTW002Repository;
    private final FaceService faceService;

    @Override
    public CrudRepository<FaceTW002Entity, UUID> entityRepository() {
        return faceTW002Repository;
    }

    @Override
    public Function<FaceTW002Entity, UUID> entityGetIdFunction() {
        return FaceTW002Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTW002Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FaceTW002Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceTW002Entity> getVariants(UUID of) {
        return faceTW002Repository.findByFaceId(of);
    }
}