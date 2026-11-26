package org.twins.face.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FaceVariantsService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;
import org.twins.face.dao.widget.wt011.FaceWT011FieldEntity;
import org.twins.face.dao.widget.wt011.FaceWT011FieldRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT011FieldService extends FaceVariantsService<FaceWT011FieldEntity> {
    private final FaceWT011FieldRepository faceWT011FieldRepository;
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public CrudRepository<FaceWT011FieldEntity, UUID> entityRepository() {
        return faceWT011FieldRepository;
    }

    @Override
    public Function<FaceWT011FieldEntity, UUID> entityGetIdFunction() {
        return FaceWT011FieldEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT011FieldEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FaceWT011FieldEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceWT011FieldEntity> getVariants(UUID faceWT011RowId) {
        return faceWT011FieldRepository.findByFaceWT011RowId(faceWT011RowId);
    }

    public void loadTwinClassFields(FaceWT011FieldEntity src) throws ServiceException {
        loadTwinClassFields(Collections.singletonList(src));
    }

    public void loadTwinClassFields(Collection<FaceWT011FieldEntity> srcCollection) throws ServiceException {
        twinClassFieldService.load(srcCollection,
                FaceWT011FieldEntity::getTwinClassFieldId,
                FaceWT011FieldEntity::getTwinClassField,
                FaceWT011FieldEntity::setTwinClassField);
    }
}
