package org.twins.face.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.service.face.FacePointedService;
import org.twins.core.service.face.FaceTwinPointerService;
import org.twins.core.service.resource.ResourceService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;
import org.twins.face.dao.widget.wt003.FaceWT003Entity;
import org.twins.face.dao.widget.wt003.FaceWT003Repository;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT003Service extends FacePointedService<FaceWT003Entity> {

    private static final String FIELD_VAR = "field.value";
    private final FaceWT003Repository faceWT003Repository;
    private final TwinService twinService;
    private final FaceTwinPointerService faceTwinPointerService;
    private final ResourceService resourceService;
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public CrudRepository<FaceWT003Entity, UUID> entityRepository() {
        return faceWT003Repository;
    }

    @Override
    public Function<FaceWT003Entity, UUID> entityGetIdFunction() {
        return FaceWT003Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT003Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FaceWT003Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceWT003Entity> getVariants(UUID of) {
        return faceWT003Repository.findByFaceId(of);
    }

    public Map<String, String> createSubstitutionMap(UUID pointerId, TwinClassFieldEntity field) throws ServiceException {
        Map<String, String> map = null;

        if (pointerId != null && field != null)  {
            String value = twinService.getValueFromTwinClassField(faceTwinPointerService.getPointer(pointerId), field);
            map = Map.of(FIELD_VAR, value != null ? value : "");
        }

        return map;
    }

    public void loadIconResources(FaceWT003Entity src) throws ServiceException {
        loadIconResources(Collections.singletonList(src));
    }

    public void loadIconResources(Collection<FaceWT003Entity> srcCollection) throws ServiceException {
        resourceService.load(srcCollection,
                FaceWT003Entity::getIconResourceId,
                FaceWT003Entity::getIconResource,
                FaceWT003Entity::setIconResource);
    }

    public void loadSubstitutionFields(FaceWT003Entity src) throws ServiceException {
        loadSubstitutionFields(Collections.singletonList(src));
    }

    public void loadSubstitutionFields(Collection<FaceWT003Entity> srcCollection) throws ServiceException {
        twinClassFieldService.load(srcCollection,
                new LoadedField<>(FaceWT003Entity::getTitleSubstitutionTwinClassFieldId, FaceWT003Entity::getSubstitutionTitleField, FaceWT003Entity::setSubstitutionTitleField),
                new LoadedField<>(FaceWT003Entity::getMessageSubstitutionTwinClassFieldId, FaceWT003Entity::getSubstitutionMessageField, FaceWT003Entity::setSubstitutionMessageField));
    }
}
