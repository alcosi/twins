package org.twins.face.service.twidget;

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
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.featurer.fieldfilter.FieldFilter;
import org.twins.core.featurer.fieldfinder.FieldFinder;
import org.twins.core.service.face.FacePointedService;
import org.twins.core.service.face.FaceService;
import org.twins.core.service.twinclass.TwinClassFieldSearchService;
import org.twins.face.dao.twidget.tw004.FaceTW004Entity;
import org.twins.face.dao.twidget.tw004.FaceTW004Repository;
import org.twins.face.domain.twidget.tw004.FaceTW004TwinClassField;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTW004Service extends FacePointedService<FaceTW004Entity> {
    private final FaceTW004Repository faceTW004Repository;
    private final FeaturerService featurerService;
    private final TwinClassFieldSearchService twinClassFieldSearchService;
    private final FaceService faceService;

    @Override
    public CrudRepository<FaceTW004Entity, UUID> entityRepository() {
        return faceTW004Repository;
    }

    @Override
    public Function<FaceTW004Entity, UUID> entityGetIdFunction() {
        return FaceTW004Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTW004Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FaceTW004Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceTW004Entity> getVariants(UUID faceId) {
        return faceTW004Repository.findByFaceId(faceId);
    }

    public List<FaceTW004TwinClassField> loadFields(PointedFace<FaceTW004Entity> pointedFace) throws ServiceException {
        FieldFinder fieldFinder = featurerService.getFeaturer(pointedFace.getConfig().getFieldFinderFeaturerId(), FieldFinder.class);
        TwinClassFieldSearch twinClassFieldSearch = fieldFinder.createSearch(pointedFace.getConfig().getFieldFinderParams(), pointedFace.getTargetTwin().getTwinClassId());
        twinClassFieldSearch.setExcludeSystemFields(false);
        List<TwinClassFieldEntity> fields = twinClassFieldSearchService.findTwinClassField(twinClassFieldSearch, new SimplePagination().setLimit(250).setOffset(0)).getList();

        Set<UUID> editableFieldIds = null;

        if (pointedFace.getConfig().getFieldFilterFeaturerId() != null) {
            FieldFilter fieldFilter = featurerService.getFeaturer(pointedFace.getConfig().getFieldFilterFeaturerId(), FieldFilter.class);
            Kit<TwinClassFieldEntity, UUID> fieldsKit = fieldFilter.filterFields(pointedFace.getConfig().getFieldFilterParams(), fields, pointedFace.getTargetTwin());
            editableFieldIds = fieldsKit.getIdSetSafe();
        }

        List<FaceTW004TwinClassField> result = new ArrayList<>(fields.size());
        for (TwinClassFieldEntity field : fields) {
            boolean isEditable = editableFieldIds == null || editableFieldIds.contains(field.getId());

            result.add(new FaceTW004TwinClassField(field, isEditable));
        }
        return result;
    }
}