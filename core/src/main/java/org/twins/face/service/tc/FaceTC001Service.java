package org.twins.face.service.tc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.featurer.fieldfinder.FieldFinder;
import org.twins.core.featurer.pointer.Pointer;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldSearchService;
import org.twins.face.dao.tc.tc001.FaceTC001Entity;
import org.twins.face.dao.tc.tc001.FaceTC001Repository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTC001Service extends EntitySecureFindServiceImpl<FaceTC001Entity> {
    private final FaceTC001Repository faceTC001Repository;
    private final FeaturerService featurerService;
    private final TwinClassFieldSearchService twinClassFieldSearchService;
    private final TwinService twinService;

    @Override
    public CrudRepository<FaceTC001Entity, UUID> entityRepository() {
        return faceTC001Repository;
    }

    @Override
    public Function<FaceTC001Entity, UUID> entityGetIdFunction() {
        return FaceTC001Entity::getFaceId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTC001Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FaceTC001Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public TwinEntity getHeadTwin(UUID currentTwinId, FaceTC001Entity entity) throws ServiceException {
        if (currentTwinId == null) {
            return null;
        }
        TwinEntity currentTwin = twinService.findEntitySafe(currentTwinId);
        Pointer pointer = featurerService.getFeaturer(entity.getHeadPointerFeaturerId(), Pointer.class);
        return pointer.point(entity.getHeadPointerParams(), currentTwin);
    }

    public void loadFields(UUID twinClassId, FaceTC001Entity entity) throws ServiceException {
        if (entity.getFieldFinderFeaturerId() == null) {
            return;
        }
        FieldFinder fieldFinder = featurerService.getFeaturer(entity.getFieldFinderFeaturerId(), FieldFinder.class);
        TwinClassFieldSearch twinClassFieldSearch = fieldFinder.createSearch(entity.getFieldFinderParams(), twinClassId);
        twinClassFieldSearch.setExcludeSystemFields(false);
        Kit<TwinClassFieldEntity, UUID> fields =  new Kit<>(twinClassFieldSearchService.findTwinClassField(twinClassFieldSearch, new SimplePagination().setLimit(250).setOffset(0)).getList(), TwinClassFieldEntity::getId);
        entity.setFields(fields);
    }
}
