package org.twins.face.service.tc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FaceVariantsService;
import org.twins.face.dao.tc.tc001.FaceTC001OptionEntity;
import org.twins.face.dao.tc.tc001.FaceTC001OptionRepository;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTC001OptionService extends FaceVariantsService<FaceTC001OptionEntity> {
    private final FaceTC001OptionRepository facetTC001OptionRepository;

    @Override
    public List<FaceTC001OptionEntity> getVariants(UUID of) {
        return facetTC001OptionRepository.findByFaceTC001Id(of);
    }

    @Override
    public CrudRepository<FaceTC001OptionEntity, UUID> entityRepository() {
        return facetTC001OptionRepository;
    }

    @Override
    public Function<FaceTC001OptionEntity, UUID> entityGetIdFunction() {
        return FaceTC001OptionEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTC001OptionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FaceTC001OptionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
