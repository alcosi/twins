package org.twins.core.service.attachment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinAttachmentRepository;

import java.sql.Timestamp;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService {
    final TwinAttachmentRepository twinAttachmentRepository;

    public TwinAttachmentEntity addAttachment(TwinAttachmentEntity twinAttachmentEntity) {
        return twinAttachmentRepository.save(
                twinAttachmentEntity
                        .setCreatedAt(Timestamp.from(Instant.now())));
    }
}
