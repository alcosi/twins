package org.twins.core.service.comment;

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
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.attachment.TwinAttachmentRepository;
import org.twins.core.dao.comment.TwinCommentAction;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dao.comment.TwinCommentRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.EntityCUD;
import org.twins.core.domain.search.CommentSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.comment.CommentSpecification.*;


@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
public class CommentService extends EntitySecureFindServiceImpl<TwinCommentEntity> {
    final AuthService authService;
    final EntitySmartService entitySmartService;
    final AttachmentService attachmentService;
    final TwinService twinService;
    final PermissionService permissionService;
    final TwinCommentRepository commentRepository;
    final TwinAttachmentRepository attachmentRepository;
    final CommentActionService commentActionService;


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
        attachmentService.addAttachments(attachmentList, twinEntity);
        return comment.setAttachmentKit(new Kit<>(attachmentList, TwinAttachmentEntity::getId));
    }

    @Transactional
    public TwinCommentEntity updateComment(UUID commentId, String commentText, EntityCUD<TwinAttachmentEntity> attachmentCUD) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
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
        if (changesHelper.hasChanges())
            entitySmartService.saveAndLogChanges(currentComment, commentRepository, changesHelper);
        return currentComment;
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

    @Transactional(readOnly = true)
    public PaginationResult<TwinCommentEntity> findCommentForDomain(CommentSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<TwinCommentEntity> spec = createCommentSearchSpecification(search)
                .and(checkDomainId(domainId));
        Page<TwinCommentEntity> ret = commentRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinCommentEntity> createCommentSearchSpecification(CommentSearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        Specification<TwinCommentEntity> specification = Specification.allOf(
                checkUuidIn(TwinCommentEntity.Fields.id, search.getIdList(), false, false),
                checkUuidIn(TwinCommentEntity.Fields.id, search.getIdExcludeList(), true, true),
                checkUuidIn(TwinCommentEntity.Fields.twinId, search.getTwinIdList(), false, false),
                checkUuidIn(TwinCommentEntity.Fields.twinId, search.getTwinIdExcludeList(), true, true),
                checkUuidIn(TwinCommentEntity.Fields.createdByUserId, search.getCreatedByUserIdList(), false, false),
                checkUuidIn(TwinCommentEntity.Fields.createdByUserId, search.getCreatedByUserIdExcludeList(), true, true),
                checkFieldLikeIn(TwinCommentEntity.Fields.text, search.getTextLikeList(), false, false),
                checkFieldLikeIn(TwinCommentEntity.Fields.text, search.getTextNotLikeList(), true, false),
                localDateTimeBetween(TwinCommentEntity.Fields.createdAt, search.getCreatedAt()),
                localDateTimeBetween(TwinCommentEntity.Fields.changedAt, search.getUpdatedAt())
        );
        if (!permissionService.currentUserHasPermission(Permissions.DOMAIN_TWINS_VIEW_ALL)) {
            specification = specification
                    .and(checkPermissions(apiUser.getDomainId(), apiUser.getBusinessAccountId(), apiUser.getUserId(), apiUser.getUserGroups()));
        } else {
            specification = specification
                    .and(checkDomainId(apiUser.getDomainId()));
        }
        return specification;
    }
}
