package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimple;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinSketchService extends EntitySecureFindServiceImpl<TwinEntity> {
    private final TwinRepository twinRepository;
    private final TwinClassFieldService twinClassFieldService;
    private final FeaturerService featurerService;
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;
    private final AuthService authService;

    private static final UUID SKETCH_STATUS_ID = UUID.fromString("00000001-0000-0000-0000-000000000001");

    @Override
    public CrudRepository<TwinEntity, UUID> entityRepository() {
        return twinRepository;
    }

    @Override
    public Function<TwinEntity, UUID> entityGetIdFunction() {
        return TwinEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinClassId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinClassId");

        return true;
    }

    @Transactional
    public TwinEntity createTwinSketch(TwinCreate twinSketchCreate) throws ServiceException {
        return createTwinSketch(List.of(twinSketchCreate)).getFirst();
    }

    @Transactional
    public List<TwinEntity> createTwinSketch(List<TwinCreate> twinSketchCreateList) throws ServiceException {
        //todo refactor, take all the functionality from TwinService, avoiding checks
        ApiUser apiUser = authService.getApiUser();

        List<TwinEntity> entities = new ArrayList<>();
        List<Object> fieldEntities = new ArrayList<>();

        for (TwinCreate sketch : twinSketchCreateList) {
            TwinEntity twinEntity = new TwinEntity()
                    .setId(UUID.randomUUID())
                    .setTwinClassId(sketch.getTwinEntity().getTwinClassId())
                    .setName("")
                    .setHeadTwinId(sketch.getTwinEntity().getHeadTwinId())
                    .setCreatedAt(Timestamp.from(Instant.now()))
                    .setTwinStatusId(SKETCH_STATUS_ID)
                    .setCreatedByUserId(apiUser.getUserId());

            validateEntityAndThrow(twinEntity, EntitySmartService.EntityValidateMode.beforeSave);

            entities.add(twinEntity);
            processFields(twinEntity, sketch.getFields(), fieldEntities);
        }

        twinRepository.saveAll(entities);
        saveFieldEntities(fieldEntities);

        return entities;
    }

    private void processFields(TwinEntity twinEntity, Map<UUID, FieldValue> fields, List<Object> fieldEntities) throws ServiceException {
        if (fields == null) return;

        for (Map.Entry<UUID, FieldValue> entry : fields.entrySet()) {
            UUID twinClassFieldId = entry.getKey();
            FieldValue fieldValue = entry.getValue();

            if (fieldValue == null || !fieldValue.isFilled()) continue;

            TwinClassFieldEntity twinClassField = twinClassFieldService.findEntitySafe(twinClassFieldId);
            FieldTyper fieldTyper = featurerService.getFeaturer(twinClassField.getFieldTyperFeaturer(), FieldTyper.class);

            Object fieldEntity = createFieldEntity(fieldTyper, twinEntity, fieldValue);
            if (fieldEntity != null) {
                fieldEntities.add(fieldEntity);
            }
        }
    }

    private Object createFieldEntity(FieldTyper fieldTyper, TwinEntity twinEntity, FieldValue fieldValue) throws ServiceException {
        Class<?> storageType = fieldTyper.getStorageType();

        if (storageType == TwinFieldStorageSimple.class) {
            return new TwinFieldSimpleEntity()
                    .setTwinId(twinEntity.getId())
                    .setTwinClassFieldId(fieldValue.getTwinClassField().getId())
                    .setValue(((FieldValueText)fieldValue).getValue());
        }

        log.warn("Unsupported field storage type: {}", storageType);
        return null;
    }

    private void saveFieldEntities(List<Object> fieldEntities) {
        List<TwinFieldSimpleEntity> simpleFields = new ArrayList<>();
        for (Object entity : fieldEntities) {
            if (entity instanceof TwinFieldSimpleEntity) {
                simpleFields.add((TwinFieldSimpleEntity) entity);
            }
        }
        twinFieldSimpleRepository.saveAll(simpleFields);
    }
}
