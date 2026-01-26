package org.twins.face.service.twidget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FacePointedService;
import org.twins.core.service.face.FaceService;
import org.twins.face.dao.twidget.tw007.FaceTW007Entity;
import org.twins.face.dao.twidget.tw007.FaceTW007Repository;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTW007Service extends FacePointedService<FaceTW007Entity> {

    private final FaceTW007Repository faceTW007Repository;
    private final FaceService faceService;


    @Override
    public List<FaceTW007Entity> getVariants(UUID of) {
        return faceTW007Repository.findByFaceId(of);
    }

    @Override
    public CrudRepository<FaceTW007Entity, UUID> entityRepository() {
        return faceTW007Repository;
    }

    @Override
    public Function<FaceTW007Entity, UUID> entityGetIdFunction() {
        return FaceTW007Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTW007Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FaceTW007Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
