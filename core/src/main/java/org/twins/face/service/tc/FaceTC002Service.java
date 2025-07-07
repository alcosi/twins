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
import org.twins.core.service.twinclass.TwinClassFieldSearchService;
import org.twins.face.dao.tc.tc002.FaceTC002Entity;
import org.twins.face.dao.tc.tc002.FaceTC002Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTC002Service extends FaceVariantsService<FaceTC002Entity> {
    private final FaceTC002Repository faceTC002Repository;
    private final FeaturerService featurerService;
    private final TwinClassFieldSearchService twinClassFieldSearchService;
    private final FaceService faceService;


    @Override
    public CrudRepository<FaceTC002Entity, UUID> entityRepository() {
        return faceTC002Repository;
    }

    @Override
    public Function<FaceTC002Entity, UUID> entityGetIdFunction() {
        return FaceTC002Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTC002Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FaceTC002Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceTC002Entity> getVariants(UUID of) {
        return faceTC002Repository.findByFaceId(of);
    }

    public void loadFields(FaceTC002Entity entity) throws ServiceException {
        loadFields(Collections.singletonList(entity));
    }

    public void loadFields(Collection<FaceTC002Entity> entities) throws ServiceException {
        for (FaceTC002Entity entity : entities) {
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
}
