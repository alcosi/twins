package org.twins.face.service.page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FaceService;
import org.twins.core.service.face.FaceVariantsService;
import org.twins.face.dao.page.pg002.FacePG002Entity;
import org.twins.face.dao.page.pg002.FacePG002TabEntity;
import org.twins.face.dao.page.pg002.FacePG002TabRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FacePG002TabService extends FaceVariantsService<FacePG002TabEntity> {
    private final FacePG002TabRepository facePG002TabRepository;
    private final FaceService faceService;

    @Override
    public CrudRepository<FacePG002TabEntity, UUID> entityRepository() {
        return facePG002TabRepository;
    }

    @Override
    public Function<FacePG002TabEntity, UUID> entityGetIdFunction() {
        return FacePG002TabEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FacePG002TabEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FacePG002TabEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FacePG002TabEntity> getVariants(UUID of) {
        return facePG002TabRepository.findByFacePG002Id(of);
    }

    public void loadTabs(FacePG002Entity src) {
        loadTabs(Collections.singletonList(src));
    }

    public void loadTabs(Collection<FacePG002Entity> srcList) {
        loadKit(srcList,
                FacePG002Entity::getId,
                FacePG002Entity::getTabs,
                FacePG002Entity::setTabs,
                facePG002TabRepository::findByFacePG002IdIn,
                FacePG002TabEntity::getId,
                FacePG002TabEntity::getFacePG002Id,
                FacePG002TabEntity::setFacePG002);
    }
}
