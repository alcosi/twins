package org.twins.face.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGrouped;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.service.attachment.AttachmentRestrictionService;
import org.twins.core.service.face.FacePointedService;
import org.twins.core.service.face.FaceService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;
import org.twins.face.dao.widget.wt004.FaceWT004Entity;
import org.twins.face.dao.widget.wt004.FaceWT004Repository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT004Service extends FacePointedService<FaceWT004Entity> {
    private final FaceWT004Repository faceWT004Repository;
    private final FaceService faceService;
    private final AttachmentRestrictionService attachmentRestrictionService;
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public CrudRepository<FaceWT004Entity, UUID> entityRepository() {
        return faceWT004Repository;
    }

    @Override
    public Function<FaceWT004Entity, UUID> entityGetIdFunction() {
        return FaceWT004Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT004Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        if (entity.getFace() == null)
            entity.setFace(findFaceForReadCheck(entity.getFaceId()));
        return faceService.isEntityReadDenied(entity.getFace(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(FaceWT004Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceWT004Entity> getVariants(UUID of) {
        return faceWT004Repository.findByFaceId(of);
    }

    public void loadImagesTwinClassFields(FaceWT004Entity src) throws ServiceException {
        loadImagesTwinClassFields(Collections.singletonList(src));
    }

    public void loadImagesTwinClassFields(Collection<FaceWT004Entity> srcCollection) throws ServiceException {
        twinClassFieldService.load(srcCollection,
                FaceWT004Entity::getImagesTwinClassFieldId,
                FaceWT004Entity::getImagesTwinClassField,
                FaceWT004Entity::setImagesTwinClassField);
    }

    public void loadRestriction(PointedFace<FaceWT004Entity> pointedFace) throws ServiceException {
        loadRestriction(Collections.singletonList(pointedFace));
    }

    public void loadRestriction(Collection<PointedFace<FaceWT004Entity>> pointedFaces) throws ServiceException {
        if (pointedFaces.isEmpty()) {
            return;
        }

        Map<Boolean, List<PointedFace<FaceWT004Entity>>> groupedFaces = pointedFaces.stream()
                .filter(pf -> pf.getConfig().getTwinAttachmentRestriction() == null)
                .collect(Collectors.partitioningBy(
                        pf -> pf.getConfig().getImagesTwinClassFieldId() != null
                ));

        List<PointedFace<FaceWT004Entity>> facesWithImageFieldId = groupedFaces.get(true);
        List<PointedFace<FaceWT004Entity>> facesWithoutImageFieldId = groupedFaces.get(false);

        loadRestrictionsFromFieldTyper(facesWithImageFieldId);
        loadGeneralAttachmentRestrictions(facesWithoutImageFieldId);
    }

    private void loadRestrictionsFromFieldTyper(List<PointedFace<FaceWT004Entity>> pointedFaceList) throws ServiceException {
        if (pointedFaceList.isEmpty()) {
            return;
        }

        KitGrouped<FaceWT004Entity, UUID, UUID> faceWT004GroupedByImageFieldId = new KitGrouped<>(
                pointedFaceList.stream().map(PointedFace::getConfig).toList(), FaceWT004Entity::getId, FaceWT004Entity::getImagesTwinClassFieldId);

        Map<UUID, TwinAttachmentRestrictionEntity> restrictionMap = attachmentRestrictionService.getRestrictionsFromFieldTyper(faceWT004GroupedByImageFieldId.getGroupedKeySet());

        for (var wt004 : faceWT004GroupedByImageFieldId) {
            wt004.setTwinAttachmentRestriction(restrictionMap.get(wt004.getImagesTwinClassFieldId()));
        }
    }

    private void loadGeneralAttachmentRestrictions(List<PointedFace<FaceWT004Entity>> pointedFaceList) throws ServiceException {
        if (pointedFaceList.isEmpty()) {
            return;
        }

        attachmentRestrictionService.loadGeneralRestrictions(
                pointedFaceList.stream().map(PointedFace::getTargetTwin).map(TwinEntity::getTwinClass).toList());

        for (var pointedFace : pointedFaceList) {
            pointedFace.getConfig().setTwinAttachmentRestriction(pointedFace.getTargetTwin().getTwinClass().getGeneralAttachmentRestriction());
        }
    }
}