package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
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

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Lazy
@RequiredArgsConstructor
public class TwinFieldAttributeService extends EntitySecureFindServiceImpl<TwinFieldAttributeEntity> {
    private final TwinFieldAttributeRepository twinFieldAttributeRepository;


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

        return true;
    }

    public void addAttributes(TwinEntity twinEntity, List<TwinFieldAttributeEntity> twinFieldAttributeEntities, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (CollectionUtils.isEmpty(twinFieldAttributeEntities)) {
            return;
        }

        for (TwinFieldAttributeEntity twinFieldAttributeEntity : twinFieldAttributeEntities) {
            twinFieldAttributeEntity
                    .setTwinId(twinEntity.getId())
                    .setTwin(twinEntity)
                    .setChangedAt(Timestamp.valueOf(LocalDateTime.now()));
            validateEntityAndThrow(twinFieldAttributeEntity, EntitySmartService.EntityValidateMode.beforeSave);
            twinChangesCollector.add(twinFieldAttributeEntity);
        }
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

    public void loadAttributes(Collection<TwinEntity> twinEntityList) {
        Map<UUID, TwinEntity> needLoad = new HashMap<>();
        for (TwinEntity twinEntity : twinEntityList) {
            if (twinEntity.getTwinFieldAttributeKit() == null) {
                needLoad.put(twinEntity.getId(), twinEntity);
            }
        }
        if (needLoad.isEmpty()) {
            return;
        }
        List<TwinFieldAttributeEntity> twinFieldAttributeEntityList = twinFieldAttributeRepository.findByTwinIdIn(needLoad.keySet());

        Map<UUID, List<TwinFieldAttributeEntity>> attributesByTwinId = twinFieldAttributeEntityList.stream().collect(Collectors.groupingBy(TwinFieldAttributeEntity::getTwinId));

        for (Map.Entry<UUID, List<TwinFieldAttributeEntity>> entry : attributesByTwinId.entrySet()) {
            TwinEntity twinEntity = needLoad.get(entry.getKey());
            if (twinEntity != null) {
                Kit<TwinFieldAttributeEntity, UUID> kit = new Kit<>(TwinFieldAttributeEntity::getId);
                kit.addAll(entry.getValue());
                twinEntity.setTwinFieldAttributeKit(kit);
            }

        }
    }

    public void deleteAttributes(List<TwinFieldAttributeEntity> entities, TwinChangesCollector twinChangesCollector) {
        for (TwinFieldAttributeEntity twinFieldAttributeEntity : entities) {
            twinChangesCollector.delete(twinFieldAttributeEntity);
        }
    }

    public void updateAttributes(List<TwinFieldAttributeEntity> entitiyList, TwinChangesCollector twinChangesCollector) {
        Kit<TwinFieldAttributeEntity, UUID> newAttributeKit = new Kit<>(entitiyList, TwinFieldAttributeEntity::getId);
        Kit<TwinFieldAttributeEntity, UUID> dbAttributeKit = new Kit<>(twinFieldAttributeRepository.findByIdIn(newAttributeKit.getIdSet()), TwinFieldAttributeEntity::getId);

        TwinFieldAttributeEntity dbAttributeEntity;
        for (TwinFieldAttributeEntity attributeEntity : entitiyList) {
            dbAttributeEntity = dbAttributeKit.get(attributeEntity.getId());
            twinChangesCollector.collectIfChanged(dbAttributeEntity, TwinFieldAttributeEntity.Fields.twinId, dbAttributeEntity.getTwinId(), attributeEntity.getTwinId());
            twinChangesCollector.collectIfChanged(dbAttributeEntity, TwinFieldAttributeEntity.Fields.twinClassFieldId, dbAttributeEntity.getTwinClassFieldId(), attributeEntity.getTwinClassFieldId());
            twinChangesCollector.collectIfChanged(dbAttributeEntity, TwinFieldAttributeEntity.Fields.twinClassFieldAttributeId, dbAttributeEntity.getTwinClassFieldAttributeId(), attributeEntity.getTwinClassFieldAttributeId());
            twinChangesCollector.collectIfChanged(dbAttributeEntity, TwinFieldAttributeEntity.Fields.noteMsg, dbAttributeEntity.getNoteMsg(), attributeEntity.getNoteMsg());
            twinChangesCollector.collectIfChanged(dbAttributeEntity, TwinFieldAttributeEntity.Fields.noteMsgContext, dbAttributeEntity.getNoteMsgContext(), attributeEntity.getNoteMsgContext());
            twinChangesCollector.collectIfChanged(dbAttributeEntity, TwinFieldAttributeEntity.Fields.changedAt, dbAttributeEntity.getChangedAt(), Timestamp.valueOf(LocalDateTime.now()));
        }
    }

    public List<TwinFieldAttributeEntity> findByClassFieldIdSet(Set<UUID> classFieldIdSet) {
        return twinFieldAttributeRepository.findByTwinClassFieldIdIn(classFieldIdSet);
    }
}
