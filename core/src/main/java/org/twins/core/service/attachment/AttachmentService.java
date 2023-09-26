package org.twins.core.service.attachment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.cambium.common.exception.ServiceException;
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
        return entitySmartService.check(attachmentId, "attachmentId", twinAttachmentRepository, checkMode);
    }

    public TwinAttachmentEntity findAttachment(UUID attachmentId, EntitySmartService.FindMode findMode) throws ServiceException {
        return entitySmartService.findById(attachmentId, "attachmentId", twinAttachmentRepository, findMode);
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
        return IterableUtils.toList(twinAttachmentRepository.saveAll(attachments));
    }

    @Transactional
    public TwinAttachmentEntity addAttachment(TwinAttachmentEntity twinAttachmentEntity) {
        return twinAttachmentRepository.save(
                twinAttachmentEntity
                        .setCreatedAt(Timestamp.from(Instant.now())));
    }

    public List<TwinAttachmentEntity> findAttachmentByTwinId(UUID twinId) {
        return twinAttachmentRepository.findByTwinId(twinId);
    }

    @Transactional
    public void deleteById(ApiUser apiUser, UUID attachmentId) {
        twinAttachmentRepository.deleteById(attachmentId);
    }

    @Transactional
    public void updateAttachments(List<TwinAttachmentEntity> attachmentEntityList) throws ServiceException {
        if (CollectionUtils.isEmpty(attachmentEntityList))
            return;
        boolean doUpdate;
        TwinAttachmentEntity dbAttachmentEntity;
        for (TwinAttachmentEntity attachmentEntity : attachmentEntityList) {
            doUpdate = false;
            dbAttachmentEntity = entitySmartService.findById(attachmentEntity.getId(), "twinAttachment", twinAttachmentRepository, EntitySmartService.FindMode.ifEmptyThrows);
            if (attachmentEntity.getDescription() != null && !attachmentEntity.getDescription().equals(dbAttachmentEntity.getDescription())) {
                dbAttachmentEntity.setDescription(attachmentEntity.getDescription());
                doUpdate = true;
            }
            if (attachmentEntity.getTitle() != null && !attachmentEntity.getTitle().equals(dbAttachmentEntity.getTitle())) {
                dbAttachmentEntity.setTitle(attachmentEntity.getTitle());
                doUpdate = true;
            }
            if (attachmentEntity.getStorageLink() != null && !attachmentEntity.getStorageLink().equals(dbAttachmentEntity.getStorageLink())) {
                dbAttachmentEntity.setStorageLink(attachmentEntity.getStorageLink());
                doUpdate = true;
            }
            if (attachmentEntity.getExternalId() != null && !attachmentEntity.getExternalId().equals(dbAttachmentEntity.getExternalId())) {
                dbAttachmentEntity.setExternalId(attachmentEntity.getExternalId());
                doUpdate = true;
            }
            if (doUpdate)
                twinAttachmentRepository.save(dbAttachmentEntity);
        }

    }

    @Transactional
    public void deleteAttachments(UUID twinId, List<UUID> attachmentDeleteUUIDList) {
        if (CollectionUtils.isEmpty(attachmentDeleteUUIDList))
            return;
        twinAttachmentRepository.deleteAllByTwinIdAndIdIn(twinId, attachmentDeleteUUIDList);
    }
}
