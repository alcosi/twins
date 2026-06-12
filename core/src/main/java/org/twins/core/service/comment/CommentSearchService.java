package org.twins.core.service.comment;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dao.comment.TwinCommentRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.apiuser.DBUMembershipCheck;
import org.twins.core.domain.search.CommentSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.CommentGroupField;
import org.twins.core.enums.sort.CommentSortField;
import org.twins.core.service.EntitySearchService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.usergroup.UserGroupService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.*;

@Service
@Lazy
@RequiredArgsConstructor
public class CommentSearchService extends EntitySearchService<        CommentSearch,        TwinCommentEntity,        CommentSortField,        CommentGroupField> {

    private final AuthService authService;
    private final PermissionService permissionService;
    private final UserGroupService userGroupService;
    private final TwinCommentRepository commentRepository;

    @Override
    public JpaSpecificationExecutor<TwinCommentEntity> jpaSpecificationExecutor() {
        return commentRepository;
    }

    @Override
    public CommentSearch emptySearch() {
        return new CommentSearch();
    }

    @Override
    protected Class<TwinCommentEntity> entityClass() {
        return TwinCommentEntity.class;
    }

    @Override
    protected TwinCommentEntity newEntity() {
        return new TwinCommentEntity();
    }

    @Override
    public Specification<TwinCommentEntity> createFilterSpecification(
            CommentSearch search, UUID domainId, Locale locale) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        Specification<TwinCommentEntity> specification = Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, TwinCommentEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, true, TwinCommentEntity.Fields.id),
                checkUuidIn(search.getTwinIdList(), false, false, TwinCommentEntity.Fields.twinId),
                checkUuidIn(search.getTwinIdExcludeList(), true, true, TwinCommentEntity.Fields.twinId),
                checkUuidIn(search.getCreatedByUserIdList(), false, false, TwinCommentEntity.Fields.createdByUserId),
                checkUuidIn(search.getCreatedByUserIdExcludeList(), true, true, TwinCommentEntity.Fields.createdByUserId),
                checkFieldLikeIn(search.getTextLikeList(), false, false, TwinCommentEntity.Fields.text),
                checkFieldLikeIn(search.getTextNotLikeList(), true, false, TwinCommentEntity.Fields.text),
                checkFieldLocalDateTimeBetween(search.getCreatedAt(), TwinCommentEntity.Fields.createdAt),
                checkFieldLocalDateTimeBetween(search.getUpdatedAt(), TwinCommentEntity.Fields.changedAt)
        );
        if (!permissionService.currentUserHasPermission(Permissions.DOMAIN_TWINS_VIEW_ALL)) {
            userGroupService.loadGroupsForCurrentUser();
            specification = specification
                    .and(checkPermissions(apiUser.getDomainId(), apiUser.getBusinessAccountId(), apiUser.getUserId(), apiUser.getUser().getUserGroupsFootprint(), TwinCommentEntity.Fields.twin))
                    .and(checkClass(java.util.List.of(), apiUser, DBUMembershipCheck.BLOCKED, TwinCommentEntity.Fields.twin));
        } else {
            specification = specification
                    .and(checkFieldUuid(apiUser.getDomainId(), TwinCommentEntity.Fields.twin, TwinEntity.Fields.twinClass, TwinClassEntity.Fields.domainId));
        }
        return specification;
    }

    @Override
    public Specification<TwinCommentEntity> createSortSpecification(
            CommentSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        if (sortField == null)
            sortField = CommentSortField.createdAt;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case createdAt -> toSortSpecification(ascending, TwinCommentEntity.Fields.createdAt);
            case changedAt -> toSortSpecification(ascending, TwinCommentEntity.Fields.changedAt);
            case authorUserName -> toSortSpecification(ascending, TwinCommentEntity.Fields.createdByUser, UserEntity.Fields.name);
            case twinName -> toSortSpecification(ascending, TwinCommentEntity.Fields.twin, TwinEntity.Fields.name);
        };
    }

    @Override
    public String convertToEntityField(CommentGroupField groupField) throws ServiceException {
        return switch (groupField) {
            case twinId -> TwinCommentEntity.Fields.twinId;
            case createdByUserId -> TwinCommentEntity.Fields.createdByUserId;
        };
    }

    @Override
    public void mapGroupedField(TwinCommentEntity entity, CommentGroupField field, Object o) {
        switch (field) {
            case twinId -> entity.setTwinId((UUID) o);
            case createdByUserId -> entity.setCreatedByUserId((UUID) o);
        }
    }
}
