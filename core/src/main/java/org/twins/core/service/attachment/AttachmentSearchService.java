package org.twins.core.service.attachment;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.attachment.TwinAttachmentRepository;
import org.twins.core.dao.i18n.specifications.I18nSpecification;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.search.AttachmentSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.AttachmentGroupField;
import org.twins.core.enums.sort.AttachmentSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class AttachmentSearchService extends EntitySearchService
        <AttachmentSearch, TwinAttachmentEntity, AttachmentSortField, AttachmentGroupField> {

    private final TwinAttachmentRepository twinAttachmentRepository;

    @Override
    public JpaSpecificationExecutor<TwinAttachmentEntity> jpaSpecificationExecutor() {
        return twinAttachmentRepository;
    }

    @Override
    public AttachmentSearch emptySearch() {
        return new AttachmentSearch();
    }

    @Override
    protected TwinAttachmentEntity newEntity() {
        return new TwinAttachmentEntity();
    }

    @Override
    protected Class<TwinAttachmentEntity> entityClass() {
        return TwinAttachmentEntity.class;
    }

    @Override
    public Specification<TwinAttachmentEntity> createFilterSpecification(AttachmentSearch search, UUID domainId, Locale locale) {
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

    @Override
    public Specification<TwinAttachmentEntity> createSortSpecification(AttachmentSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        if (sortField == null)
            sortField = AttachmentSortField.createdAt;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case createdAt -> toSortSpecification(ascending, TwinAttachmentEntity.Fields.createdAt);
            case externalId -> toSortSpecification(ascending, TwinAttachmentEntity.Fields.externalId);
            case size -> toSortSpecification(ascending, TwinAttachmentEntity.Fields.size);
            case order -> toSortSpecification(ascending, TwinAttachmentEntity.Fields.order);
            case twinName -> toSortSpecification(ascending, TwinAttachmentEntity.Fields.twinSpecOnly, TwinEntity.Fields.name);
            case twinClassFieldName -> I18nSpecification.toSortSpecificationDirect(ascending, locale, TwinAttachmentEntity.Fields.twinClassFieldSpecOnly, TwinClassFieldEntity.Fields.nameI18nTranslationsSpecOnly);
            case authorUserName -> toSortSpecification(ascending, TwinAttachmentEntity.Fields.createdByUserSpecOnly, UserEntity.Fields.name);
            case twinflowTransitionName -> I18nSpecification.toSortSpecification(ascending, locale, TwinAttachmentEntity.Fields.twinflowTransitionSpecOnly, TwinflowTransitionEntity.Fields.nameI18n);
            case viewPermissionName -> I18nSpecification.toSortSpecificationDirect(ascending, locale, TwinAttachmentEntity.Fields.viewPermissionSpecOnly, PermissionEntity.Fields.nameI18nTranslationsSpecOnly);
        };
    }

    @Override
    public String convertToEntityField(AttachmentGroupField groupField) {
        return switch (groupField) {
            case twinId -> TwinAttachmentEntity.Fields.twinId;
            case twinflowTransitionId -> TwinAttachmentEntity.Fields.twinflowTransitionId;
            case viewPermissionId -> TwinAttachmentEntity.Fields.viewPermissionId;
            case createdByUserId -> TwinAttachmentEntity.Fields.createdByUserId;
            case twinCommentId -> TwinAttachmentEntity.Fields.twinCommentId;
            case twinClassFieldId -> TwinAttachmentEntity.Fields.twinClassFieldId;
            case storageId -> TwinAttachmentEntity.Fields.storageId;
        };
    }

    @Override
    public void mapGroupedField(TwinAttachmentEntity entity, AttachmentGroupField field, Object o) {
        switch (field) {
            case twinId -> entity.setTwinId((UUID) o);
            case twinflowTransitionId -> entity.setTwinflowTransitionId((UUID) o);
            case viewPermissionId -> entity.setViewPermissionId((UUID) o);
            case createdByUserId -> entity.setCreatedByUserId((UUID) o);
            case twinCommentId -> entity.setTwinCommentId((UUID) o);
            case twinClassFieldId -> entity.setTwinClassFieldId((UUID) o);
            case storageId -> entity.setStorageId((UUID) o);
        }
    }
}
