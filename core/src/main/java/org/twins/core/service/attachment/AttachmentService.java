package org.twins.core.service.attachment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinAttachmentRepository;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
