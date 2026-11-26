package org.twins.face.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FaceVariantsService;
import org.twins.face.dao.widget.wt011.FaceWT011FieldEntity;
import org.twins.face.dao.widget.wt011.FaceWT011FieldRepository;
import org.twins.face.dao.widget.wt011.FaceWT011RowEntity;
import org.twins.face.dao.widget.wt011.FaceWT011RowRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT011RowService extends FaceVariantsService<FaceWT011RowEntity> {
    private final FaceWT011RowRepository faceWT011RowRepository;
    private final FaceWT011FieldRepository faceWT011FieldRepository;

    @Override
    public CrudRepository<FaceWT011RowEntity, UUID> entityRepository() {
        return faceWT011RowRepository;
    }

    @Override
    public Function<FaceWT011RowEntity, UUID> entityGetIdFunction() {
        return FaceWT011RowEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT011RowEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FaceWT011RowEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceWT011RowEntity> getVariants(UUID faceWT011SectionId) {
        return faceWT011RowRepository.findByFaceWT011SectionId(faceWT011SectionId);
    }

    public void loadFields(FaceWT011RowEntity src) {
        loadFields(Collections.singletonList(src));
    }

    public void loadFields(Collection<FaceWT011RowEntity> srcList) {
        loadKit(srcList,
                FaceWT011RowEntity::getId,
                FaceWT011RowEntity::getFields,
                FaceWT011RowEntity::setFields,
                faceWT011FieldRepository::findByFaceWT011RowIdIn,
                FaceWT011FieldEntity::getId,
                FaceWT011FieldEntity::getFaceWT011RowId,
                FaceWT011FieldEntity::setFaceWT011Row);
    }
}
