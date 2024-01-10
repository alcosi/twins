package org.twins.core.service.attachment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.Kit;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinAttachmentRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService {
    final EntitySmartService entitySmartService;
    final TwinAttachmentRepository twinAttachmentRepository;
    final UserService userService;

    public UUID checkAttachmentId(UUID attachmentId, EntitySmartService.CheckMode checkMode) throws ServiceException {
        return entitySmartService.check(attachmentId, twinAttachmentRepository, checkMode);
    }

    public TwinAttachmentEntity findAttachment(UUID attachmentId, EntitySmartService.FindMode findMode) throws ServiceException {
        return entitySmartService.findById(attachmentId, twinAttachmentRepository, findMode);
    }

    @Transactional
    public List<TwinAttachmentEntity> addAttachments(UUID twinId, UserEntity userEntity, List<TwinAttachmentEntity> attachments) throws ServiceException {
        for (TwinAttachmentEntity attachmentEntity : attachments) {
            attachmentEntity
                    .setTwinId(twinId)
                    .setCreatedAt(Timestamp.from(Instant.now()))
                    .setCreatedByUserId(userEntity.getId())
                    .setCreatedByUser(userEntity);
        }
        return IterableUtils.toList(entitySmartService.saveAllAndLog(attachments, twinAttachmentRepository));
    }

    @Transactional
    public TwinAttachmentEntity addAttachment(TwinAttachmentEntity twinAttachmentEntity) {
        TwinAttachmentEntity ret = twinAttachmentRepository.save(
                twinAttachmentEntity
                        .setCreatedAt(Timestamp.from(Instant.now())));
        log.info(ret.easyLog(EasyLoggable.Level.NORMAL) + " was saved");
        return ret;
    }

    public List<TwinAttachmentEntity> findAttachmentByTwinId(UUID twinId) {
        return twinAttachmentRepository.findByTwinId(twinId);
    }

    public Kit<TwinAttachmentEntity> loadAttachments(TwinEntity twinEntity) {
        if (twinEntity.getAttachmentKit() != null)
            return twinEntity.getAttachmentKit();
        List<TwinAttachmentEntity> attachmentEntityList = twinAttachmentRepository.findByTwinId(twinEntity.getId());
        if (attachmentEntityList != null)
            twinEntity.setAttachmentKit(new Kit<>(attachmentEntityList, TwinAttachmentEntity::getId));
        return twinEntity.getAttachmentKit();
    }

    public void loadAttachments(Collection<TwinEntity> twinEntityList) {
        Map<UUID, TwinEntity> needLoad = new HashMap<>();
        for (TwinEntity twinEntity : twinEntityList)
            if (twinEntity.getAttachmentKit() == null)
                needLoad.put(twinEntity.getId(), twinEntity);
        if (needLoad.size() == 0)
            return;
        List<TwinAttachmentEntity> attachmentEntityList = twinAttachmentRepository.findByTwinIdIn(needLoad.keySet());
        if (CollectionUtils.isEmpty(attachmentEntityList))
            return;
        Map<UUID, List<TwinAttachmentEntity>> attachmentMap = new HashMap<>(); // key - twinId
        for (TwinAttachmentEntity attachmentEntity : attachmentEntityList) { //grouping by twin
            attachmentMap.computeIfAbsent(attachmentEntity.getTwinId(), k -> new ArrayList<>());
            attachmentMap.get(attachmentEntity.getTwinId()).add(attachmentEntity);
        }
        TwinEntity twinEntity;
        for (Map.Entry<UUID, List<TwinAttachmentEntity>> entry : attachmentMap.entrySet()) {
            twinEntity = needLoad.get(entry.getKey());
            twinEntity.setAttachmentKit(new Kit<>(entry.getValue(), TwinAttachmentEntity::getId));
        }
    }

    @Transactional
    public void deleteById(ApiUser apiUser, UUID attachmentId) throws ServiceException {
        entitySmartService.deleteAndLog(attachmentId, twinAttachmentRepository);
    }

    @Transactional
    public void updateAttachments(List<TwinAttachmentEntity> attachmentEntityList) throws ServiceException {
        if (CollectionUtils.isEmpty(attachmentEntityList))
            return;
        ChangesHelper changesHelper = new ChangesHelper();
        TwinAttachmentEntity dbAttachmentEntity;
        for (TwinAttachmentEntity attachmentEntity : attachmentEntityList) {
            changesHelper.flush();
            dbAttachmentEntity = entitySmartService.findById(attachmentEntity.getId(), twinAttachmentRepository, EntitySmartService.FindMode.ifEmptyThrows);
            if (changesHelper.isChanged("description", dbAttachmentEntity.getDescription(), attachmentEntity.getDescription())) {
                dbAttachmentEntity.setDescription(attachmentEntity.getDescription());
            }
            if (changesHelper.isChanged("title", dbAttachmentEntity.getTitle(), attachmentEntity.getTitle())) {
                dbAttachmentEntity.setTitle(attachmentEntity.getTitle());
            }
            if (changesHelper.isChanged("storageLink", dbAttachmentEntity.getStorageLink(), attachmentEntity.getStorageLink())) {
                dbAttachmentEntity.setStorageLink(attachmentEntity.getStorageLink());
            }
            if (changesHelper.isChanged("externalId", dbAttachmentEntity.getExternalId(), attachmentEntity.getExternalId())) {
                dbAttachmentEntity.setExternalId(attachmentEntity.getExternalId());
            }
            if (changesHelper.hasChanges()) {
                twinAttachmentRepository.save(dbAttachmentEntity);
                log.info(dbAttachmentEntity.easyLog(EasyLoggable.Level.NORMAL) + " was updated: " + changesHelper.collectForLog());
            }
        }
    }

    private String createChangesLogString(String field, String oldValue, String newValue) {
        return field + " was changed from[" + oldValue + "] to[" + newValue + "]";
    }

    @Transactional
    public void deleteAttachments(UUID twinId, List<UUID> attachmentDeleteUUIDList) {
        if (CollectionUtils.isEmpty(attachmentDeleteUUIDList))
            return;
        attachmentDeleteUUIDList.forEach(aId -> log.info("Attachment[" +  aId + "] will be deleted (if present)"));
        twinAttachmentRepository.deleteAllByTwinIdAndIdIn(twinId, attachmentDeleteUUIDList);
    }
}
