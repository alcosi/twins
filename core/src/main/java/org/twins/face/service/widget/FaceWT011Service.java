package org.twins.face.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FacePointedService;
import org.twins.core.service.face.FaceService;
import org.twins.face.dao.widget.wt011.FaceWT011ColumnEntity;
import org.twins.face.dao.widget.wt011.FaceWT011ColumnRepository;
import org.twins.face.dao.widget.wt011.FaceWT011Entity;
import org.twins.face.dao.widget.wt011.FaceWT011Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT011Service extends FacePointedService<FaceWT011Entity> {

    private final FaceWT011Repository faceWT011Repository;
    private final FaceWT011ColumnRepository faceWT011ColumnRepository;
    private final FaceService faceService;

    @Override
    public CrudRepository<FaceWT011Entity, UUID> entityRepository() {
        return faceWT011Repository;
    }

    @Override
    public Function<FaceWT011Entity, UUID> entityGetIdFunction() {
        return FaceWT011Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT011Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        if (entity.getFace() == null)
            entity.setFace(findFaceForReadCheck(entity.getFaceId()));
        return faceService.isEntityReadDenied(entity.getFace(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(FaceWT011Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceWT011Entity> getVariants(UUID of) {
        return faceWT011Repository.findByFaceId(of);
    }

    public void loadColumns(FaceWT011Entity src) {
        loadColumns(Collections.singletonList(src));
    }

    public void loadColumns(Collection<FaceWT011Entity> srcList) {
        loadKit(srcList,
                FaceWT011Entity::getId,
                FaceWT011Entity::getColumns,
                FaceWT011Entity::setColumns,
                faceWT011ColumnRepository::findByFaceWT011IdIn,
                FaceWT011ColumnEntity::getId,
                FaceWT011ColumnEntity::getFaceWT011Id,
                FaceWT011ColumnEntity::setFaceWT011);
    }
}
