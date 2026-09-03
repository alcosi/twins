package org.twins.core.service.attachment;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionRepository;
import org.twins.core.domain.search.AttachmentRestrictionSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.AttachmentRestrictionSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.*;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentRestrictionSearchService extends EntitySearchService
        <AttachmentRestrictionSearch, TwinAttachmentRestrictionEntity, AttachmentRestrictionSortField, Void> {
    private final TwinAttachmentRestrictionRepository twinAttachmentRestrictionRepository;

    @Override
    public JpaSpecificationExecutor<TwinAttachmentRestrictionEntity> jpaSpecificationExecutor() {
        return twinAttachmentRestrictionRepository;
    }

    @Override
    public AttachmentRestrictionSearch emptySearch() {
        return new AttachmentRestrictionSearch();
    }

    @Override
    protected TwinAttachmentRestrictionEntity newEntity() {
        return new TwinAttachmentRestrictionEntity();
    }

    @Override
    protected Class<TwinAttachmentRestrictionEntity> entityClass() {
        return TwinAttachmentRestrictionEntity.class;
    }

    @Override
    public Specification<TwinAttachmentRestrictionEntity> createFilterSpecification(AttachmentRestrictionSearch search, UUID domainId, Locale locale) throws ServiceException {
        return Specification.allOf(
                checkFieldUuid(domainId, TwinAttachmentRestrictionEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, TwinAttachmentRestrictionEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinAttachmentRestrictionEntity.Fields.id),
                checkFieldIntegerRange(search.getMinCountRange(), TwinAttachmentRestrictionEntity.Fields.minCount),
                checkFieldIntegerRange(search.getMaxCountRange(), TwinAttachmentRestrictionEntity.Fields.maxCount),
                checkFieldIntegerRange(search.getFileSizeMbLimitRange(), TwinAttachmentRestrictionEntity.Fields.fileSizeMbLimit),
                checkFieldLikeIn(search.getFileExtensionLimitLikeList(), false, true, TwinAttachmentRestrictionEntity.Fields.fileExtensionLimit),
                checkFieldLikeIn(search.getFileExtensionLimitNotLikeList(), true, true, TwinAttachmentRestrictionEntity.Fields.fileExtensionLimit),
                checkFieldLikeIn(search.getFileNameRegexpLikeList(), false, true, TwinAttachmentRestrictionEntity.Fields.fileNameRegexp),
                checkFieldLikeIn(search.getFileNameRegexpNotLikeList(), true, true, TwinAttachmentRestrictionEntity.Fields.fileNameRegexp));
    }

    @Override
    protected AttachmentRestrictionSortField defaultSortField() {
        return AttachmentRestrictionSortField.minCount;
    }

    @Override
    public Specification<TwinAttachmentRestrictionEntity> createSortSpecification(AttachmentRestrictionSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case minCount -> toSortSpecification(ascending, TwinAttachmentRestrictionEntity.Fields.minCount);
            case maxCount -> toSortSpecification(ascending, TwinAttachmentRestrictionEntity.Fields.maxCount);
            case fileSizeMbLimit -> toSortSpecification(ascending, TwinAttachmentRestrictionEntity.Fields.fileSizeMbLimit);
        };
    }

    @Override
    public String convertToEntityField(Void groupField) throws ServiceException {
        // grouping is not supported for attachment restriction
        return null;
    }

    @Override
    public void mapGroupedField(TwinAttachmentRestrictionEntity entity, Void field, Object o) {
        // grouping is not supported for attachment restriction
    }
}
