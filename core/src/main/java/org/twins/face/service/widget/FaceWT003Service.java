package org.twins.face.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.FieldTyperSimple;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueI18n;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.face.FaceTwinPointerService;
import org.twins.core.service.face.FaceVariantsService;
import org.twins.core.service.twin.TwinFieldSimpleSearchService;
import org.twins.core.service.twin.TwinService;
import org.twins.face.dao.widget.wt003.FaceWT003Entity;
import org.twins.face.dao.widget.wt003.FaceWT003Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT003Service extends FaceVariantsService<FaceWT003Entity> {

    private static final String FIELD_VAR = "field.value";
    private final FaceWT003Repository faceWT003Repository;
    private final TwinService twinService;
    private final FaceTwinPointerService faceTwinPointerService;

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
}
