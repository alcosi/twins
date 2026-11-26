package org.twins.face.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.featurer.classfield.filter.FieldFilter;
import org.twins.core.featurer.classfield.finder.FieldFinder;
import org.twins.core.service.face.FacePointedService;
import org.twins.core.service.face.FaceService;
import org.twins.core.service.twinclassfield.TwinClassFieldSearchConfigService;
import org.twins.face.dao.widget.wt006.FaceWT006Entity;
import org.twins.face.dao.widget.wt006.FaceWT006Repository;
import org.twins.face.domain.widget.wt006.FaceWT006TwinClassField;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT006Service extends FacePointedService<FaceWT006Entity> {
    private final FaceWT006Repository faceWT006Repository;
    private final FeaturerService featurerService;
    private final TwinClassFieldSearchConfigService twinClassFieldSearchConfigService;
    private final FaceService faceService;

    @Override
    public CrudRepository<FaceWT006Entity, UUID> entityRepository() {
        return faceWT006Repository;
    }

    @Override
    public Function<FaceWT006Entity, UUID> entityGetIdFunction() {
        return FaceWT006Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT006Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        if (entity.getFace() == null)
            entity.setFace(findFaceForReadCheck(entity.getFaceId()));
        return faceService.isEntityReadDenied(entity.getFace(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(FaceWT006Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceWT006Entity> getVariants(UUID faceId) {
        return faceWT006Repository.findByFaceId(faceId);
    }

    public List<FaceWT006TwinClassField> loadFields(PointedFace<FaceWT006Entity> pointedFace) throws ServiceException {
        Map<String, String> params = new HashMap<>();
        params.put(FieldFinder.PARAM_CURRENT_TWIN_ID, pointedFace.getTargetTwin().getId().toString());
        params.put(FieldFinder.PARAM_CURRENT_TWIN_CLASS_ID, pointedFace.getTargetTwin().getTwinClassId().toString());
        List<TwinClassFieldEntity> fields = twinClassFieldSearchConfigService.findConfigured(pointedFace.getConfig().getTwinClassFieldSearchId(), params, null, SimplePagination.FRIENDLY).getList();

        Set<UUID> editableFieldIds = null;

        if (pointedFace.getConfig().getFieldFilterFeaturerId() != null) {
            FieldFilter fieldFilter = featurerService.getFeaturer(pointedFace.getConfig().getFieldFilterFeaturerId(), FieldFilter.class);
            Kit<TwinClassFieldEntity, UUID> fieldsKit = fieldFilter.filterFields(pointedFace.getConfig().getFieldFilterParams(), fields, pointedFace.getTargetTwin());
            editableFieldIds = fieldsKit.getIdSetSafe();
        }

        List<FaceWT006TwinClassField> result = new ArrayList<>(fields.size());
        int order = 0;

        for (TwinClassFieldEntity field : fields) {
            boolean isEditable = editableFieldIds == null || editableFieldIds.contains(field.getId());

            result.add(new FaceWT006TwinClassField(field, isEditable, ++order));
        }

        return result;
    }
}