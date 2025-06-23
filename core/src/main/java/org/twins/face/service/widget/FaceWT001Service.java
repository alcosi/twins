package org.twins.face.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FaceService;
import org.twins.core.service.face.FaceVariantsService;
import org.twins.face.dao.widget.wt001.FaceWT001ColumnRepository;
import org.twins.face.dao.widget.wt001.FaceWT001Entity;
import org.twins.face.dao.widget.wt001.FaceWT001Repository;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT001Service extends FaceVariantsService<FaceWT001Entity> {
    private final FaceWT001Repository faceWT001Repository;
    private final FaceWT001ColumnRepository faceWT001ColumnRepository;
    private final FaceService faceService;

    @Override
    public CrudRepository<FaceWT001Entity, UUID> entityRepository() {
        return faceWT001Repository;
    }

    @Override
    public Function<FaceWT001Entity, UUID> entityGetIdFunction() {
        return FaceWT001Entity::getFaceId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT001Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FaceWT001Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceWT001Entity> getVariants(UUID of) {
        return faceWT001Repository.findByFaceId(of);
    }
}
