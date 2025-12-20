package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldAttributeEntity;
import org.twins.core.dao.twin.TwinFieldAttributeRepository;
import org.twins.core.domain.EntityCUD;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.twinclass.TwinClassFieldAttributeService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinFieldAttributeService extends EntitySecureFindServiceImpl<TwinFieldAttributeEntity> {
    private final TwinFieldAttributeRepository twinFieldAttributeRepository;
    @Lazy
    private final PermissionService permissionService;
    @Lazy
    private final TwinService twinService;
    @Lazy
    private final TwinClassFieldAttributeService twinClassFieldAttributeService;


    @Override
    public CrudRepository<TwinFieldAttributeEntity, UUID> entityRepository() {
        return twinFieldAttributeRepository;
    }

    @Override
    public Function<TwinFieldAttributeEntity, UUID> entityGetIdFunction() {
        return TwinFieldAttributeEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinFieldAttributeEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinFieldAttributeEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinId");
        if (entity.getTwinClassFieldId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinClassFieldId");
        if (entity.getTwinClassFieldAttributeId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinClassFieldAttributeId");

        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getTwin() == null || !entity.getTwin().getId().equals(entity.getTwinId()))
                    entity.setTwin(twinService.findEntitySafe(entity.getTwinId()));
                if (entity.getTwinClassFieldAttribute() == null || !entity.getTwinClassFieldAttribute().getId().equals(entity.getTwinClassFieldAttributeId()))
                    entity.setTwinClassFieldAttribute(twinClassFieldAttributeService.findEntitySafe(entity.getTwinClassFieldAttributeId()));
        }
        return true;
    }

    public void addAttributes(TwinEntity twinEntity, List<TwinFieldAttributeEntity> twinFieldAttributes, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (CollectionUtils.isEmpty(twinFieldAttributes)) {
            return;
        }
        loadTwinClassFieldAttributes(twinFieldAttributes);
        for (var twinFieldAttribute : twinFieldAttributes) {
            twinFieldAttribute
                    .setTwinId(twinEntity.getId())
                    .setTwin(twinEntity)
                    .setChangedAt(Timestamp.valueOf(LocalDateTime.now()));

            validateEntityAndThrow(twinFieldAttribute, EntitySmartService.EntityValidateMode.beforeSave);
            //permission check after validation to make sure TwinClassFieldAttributeEntity is loaded
            UUID createPermissionId = twinFieldAttribute.getTwinClassFieldAttribute().getCreatePermissionId();
            if (createPermissionId != null && !permissionService.currentUserHasPermission(createPermissionId)) {
                throw new ServiceException(ErrorCodeTwins.NO_REQUIRED_PERMISSION, "cannot create % without permission[%]", twinFieldAttribute.getTwinClassFieldAttribute().logNormal(), createPermissionId.toString());
            }
            //upset (if uniq flag is true) logic is implemented in db level (see twin_field_attribute_upsert_trigger)
            twinChangesCollector.add(twinFieldAttribute);
        }
    }

    public void loadTwinClassFieldAttributes(TwinFieldAttributeEntity twinFieldAttributeEntity) throws ServiceException {
        loadTwinClassFieldAttributes(Collections.singletonList(twinFieldAttributeEntity));
    }

    public void loadTwinClassFieldAttributes(Collection<TwinFieldAttributeEntity> twinFieldAttributes) throws ServiceException {
        twinClassFieldAttributeService.load(
                twinFieldAttributes,
                TwinFieldAttributeEntity::getId,
                TwinFieldAttributeEntity::getTwinClassFieldAttributeId,
                TwinFieldAttributeEntity::getTwinClassFieldAttribute,
                TwinFieldAttributeEntity::setTwinClassFieldAttribute
        );
    }

    public void cudAttributes(TwinEntity twinEntity, EntityCUD<TwinFieldAttributeEntity> attributeCUD, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (attributeCUD == null)
            return;
        if (CollectionUtils.isNotEmpty(attributeCUD.getCreateList())) {
            addAttributes(twinEntity, attributeCUD.getCreateList(), twinChangesCollector);
        }
        if (CollectionUtils.isNotEmpty(attributeCUD.getUpdateList())) {
            updateAttributes(attributeCUD.getUpdateList(), twinChangesCollector);
        }
        if (CollectionUtils.isNotEmpty(attributeCUD.getDeleteList())) {
            deleteAttributes(attributeCUD.getDeleteList(), twinChangesCollector);
        }
    }

    public void loadAttributes(TwinEntity twinEntity) {
        loadAttributes(Collections.singletonList(twinEntity));
    }

    public void loadAttributes(Collection<TwinEntity> twinEntityList) {
        Kit<TwinEntity, UUID> needLoad = new Kit<>(TwinEntity::getId);
        for (TwinEntity twinEntity : twinEntityList) {
            if (twinEntity.getTwinFieldAttributeKit() == null) {
                needLoad.add(twinEntity);
            }
        }
        if (needLoad.isEmpty())
            return;

        KitGrouped<TwinFieldAttributeEntity, UUID, UUID> attributes = new KitGrouped<>(twinFieldAttributeRepository.findByTwinIdIn(needLoad.getIdSet()), TwinFieldAttributeEntity::getId, TwinFieldAttributeEntity::getTwinId);
        for (TwinEntity twinEntity : needLoad) {
            if (attributes.containsGroupedKey(twinEntity.getId()))
                twinEntity.setTwinFieldAttributeKit(new KitGrouped<>(attributes.getGrouped(twinEntity.getId()), TwinFieldAttributeEntity::getId, TwinFieldAttributeEntity::getTwinClassFieldId));
            else
                twinEntity.setTwinFieldAttributeKit(KitGrouped.EMPTY);
        }
    }

    public void deleteAttributes(List<TwinFieldAttributeEntity> entities, TwinChangesCollector twinChangesCollector) throws ServiceException {
        for (TwinFieldAttributeEntity twinFieldAttributeEntity : entities) {
            UUID deletePermissionId = twinFieldAttributeEntity.getTwinClassFieldAttribute().getDeletePermissionId();
            if (deletePermissionId != null && !permissionService.currentUserHasPermission(deletePermissionId)) {
                throw new ServiceException(ErrorCodeTwins.NO_REQUIRED_PERMISSION, "cannot delete % without permission[%]", twinFieldAttributeEntity.getTwinClassFieldAttribute().logNormal(), deletePermissionId.toString());
            }

            twinChangesCollector.delete(twinFieldAttributeEntity);
        }
    }

    public void updateAttributes(List<TwinFieldAttributeEntity> entitiyList, TwinChangesCollector twinChangesCollector) throws ServiceException {
        Kit<TwinFieldAttributeEntity, UUID> newAttributeKit = new Kit<>(entitiyList, TwinFieldAttributeEntity::getId);
        Kit<TwinFieldAttributeEntity, UUID> dbAttributeKit = new Kit<>(twinFieldAttributeRepository.findByIdIn(newAttributeKit.getIdSet()), TwinFieldAttributeEntity::getId);

        TwinFieldAttributeEntity dbAttributeEntity;
        for (TwinFieldAttributeEntity attributeEntity : entitiyList) {
            dbAttributeEntity = dbAttributeKit.get(attributeEntity.getId());

            UUID updatePermissionId = dbAttributeEntity.getTwinClassFieldAttribute().getUpdatePermissionId();
            if (updatePermissionId != null && !permissionService.currentUserHasPermission(updatePermissionId)) {
                throw new ServiceException(ErrorCodeTwins.NO_REQUIRED_PERMISSION, "cannot update % without permission[%]", dbAttributeEntity.getTwinClassFieldAttribute(), updatePermissionId.toString());
            }
            if (twinChangesCollector.collectIfChanged(dbAttributeEntity, TwinFieldAttributeEntity.Fields.twinId, dbAttributeEntity.getTwinId(), attributeEntity.getTwinId())) {
                throw new ServiceException(ErrorCodeCommon.FORBIDDEN, "cannot update twin id for twin field attribute[" + dbAttributeEntity.getId() +"]");
            }

            if (twinChangesCollector.collectIfChanged(dbAttributeEntity, TwinFieldAttributeEntity.Fields.twinClassFieldId, dbAttributeEntity.getTwinClassFieldId(), attributeEntity.getTwinClassFieldId())) {
                throw new ServiceException(ErrorCodeCommon.FORBIDDEN, "cannot update twin class field id for twin field attribute[" + dbAttributeEntity.getId() +"]");
            }
            if (twinChangesCollector.collectIfChanged(dbAttributeEntity, TwinFieldAttributeEntity.Fields.twinClassFieldAttributeId, dbAttributeEntity.getTwinClassFieldAttributeId(), attributeEntity.getTwinClassFieldAttributeId())) {
                dbAttributeEntity.setTwinClassFieldAttributeId(attributeEntity.getTwinClassFieldAttributeId());
            }
            if (twinChangesCollector.collectIfChanged(dbAttributeEntity, TwinFieldAttributeEntity.Fields.noteMsg, dbAttributeEntity.getNoteMsg(), attributeEntity.getNoteMsg())) {
                dbAttributeEntity.setNoteMsg(attributeEntity.getNoteMsg());
            }
            if (twinChangesCollector.collectIfChanged(dbAttributeEntity, TwinFieldAttributeEntity.Fields.noteMsgContext, dbAttributeEntity.getNoteMsgContext(), attributeEntity.getNoteMsgContext())) {
                dbAttributeEntity.setNoteMsgContext(attributeEntity.getNoteMsgContext());
            }
            if (twinChangesCollector.collectIfChanged(dbAttributeEntity, TwinFieldAttributeEntity.Fields.changedAt, dbAttributeEntity.getChangedAt(), Timestamp.valueOf(LocalDateTime.now()))) {
                dbAttributeEntity.setChangedAt(Timestamp.valueOf(LocalDateTime.now()));
            }
        }
    }
}
