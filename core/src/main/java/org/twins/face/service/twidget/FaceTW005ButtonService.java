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
import org.twins.face.dao.twidget.tw005.FaceTW005ButtonEntity;
import org.twins.face.dao.twidget.tw005.FaceTW005ButtonRepository;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTW005ButtonService extends FaceVariantsService<FaceTW005ButtonEntity> {
    private final FaceTW005ButtonRepository faceTW005ButtonRepository;
    private final FaceService faceService;

    @Override
    public CrudRepository<FaceTW005ButtonEntity, UUID> entityRepository() {
        return faceTW005ButtonRepository;
    }

    @Override
    public Function<FaceTW005ButtonEntity, UUID> entityGetIdFunction() {
        return FaceTW005ButtonEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTW005ButtonEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FaceTW005ButtonEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceTW005ButtonEntity> getVariants(UUID of) {
        return faceTW005ButtonRepository.findByFaceTW005Id(of);
    }
}