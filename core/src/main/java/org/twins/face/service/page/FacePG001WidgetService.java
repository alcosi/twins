package org.twins.face.service.page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FaceVariantsService;
import org.twins.face.dao.page.pg001.FacePG001Entity;
import org.twins.face.dao.page.pg001.FacePG001WidgetEntity;
import org.twins.face.dao.page.pg001.FacePG001WidgetRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FacePG001WidgetService extends FaceVariantsService<FacePG001WidgetEntity> {
    private final FacePG001WidgetRepository facePG001WidgetRepository;
    private final FacePG001Service facePG001Service;

    @Override
    public CrudRepository<FacePG001WidgetEntity, UUID> entityRepository() {
        return facePG001WidgetRepository;
    }

    @Override
    public Function<FacePG001WidgetEntity, UUID> entityGetIdFunction() {
        return FacePG001WidgetEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FacePG001WidgetEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FacePG001WidgetEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadWidgets(FacePG001Entity src) {
        loadWidgets(Collections.singletonList(src));
    }

    public void loadWidgets(Collection<FacePG001Entity> srcList) {
        loadKit(srcList,
                FacePG001Entity::getId,
                FacePG001Entity::getWidgets,
                FacePG001Entity::setWidgets,
                facePG001WidgetRepository::findByFacePG001IdIn,
                FacePG001WidgetEntity::getId,
                FacePG001WidgetEntity::getFacePG001Id);
    }

    @Override
    public List<FacePG001WidgetEntity> getVariants(UUID of) {
        return facePG001WidgetRepository.findByFacePG001Id(of);
    }
}
