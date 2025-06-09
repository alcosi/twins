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
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.face.TwidgetConfig;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.face.domain.twidget.tw004.FaceTW004TwinClassField;
import org.twins.core.featurer.fieldfilter.FieldFilter;
import org.twins.core.featurer.fieldfinder.FieldFinder;
import org.twins.core.service.face.FaceService;
import org.twins.core.service.face.FaceTwidgetService;
import org.twins.core.service.twinclass.TwinClassFieldSearchService;
import org.twins.face.dao.twidget.tw004.FaceTW004Entity;
import org.twins.face.dao.twidget.tw004.FaceTW004Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTW004Service extends FaceTwidgetService<FaceTW004Entity> {
    private final FaceTW004Repository faceTW004Repository;
    private final FaceService faceService;
    private final FeaturerService featurerService;
    private final TwinClassFieldSearchService twinClassFieldSearchService;

    @Override
    public CrudRepository<FaceTW004Entity, UUID> entityRepository() {
        return faceTW004Repository;
    }

    @Override
    public Function<FaceTW004Entity, UUID> entityGetIdFunction() {
        return FaceTW004Entity::getFaceId;
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
    public FaceTW004Entity getConfig(UUID faceId, TwinEntity currentTwin, TwinEntity targetTwin) throws ServiceException {
        return findEntitySafe(faceId);
    }

    public List<FaceTW004TwinClassField> loadFields(UUID twinClassId, TwidgetConfig<FaceTW004Entity> twidgetConfig) throws ServiceException {
        FieldFinder fieldFinder = featurerService.getFeaturer(twidgetConfig.getConfig().getFieldFinderFeaturer(), FieldFinder.class);
        TwinClassFieldSearch twinClassFieldSearch = fieldFinder.createSearch(twidgetConfig.getConfig().getFieldFinderParams(), twinClassId);
        twinClassFieldSearch.setExcludeSystemFields(false);
        List<TwinClassFieldEntity> fields = twinClassFieldSearchService.findTwinClassField(twinClassFieldSearch, new SimplePagination().setLimit(250).setOffset(0)).getList();

        FieldFilter fieldFilter = featurerService.getFeaturer(twidgetConfig.getConfig().getFieldFilterFeaturer(), FieldFilter.class);
        Kit<TwinClassFieldEntity, UUID> fieldsKit = fieldFilter.filterFields(twidgetConfig.getConfig().getFieldFilterParams(), fields, twidgetConfig.getTargetTwin());
        Set<UUID> editableFieldIds = fieldsKit.getIdSetSafe();

        List<FaceTW004TwinClassField> result = new ArrayList<>(fields.size());
        for (TwinClassFieldEntity field : fields) {
            result.add(new FaceTW004TwinClassField(field, editableFieldIds.contains(field.getId())));
        }
        return result;
    }
}
