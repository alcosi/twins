package org.twins.face.service.tc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.featurer.fieldfinder.FieldFinder;
import org.twins.core.service.face.FaceService;
import org.twins.core.service.face.FaceVariantsService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldSearchService;
import org.twins.face.dao.tc.tc001.FaceTC001Entity;
import org.twins.face.dao.tc.tc001.FaceTC001Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTC001Service extends FaceVariantsService<FaceTC001Entity> {
    private final FaceTC001Repository faceTC001Repository;
    private final FeaturerService featurerService;
    private final TwinClassFieldSearchService twinClassFieldSearchService;
    private final TwinService twinService;
    private final FaceService faceService;

    @Override
    public CrudRepository<FaceTC001Entity, UUID> entityRepository() {
        return faceTC001Repository;
    }

    @Override
    public Function<FaceTC001Entity, UUID> entityGetIdFunction() {
        return FaceTC001Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTC001Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FaceTC001Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadFields(FaceTC001Entity entity) throws ServiceException {
        loadFields(Collections.singletonList(entity));
    }

    public void loadFields(Collection<FaceTC001Entity> entities) throws ServiceException {
        for (FaceTC001Entity entity : entities) {
            if (entity.getFields() != null) {
                continue;
            }
            FieldFinder fieldFinder = featurerService.getFeaturer(entity.getFieldFinderFeaturerId(), FieldFinder.class);
            TwinClassFieldSearch twinClassFieldSearch = fieldFinder.createSearch(entity.getFieldFinderParams(), entity.getTwinClassId());
            twinClassFieldSearch.setExcludeSystemFields(false);
            Kit<TwinClassFieldEntity, UUID> fields = new Kit<>(twinClassFieldSearchService.findTwinClassField(twinClassFieldSearch), TwinClassFieldEntity::getId);
            entity.setFields(fields);
        }
    }

    @Override
    public List<FaceTC001Entity> getVariants(UUID of) {
        return faceTC001Repository.findByFaceId(of);
    }
}
