package org.twins.core.service.resource;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.resource.ResourceRepository;
import org.twins.core.domain.search.ResourceSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.ResourceGroupField;
import org.twins.core.enums.sort.ResourceSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.*;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceSearchService extends EntitySearchService
        <ResourceSearch, ResourceEntity, ResourceSortField, ResourceGroupField> {
    private final ResourceRepository resourceRepository;

    @Override
    public JpaSpecificationExecutor<ResourceEntity> jpaSpecificationExecutor() {
        return resourceRepository;
    }

    @Override
    public ResourceSearch emptySearch() {
        return new ResourceSearch();
    }

    @Override
    protected ResourceEntity newEntity() {
        return new ResourceEntity();
    }

    @Override
    protected Class<ResourceEntity> entityClass() {
        return ResourceEntity.class;
    }

    @Override
    public Specification<ResourceEntity> createFilterSpecification(ResourceSearch search, UUID domainId, Locale locale) throws ServiceException {
        // Domain isolation: the caller's domain comes from the DomainId header via EntitySearchService
        return Specification.allOf(
                checkFieldUuid(domainId, ResourceEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, ResourceEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, ResourceEntity.Fields.id),
                checkFieldLikeContainsIn(search.getOriginalFileNameLikeList(), false, true, ResourceEntity.Fields.originalFileName),
                checkFieldLikeContainsIn(search.getOriginalFileNameNotLikeList(), true, true, ResourceEntity.Fields.originalFileName),
                checkUuidIn(search.getUploadedByUserIdList(), false, false, ResourceEntity.Fields.uploadedByUserId),
                checkUuidIn(search.getUploadedByUserIdExcludeList(), true, false, ResourceEntity.Fields.uploadedByUserId),
                checkUuidIn(search.getStorageIdList(), false, false, ResourceEntity.Fields.storageId),
                checkUuidIn(search.getStorageIdExcludeList(), true, false, ResourceEntity.Fields.storageId),
                checkFieldLikeContainsIn(search.getStorageFileKeyLikeList(), false, true, ResourceEntity.Fields.storageFileKey),
                checkFieldLikeContainsIn(search.getStorageFileKeyNotLikeList(), true, true, ResourceEntity.Fields.storageFileKey),
                checkFieldLongRange(search.getSizeInBytes(), ResourceEntity.Fields.sizeInBytes),
                checkFieldLocalDateTimeBetween(search.getCreatedAt(), ResourceEntity.Fields.createdAt)
        );
    }

    @Override
    protected ResourceSortField defaultSortField() {
        return ResourceSortField.createdAt;
    }

    @Override
    public Specification<ResourceEntity> createSortSpecification(ResourceSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case originalFileName -> toSortSpecification(ascending, ResourceEntity.Fields.originalFileName);
            case sizeInBytes -> toSortSpecification(ascending, ResourceEntity.Fields.sizeInBytes);
            case createdAt -> toSortSpecification(ascending, ResourceEntity.Fields.createdAt);
            case uploadedByUserName -> toSortSpecification(ascending, ResourceEntity.Fields.uploadedByUser, UserEntity.Fields.name);
        };
    }

    @Override
    public String convertToEntityField(ResourceGroupField groupField) {
        return switch (groupField) {
            case uploadedByUserId -> ResourceEntity.Fields.uploadedByUserId;
            case storageId -> ResourceEntity.Fields.storageId;
        };
    }

    @Override
    public void mapGroupedField(ResourceEntity entity, ResourceGroupField field, Object o) {
        switch (field) {
            case uploadedByUserId -> entity.setUploadedByUserId((UUID) o);
            case storageId -> entity.setStorageId((UUID) o);
        }
    }
}
