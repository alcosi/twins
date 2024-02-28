package org.twins.core.service.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.Kit;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.PaginationUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.EntityCUD;
import org.twins.core.domain.comment.CommentListResult;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.auth.AuthService;

import java.sql.Timestamp;
import java.util.*;

@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
public class CommentService {
    final AuthService authService;
    final EntitySmartService entitySmartService;
    final AttachmentService attachmentService;
    final TwinRepository twinRepository;
    final TwinCommentRepository commentRepository;
    final TwinAttachmentRepository attachmentRepository;

    public void createComment(TwinCommentEntity comment, List<TwinAttachmentEntity> attachmentList) throws ServiceException {
        if (comment.getText() == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_COMMENT_FIELD_TEXT_IS_NULL);
        ApiUser apiUser = authService.getApiUser();
        UUID userId = apiUser.getUser().getId();
        comment.setCreatedByUserId(userId);
        entitySmartService.save(comment, commentRepository, EntitySmartService.SaveMode.saveAndLogOnException);
        addAttachmentsByCommentId(comment.getId(), comment.getTwinId(), userId, attachmentList);
    }

    @Transactional
    public void updateComment(UUID commentId, String commentText, EntityCUD<TwinAttachmentEntity> attachmentUpdate) throws ServiceException {
        TwinCommentEntity currentComment = entitySmartService.findById(commentId, commentRepository, EntitySmartService.FindMode.ifEmptyThrows);
        if (!currentComment.getText().equals(commentText)) {
            currentComment.setText(commentText);
            currentComment.setChangedAt(new Timestamp(System.currentTimeMillis()));
        }
        addAttachmentsByCommentId(commentId, currentComment.getTwinId(), currentComment.getCreatedByUserId(), attachmentUpdate.getCreateList());
        attachmentService.updateAttachments(attachmentUpdate.getUpdateList());
        attachmentService.deleteAttachments(currentComment.getTwinId(), attachmentUpdate.getDeleteUUIDList());
    }

    public CommentListResult findComment(UUID twinId, Sort.Direction createdBySortDirection, int offset, int limit) throws ServiceException {
        Pageable pageable = PaginationUtils.paginationOffset(offset, limit, Sort.by(createdBySortDirection, TwinCommentEntity.Fields.createdAt));
        List<TwinCommentEntity> commentListByTwinId = commentRepository.findAllByTwinId(twinId, pageable);
        long totalElement = commentRepository.countByTwinId(twinId);
        return (CommentListResult) new CommentListResult()
                .setCommentList(commentListByTwinId)
                .setOffset(offset)
                .setLimit(limit)
                .setTotal(totalElement);
    }

    public void deleteComment(UUID commentId, UUID twinId) throws ServiceException {
        // attachments will be deleted by cascade by fk
        entitySmartService.check(commentId, commentRepository, EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS);
        entitySmartService.check(twinId, twinRepository, EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS);
        commentRepository.deleteByIdAndTwinId(commentId, twinId);
        log.info("comment[" + commentId + "] perhaps was deleted by twin id[" + twinId + "]");
    }

    private void addAttachmentsByCommentId(UUID commentId, UUID twinId, UUID userId, List<TwinAttachmentEntity> attachmentList) {
        if (CollectionUtils.isEmpty(attachmentList))
            return;
        attachmentList.forEach(attachment -> {
            attachment.setTwinId(twinId);
            attachment.setCreatedByUserId(userId);
            attachment.setTwinCommentId(commentId);
        });
        entitySmartService.saveAllAndLog(attachmentList, attachmentRepository);
    }

    public void loadAttachments(Collection<TwinCommentEntity> twinCommentList) {
        Map<UUID, TwinCommentEntity> needLoad = new HashMap<>();
        for (TwinCommentEntity twinComment : twinCommentList)
            if (twinComment.getAttachmentKit() == null)
                needLoad.put(twinComment.getId(), twinComment);
        if (needLoad.size() == 0)
            return;
        List<TwinAttachmentEntity> attachmentEntityList = attachmentRepository.findByTwinCommentIdIn(needLoad.keySet());
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(attachmentEntityList))
            return;
        Map<UUID, List<TwinAttachmentEntity>> attachmentMap = new HashMap<>(); // key - twinCommentId
        for (TwinAttachmentEntity attachmentEntity : attachmentEntityList) { //grouping by twinCommentId
            attachmentMap.computeIfAbsent(attachmentEntity.getTwinCommentId(), k -> new ArrayList<>());
            attachmentMap.get(attachmentEntity.getTwinCommentId()).add(attachmentEntity);
        }
        TwinCommentEntity twinComment;
        for (Map.Entry<UUID, List<TwinAttachmentEntity>> entry : attachmentMap.entrySet()) {
            twinComment = needLoad.get(entry.getKey());
            twinComment.setAttachmentKit(new Kit<>(entry.getValue(), TwinAttachmentEntity::getId));
        }
    }
}
