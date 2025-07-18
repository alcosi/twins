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
import org.twins.core.service.face.FaceVariantsService;
import org.twins.core.service.twinclass.TwinClassFieldSearchService;
import org.twins.face.dao.tc.tc002.FaceTC002OptionEntity;
import org.twins.face.dao.tc.tc002.FaceTC002OptionRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTC002OptionService extends FaceVariantsService<FaceTC002OptionEntity> {
    private final FaceTC002OptionRepository faceTC002OptionRepository;
    private final FeaturerService featurerService;
    private final TwinClassFieldSearchService twinClassFieldSearchService;

    @Override
    public CrudRepository<FaceTC002OptionEntity, UUID> entityRepository() {
        return faceTC002OptionRepository;
    }

    @Override
    public Function<FaceTC002OptionEntity, UUID> entityGetIdFunction() {
        return FaceTC002OptionEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTC002OptionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FaceTC002OptionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceTC002OptionEntity> getVariants(UUID of) {
        return faceTC002OptionRepository.findByFaceTC002Id(of);
    }

}
