package org.twins.face.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FaceVariantsService;
import org.twins.face.dao.widget.wt011.FaceWT011ColumnEntity;
import org.twins.face.dao.widget.wt011.FaceWT011ColumnRepository;
import org.twins.face.dao.widget.wt011.FaceWT011SectionEntity;
import org.twins.face.dao.widget.wt011.FaceWT011SectionRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT011ColumnService extends FaceVariantsService<FaceWT011ColumnEntity> {
    private final FaceWT011ColumnRepository faceWT011ColumnRepository;
    private final FaceWT011SectionRepository faceWT011SectionRepository;

    @Override
    public CrudRepository<FaceWT011ColumnEntity, UUID> entityRepository() {
        return faceWT011ColumnRepository;
    }

    @Override
    public Function<FaceWT011ColumnEntity, UUID> entityGetIdFunction() {
        return FaceWT011ColumnEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT011ColumnEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FaceWT011ColumnEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceWT011ColumnEntity> getVariants(UUID faceWT011Id) {
        return faceWT011ColumnRepository.findByFaceWT011Id(faceWT011Id);
    }

    public void loadSections(FaceWT011ColumnEntity src) {
        loadSections(Collections.singletonList(src));
    }

    public void loadSections(Collection<FaceWT011ColumnEntity> srcList) {
        loadKit(srcList,
                FaceWT011ColumnEntity::getId,
                FaceWT011ColumnEntity::getSections,
                FaceWT011ColumnEntity::setSections,
                faceWT011SectionRepository::findByFaceWT011ColumnIdIn,
                FaceWT011SectionEntity::getId,
                FaceWT011SectionEntity::getFaceWT011ColumnId,
                FaceWT011SectionEntity::setFaceWT011Column);
    }
}
