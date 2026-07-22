package org.twins.core.service.comment;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.PaginationUtils;
import org.cambium.common.util.StringUtils;
import org.cambium.common.util.UuidUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.attachment.TwinAttachmentRepository;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dao.comment.TwinCommentRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.EntityCUD;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.enums.comment.TwinCommentAction;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;



@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Lazy
@RequiredArgsConstructor
public class CommentService extends EntitySecureFindServiceImpl<TwinCommentEntity> {
    private final AuthService authService;
    private final EntitySmartService entitySmartService;
    private final AttachmentService attachmentService;
    private final HistoryService historyService;
    private final TwinService twinService;
    private final TwinCommentRepository commentRepository;
    private final TwinAttachmentRepository attachmentRepository;
    private final CommentActionService commentActionService;
    private final UserService userService;

    @Transactional(rollbackFor = Throwable.class)
    public TwinCommentEntity createComment(TwinCommentEntity comment, List<TwinAttachmentEntity> attachmentList) throws ServiceException {
        if (comment.getText() == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_COMMENT_FIELD_TEXT_IS_NULL);
        ApiUser apiUser = authService.getApiUser();
        TwinEntity twinEntity = twinService.findEntitySafe(comment.getTwinId());

        comment
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(apiUser.getUserId())
                .setCreatedByUser(apiUser.getUser())
                .setTwin(twinEntity);
        saveSafe(comment);

        addCommentHistory(comment, twinEntity);
        if (CollectionUtils.isEmpty(attachmentList))
            return comment;
        addCommentIdInAttachments(comment.getId(), attachmentList);
        attachmentService.addAttachments(attachmentList, twinEntity);
        return comment.setAttachmentKit(new Kit<>(attachmentList, TwinAttachmentEntity::getId));
    }

    @Transactional(rollbackFor = Throwable.class)
    public void createComment(TwinEntity twin, Set<String> comments, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (CollectionUtils.isEmpty(comments))
            return;
        ApiUser apiUser = authService.getApiUser();

        for (var commentText : comments) {
            if (StringUtils.isEmpty(commentText)) {
                log.info("Comment text is empty. Skipping comment creation.");
                continue;
            }
            TwinCommentEntity comment = new TwinCommentEntity()
                    .setId(UuidUtils.generate())
                    .setText(commentText)
                    .setCreatedAt(Timestamp.from(Instant.now()))
                    .setCreatedByUserId(apiUser.getUserId())
                    .setCreatedByUser(apiUser.getUser())
                    .setTwinId(twin.getId())
                    .setTwin(twin);
            twinChangesCollector.add(comment);
            addCommentHistory(comment, twin, twinChangesCollector);
        }
    }

    private void addCommentHistory(TwinCommentEntity comment, TwinEntity twin) throws ServiceException {
        TwinChangesCollector collector = new TwinChangesCollector();
        addCommentHistory(comment, twin, collector);
        historyService.saveHistory(collector.getHistoryCollector());
    }

    private void addCommentHistory(TwinCommentEntity comment, TwinEntity twin, TwinChangesCollector collector) {
        if (collector.isHistoryCollectorEnabled()) {
            collector.getHistoryCollector(twin).add(historyService.commentCreate(comment));
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinCommentEntity updateComment(UUID commentId, String commentText, EntityCUD<TwinAttachmentEntity> attachmentCUD) throws ServiceException {
        TwinCommentEntity currentComment = findEntitySafe(commentId);
        commentActionService.checkAllowed(currentComment, TwinCommentAction.EDIT);
        ChangesHelper changesHelper = new ChangesHelper();
        if (changesHelper.isChanged("text", currentComment.getText(), commentText)) {
            currentComment
                    .setText(commentText)
                    .setChangedAt(Timestamp.from(Instant.now()));
        }
        if (attachmentCUD != null) {
            addCommentIdInAttachments(commentId, attachmentCUD.getCreateList());
            addCommentIdInAttachments(commentId, attachmentCUD.getUpdateList());
            attachmentService.addAttachments(attachmentCUD.getCreateList(), currentComment.getTwin());
            attachmentService.updateAttachments(attachmentCUD.getUpdateList(), currentComment.getTwin());
            attachmentService.deleteAttachments(currentComment.getTwin(), attachmentCUD.getDeleteList());
        }
        return updateSafe(currentComment, changesHelper);
    }

    public PaginationResult<TwinCommentEntity> findComment(UUID twinId, SimplePagination pagination) throws ServiceException {
        Page<TwinCommentEntity> commentList = commentRepository.findAllByTwinId(twinId, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(commentList, pagination);
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
        loadKit(
                twinCommentList,
                TwinCommentEntity::getId,
                TwinCommentEntity::getAttachmentKit,
                TwinCommentEntity::setAttachmentKit,
                attachmentRepository::findByTwinCommentIdIn,
                TwinAttachmentEntity::getId,
                TwinAttachmentEntity::getTwinCommentId,
                TwinAttachmentEntity::setComment
        );
    }

    public void loadUser(TwinCommentEntity entity) throws ServiceException {
        loadUser(Collections.singletonList(entity));
    }

    public void loadUser(Collection<TwinCommentEntity> entities) throws ServiceException {
        userService.load(entities,
                TwinCommentEntity::getCreatedByUserId,
                TwinCommentEntity::getCreatedByUser,
                TwinCommentEntity::setCreatedByUser);
    }

    public void loadTwin(TwinCommentEntity entity) throws ServiceException {
        loadTwin(Collections.singletonList(entity));
    }

    public void loadTwin(Collection<TwinCommentEntity> entities) throws ServiceException {
        twinService.load(entities,
                TwinCommentEntity::getTwinId,
                TwinCommentEntity::getTwin,
                TwinCommentEntity::setTwin);
    }

    @Data
    @Accessors(chain = true)
    public static class CommentCreateResult {
        private UUID commentId;
        private List<UUID> attachments;
    }

    @Override
    public CrudRepository<TwinCommentEntity, UUID> entityRepository() {
        return commentRepository;
    }

    @Override
    public Function<TwinCommentEntity, UUID> entityGetIdFunction() {
        return TwinCommentEntity::getId;
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
