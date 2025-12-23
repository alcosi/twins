package org.twins.core.service.attachment;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.attachment.TwinAttachmentRepository;
import org.twins.core.domain.search.AttachmentSearch;

import static org.twins.core.dao.specifications.CommonSpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class AttachmentSearchService {
    private final TwinAttachmentRepository twinAttachmentRepository;

    public PaginationResult<TwinAttachmentEntity> findAttachments(AttachmentSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinAttachmentEntity> spec = createAttachmentSearchSpecification(search);
        Page<TwinAttachmentEntity> ret = twinAttachmentRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinAttachmentEntity> createAttachmentSearchSpecification(AttachmentSearch search) {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, TwinAttachmentEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinAttachmentEntity.Fields.id),
                checkUuidIn(search.getTwinIdList(), false, false, TwinAttachmentEntity.Fields.twinId),
                checkUuidIn(search.getTwinIdExcludeList(), true, false, TwinAttachmentEntity.Fields.twinId),
                checkUuidIn(search.getTwinflowTransitionIdList(), false, false, TwinAttachmentEntity.Fields.twinflowTransitionId),
                checkUuidIn(search.getTwinflowTransitionIdExcludeList(), true, true, TwinAttachmentEntity.Fields.twinflowTransitionId),
                checkUuidIn(search.getCommentIdList(), false, false, TwinAttachmentEntity.Fields.twinCommentId),
                checkUuidIn(search.getCommentIdExcludeList(), true, true, TwinAttachmentEntity.Fields.twinCommentId),
                checkUuidIn(search.getTwinClassFieldIdList(), false, false, TwinAttachmentEntity.Fields.twinClassFieldId),
                checkUuidIn(search.getTwinClassFieldIdExcludeList(), true, true, TwinAttachmentEntity.Fields.twinClassFieldId),
                checkFieldLikeIn(search.getStorageLinkLikeList(), false, true, TwinAttachmentEntity.Fields.storageFileKey),
                checkFieldLikeIn(search.getStorageLinkNotLikeList(), true, true, TwinAttachmentEntity.Fields.storageFileKey),
                checkUuidIn(search.getViewPermissionIdList(), false, false, TwinAttachmentEntity.Fields.viewPermissionId),
                checkUuidIn(search.getViewPermissionIdExcludeList(), true, true, TwinAttachmentEntity.Fields.viewPermissionId),
                checkUuidIn(search.getCreatedByUserIdList(), false, false, TwinAttachmentEntity.Fields.createdByUserId),
                checkUuidIn(search.getCreatedByUserIdExcludeList(), true, true, TwinAttachmentEntity.Fields.createdByUserId),
                checkFieldLikeIn(search.getExternalIdLikeList(), false, true, TwinAttachmentEntity.Fields.externalId),
                checkFieldLikeIn(search.getExternalIdNotLikeList(), true, true, TwinAttachmentEntity.Fields.externalId),
                checkFieldLikeIn(search.getTitleLikeList(), false, true, TwinAttachmentEntity.Fields.title),
                checkFieldLikeIn(search.getTitleNotLikeList(), true, true, TwinAttachmentEntity.Fields.title),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TwinAttachmentEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinAttachmentEntity.Fields.description),
                checkFieldLocalDateTimeBetween(search.getCreatedAt(), TwinAttachmentEntity.Fields.createdAt),
                checkFieldLongRange(search.getOrder(), TwinAttachmentEntity.Fields.order)
        );
    }
}
