package org.twins.face.service.twidget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.service.attachment.AttachmentRestrictionService;
import org.twins.core.service.face.FacePointedService;
import org.twins.core.service.face.FaceService;
import org.twins.face.dao.twidget.tw001.FaceTW001Entity;
import org.twins.face.dao.twidget.tw001.FaceTW001Repository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTW001Service extends FacePointedService<FaceTW001Entity> {
    private final FaceTW001Repository faceTW001Repository;
    private final FaceService faceService;
    private final AttachmentRestrictionService attachmentRestrictionService;

    @Override
    public CrudRepository<FaceTW001Entity, UUID> entityRepository() {
        return faceTW001Repository;
    }

    @Override
    public Function<FaceTW001Entity, UUID> entityGetIdFunction() {
        return FaceTW001Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTW001Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FaceTW001Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceTW001Entity> getVariants(UUID of) {
        return faceTW001Repository.findByFaceId(of);
    }

    public void loadRestriction(PointedFace<FaceTW001Entity> pointedFace) throws ServiceException {
        loadRestriction(Collections.singletonList(pointedFace));
    }

    public void loadRestriction(Collection<PointedFace<FaceTW001Entity>> pointedFaces) throws ServiceException {
        if (pointedFaces.isEmpty()) {
            return;
        }

        Map<Boolean, List<PointedFace<FaceTW001Entity>>> groupedFaces = pointedFaces.stream()
                .filter(pf -> pf.getConfig().getTwinAttachmentRestriction() == null)
                .collect(Collectors.partitioningBy(
                        pf -> pf.getConfig().getImagesTwinClassFieldId() != null
                ));

        List<PointedFace<FaceTW001Entity>> facesWithImageFieldId = groupedFaces.get(true);
        List<PointedFace<FaceTW001Entity>> facesWithoutImageFieldId = groupedFaces.get(false);

        loadRestrictionFromFieldTyper(facesWithImageFieldId);
        loadGeneralAttachmentRestriction(facesWithoutImageFieldId);
    }

    private void loadRestrictionFromFieldTyper(List<PointedFace<FaceTW001Entity>> pointedFaceList) throws ServiceException {
        if (pointedFaceList.isEmpty()) {
            return;
        }

        Map<UUID, FaceTW001Entity> tw001Map = pointedFaceList.stream()
                .map(PointedFace::getConfig)
                .collect(Collectors.toMap(
                        FaceTW001Entity::getImagesTwinClassFieldId,
                        entity -> entity
                ));

        Map<UUID, TwinAttachmentRestrictionEntity> restrictionMap = attachmentRestrictionService.getRestrictionFromFieldTyper(tw001Map.keySet());

        for (UUID key : restrictionMap.keySet()) {
            tw001Map.get(key).setTwinAttachmentRestriction(restrictionMap.get(key));
        }
    }

    private void loadGeneralAttachmentRestriction(List<PointedFace<FaceTW001Entity>> pointedFaceList) throws ServiceException {
        if (pointedFaceList.isEmpty()) {
            return;
        }

        Map<UUID, FaceTW001Entity> tw001Map = pointedFaceList.stream()
                .collect(Collectors.toMap(
                        k -> k.getTargetTwin().getTwinClass().getGeneralAttachmentRestrictionId(),
                        PointedFace::getConfig
                ));

        Map<UUID, TwinAttachmentRestrictionEntity> restrictionMap = attachmentRestrictionService.getGeneralRestrictions(tw001Map.keySet());

        for (UUID key : restrictionMap.keySet()) {
            tw001Map.get(key).setTwinAttachmentRestriction(restrictionMap.get(key));
        }
    }



















//    public void loadRestriction1111(Collection<PointedFace<FaceTW001Entity>> pointedFaces) throws ServiceException {
//        TwinAttachmentRestrictionEntity restrictionEntity;
//
//        for (PointedFace<FaceTW001Entity> pointedFace : pointedFaces) {
//            if (pointedFace.getConfig().getImagesTwinClassFieldId() != null) {
//                restrictionEntity = attachmentRestrictionService.getRestrictionFromFieldTyper(pointedFace.getConfig().getImagesTwinClassField());
//            } else {
//                restrictionEntity = attachmentRestrictionService.findEntitySafe(
//                        pointedFace.getTargetTwin().getTwinClass().getGeneralAttachmentRestrictionId()
//                );
//            }
//
//            pointedFace.getConfig().setTwinAttachmentRestriction(restrictionEntity);
//        }
//    }
}