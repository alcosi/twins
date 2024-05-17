package org.twins.core.service.comment;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.PaginationUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.EntityCUD;
import org.twins.core.domain.comment.CommentListResult;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.pagination.SimplePagination;
import org.twins.core.service.twin.TwinService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
public class CommentService extends EntitySecureFindServiceImpl<TwinCommentEntity> {
    final AuthService authService;
    final EntitySmartService entitySmartService;
    final AttachmentService attachmentService;
    final TwinService twinService;
    final TwinCommentRepository commentRepository;
    final TwinAttachmentRepository attachmentRepository;

    @Transactional
    public TwinCommentEntity createComment(TwinCommentEntity comment, List<TwinAttachmentEntity> attachmentList) throws ServiceException {
        if (comment.getText() == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_COMMENT_FIELD_TEXT_IS_NULL);
        ApiUser apiUser = authService.getApiUser();
        UUID userId = apiUser.getUser().getId();
        comment.setCreatedByUserId(userId);
        comment.setCreatedByUser(apiUser.getUser());
        TwinEntity twinEntity = twinService.findEntitySafe(comment.getTwinId());
        entitySmartService.save(comment, commentRepository, EntitySmartService.SaveMode.saveAndLogOnException);
        if (CollectionUtils.isEmpty(attachmentList))
            return comment;
        addCommentIdInAttachments(comment.getId(), attachmentList);
        attachmentService.addAttachments(twinEntity, apiUser.getUser(), attachmentList);
        return comment.setAttachmentKit(new Kit<>(attachmentList, TwinAttachmentEntity::getId));
    }

    @Transactional
    public TwinCommentEntity updateComment(UUID commentId, String commentText, EntityCUD<TwinAttachmentEntity> attachmentCUD) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        TwinCommentEntity currentComment = findEntitySafe(commentId);
        if (!apiUser.getUser().getId().equals(currentComment.getCreatedByUserId()))
            throw new ServiceException(ErrorCodeTwins.TWIN_COMMENT_EDIT_ACCESS_DENIED, "This comment belongs to another user. Editing is not possible");
        if (!currentComment.getText().equals(commentText)) {
            currentComment
                    .setText(commentText)
                    .setChangedAt(Timestamp.from(Instant.now()));
        }
        if (attachmentCUD == null)
            return currentComment;
        addCommentIdInAttachments(commentId, attachmentCUD.getCreateList());
        addCommentIdInAttachments(commentId, attachmentCUD.getUpdateList());
        TwinEntity twinEntity = twinService.findEntitySafe(currentComment.getTwinId());
        attachmentService.addAttachments(twinEntity, apiUser.getUser(), attachmentCUD.getCreateList());
        attachmentService.updateAttachments(attachmentCUD.getUpdateList());
        attachmentService.deleteAttachments(currentComment.getTwinId(), attachmentCUD.getDeleteUUIDList());
        return currentComment;
    }

    public CommentListResult findComment(UUID twinId, SimplePagination pagination) throws ServiceException {
        Page<TwinCommentEntity> commentList = commentRepository.findAllByTwinId(twinId, PaginationUtils.pageableOffset(pagination));
        return convertPageInSearchResult(commentList, pagination);

    }

    private CommentListResult convertPageInSearchResult(Page<TwinCommentEntity> commentList, SimplePagination pagination) {
        return (CommentListResult) new CommentListResult()
                .setCommentList(commentList.toList())
                .setTotal(commentList.getTotalElements())
                .setOffset(pagination.getOffset())
                .setLimit(pagination.getLimit());
    }

    public void deleteComment(UUID commentId) throws ServiceException {
        // attachments will be deleted by cascade by fk
        entitySmartService.deleteAndLog(commentId, commentRepository);
    }

    private void addCommentIdInAttachments(UUID commentId, List<TwinAttachmentEntity> attachmentList) {
        if (CollectionUtils.isEmpty(attachmentList))
            return;
        attachmentList.forEach(attachment -> {
            attachment.setTwinCommentId(commentId);
        });
    }

    public Kit<TwinAttachmentEntity, UUID> loadAttachments(TwinCommentEntity twinComment) {
        if (twinComment.getAttachmentKit() != null)
            return twinComment.getAttachmentKit();
        List<TwinAttachmentEntity> attachmentEntityList = attachmentRepository.findByTwinCommentId(twinComment.getId());
        if (attachmentEntityList != null)
            twinComment.setAttachmentKit(new Kit<>(attachmentEntityList, TwinAttachmentEntity::getId));
        return twinComment.getAttachmentKit();
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

    @Data
    @Accessors(chain = true)
    public static class CommentCreateResult{
        private UUID commentId;
        private List<UUID> attachments;
    }

    @Override
    public CrudRepository<TwinCommentEntity, UUID> entityRepository() {
        return commentRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinCommentEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinCommentEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        //todo validate
        return true;
    }

}
