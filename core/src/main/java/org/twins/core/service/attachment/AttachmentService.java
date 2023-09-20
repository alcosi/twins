package org.twins.core.service.attachment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinAttachmentRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.EntitySmartService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService {
    final EntitySmartService entitySmartService;
    final TwinAttachmentRepository twinAttachmentRepository;

    public UUID checkAttachmentId(UUID attachmentId, EntitySmartService.CheckMode checkMode) throws ServiceException {
        return entitySmartService.check(attachmentId, "attachmentId", twinAttachmentRepository, checkMode);
    }

    public TwinAttachmentEntity findAttachment(UUID attachmentId, EntitySmartService.FindMode findMode) throws ServiceException {
        return entitySmartService.findById(attachmentId, "attachmentId", twinAttachmentRepository, findMode);
    }

    public List<TwinAttachmentEntity> addAttachments(UUID twinId, UUID userId, List<TwinAttachmentEntity> attachments) {
        for (TwinAttachmentEntity attachmentEntity : attachments) {
            attachmentEntity
                    .setTwinId(twinId)
                    .setCreatedAt(Timestamp.from(Instant.now()))
                    .setCreatedByUserId(userId);
        }
        return IterableUtils.toList(twinAttachmentRepository.saveAll(attachments));
    }

    public TwinAttachmentEntity addAttachment(TwinAttachmentEntity twinAttachmentEntity) {
        return twinAttachmentRepository.save(
                twinAttachmentEntity
                        .setCreatedAt(Timestamp.from(Instant.now())));
    }

    public List<TwinAttachmentEntity> findAttachmentByTwinId(UUID twinId) {
        return twinAttachmentRepository.findByTwinId(twinId);
    }

    public void deleteById(ApiUser apiUser, UUID attachmentId) {
        twinAttachmentRepository.deleteById(attachmentId);
    }


}
