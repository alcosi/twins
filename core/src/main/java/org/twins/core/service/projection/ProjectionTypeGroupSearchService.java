package org.twins.core.service.projection;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.projection.ProjectionTypeGroupEntity;
import org.twins.core.dao.projection.ProjectionTypeGroupRepository;
import org.twins.core.domain.search.ProjectionTypeGroupSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.ProjectionTypeGroupSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.*;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectionTypeGroupSearchService extends EntitySearchService
        <ProjectionTypeGroupSearch, ProjectionTypeGroupEntity, ProjectionTypeGroupSortField, Void> {
    private final ProjectionTypeGroupRepository projectionTypeGroupRepository;

    @Override
    public JpaSpecificationExecutor<ProjectionTypeGroupEntity> jpaSpecificationExecutor() {
        return projectionTypeGroupRepository;
    }

    @Override
    public ProjectionTypeGroupSearch emptySearch() {
        return new ProjectionTypeGroupSearch();
    }

    @Override
    protected ProjectionTypeGroupEntity newEntity() {
        return new ProjectionTypeGroupEntity();
    }

    @Override
    protected Class<ProjectionTypeGroupEntity> entityClass() {
        return ProjectionTypeGroupEntity.class;
    }

    @Override
    public Specification<ProjectionTypeGroupEntity> createFilterSpecification(ProjectionTypeGroupSearch search, UUID domainId, Locale locale) throws ServiceException {
        return Specification.allOf(
                checkFieldUuid(domainId, ProjectionTypeGroupEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, ProjectionTypeGroupEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, ProjectionTypeGroupEntity.Fields.id),
                checkFieldLikeIn(search.getKeyLikeList(), false, true, ProjectionTypeGroupEntity.Fields.key),
                checkFieldLikeIn(search.getKeyNotLikeList(), true, true, ProjectionTypeGroupEntity.Fields.key));
    }

    @Override
    public Specification<ProjectionTypeGroupEntity> createSortSpecification(ProjectionTypeGroupSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        if (sortField == null)
            sortField = ProjectionTypeGroupSortField.key;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case key -> toSortSpecification(ascending, ProjectionTypeGroupEntity.Fields.key);
        };
    }

    @Override
    public String convertToEntityField(Void groupField) throws ServiceException {
        // grouping is not supported for projection type group
        return null;
    }

    @Override
    public void mapGroupedField(ProjectionTypeGroupEntity entity, Void field, Object o) {
        // grouping is not supported for projection type group
    }
}
