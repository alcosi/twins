package org.twins.face.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FaceVariantsService;
import org.twins.face.dao.widget.wt011.FaceWT011RowEntity;
import org.twins.face.dao.widget.wt011.FaceWT011RowRepository;
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
public class FaceWT011SectionService extends FaceVariantsService<FaceWT011SectionEntity> {
    private final FaceWT011SectionRepository faceWT011SectionRepository;
    private final FaceWT011RowRepository faceWT011RowRepository;

    @Override
    public CrudRepository<FaceWT011SectionEntity, UUID> entityRepository() {
        return faceWT011SectionRepository;
    }

    @Override
    public Function<FaceWT011SectionEntity, UUID> entityGetIdFunction() {
        return FaceWT011SectionEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT011SectionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FaceWT011SectionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceWT011SectionEntity> getVariants(UUID faceWT011ColumnId) {
        return faceWT011SectionRepository.findByFaceWT011ColumnId(faceWT011ColumnId);
    }

    public void loadRows(FaceWT011SectionEntity src) {
        loadRows(Collections.singletonList(src));
    }

    public void loadRows(Collection<FaceWT011SectionEntity> srcList) {
        loadKit(srcList,
                FaceWT011SectionEntity::getId,
                FaceWT011SectionEntity::getRows,
                FaceWT011SectionEntity::setRows,
                faceWT011RowRepository::findByFaceWT011SectionIdIn,
                FaceWT011RowEntity::getId,
                FaceWT011RowEntity::getFaceWT011SectionId,
                FaceWT011RowEntity::setFaceWT011Section);
    }
}
