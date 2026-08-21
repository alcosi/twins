package org.twins.core.service.system;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.dao.FeaturerTypeEntity;
import org.cambium.featurer.dao.FeaturerTypeRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.domain.search.FeaturerTypeSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.FeaturerTypeSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.*;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class FeaturerTypeSearchService extends EntitySearchService
        <FeaturerTypeSearch, FeaturerTypeEntity, FeaturerTypeSortField, Void> {
    private final FeaturerTypeRepository featurerTypeRepository;

    @Override
    public JpaSpecificationExecutor<FeaturerTypeEntity> jpaSpecificationExecutor() {
        return featurerTypeRepository;
    }

    @Override
    public FeaturerTypeSearch emptySearch() {
        return new FeaturerTypeSearch();
    }

    @Override
    protected FeaturerTypeEntity newEntity() {
        return new FeaturerTypeEntity();
    }

    @Override
    protected Class<FeaturerTypeEntity> entityClass() {
        return FeaturerTypeEntity.class;
    }

    @Override
    public Specification<FeaturerTypeEntity> createFilterSpecification(FeaturerTypeSearch search, UUID domainId, Locale locale) throws ServiceException {
        return Specification.allOf(
                checkIntegerIn(search.getIdList(), false, FeaturerTypeEntity.Fields.id),
                checkIntegerIn(search.getIdExcludeList(), true, FeaturerTypeEntity.Fields.id),
                checkFieldLikeIn(search.getNameLikeList(), false, true, FeaturerTypeEntity.Fields.name),
                checkFieldLikeIn(search.getNameNotLikeList(), true, true, FeaturerTypeEntity.Fields.name),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, FeaturerTypeEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, FeaturerTypeEntity.Fields.description));
    }

    @Override
    public Specification<FeaturerTypeEntity> createSortSpecification(FeaturerTypeSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        if (sortField == null)
            sortField = FeaturerTypeSortField.name;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case name -> toSortSpecification(ascending, FeaturerTypeEntity.Fields.name);
            case description -> toSortSpecification(ascending, FeaturerTypeEntity.Fields.description);
        };
    }

    @Override
    public String convertToEntityField(Void groupField) throws ServiceException {
        // grouping is not supported for featurer type
        return null;
    }

    @Override
    public void mapGroupedField(FeaturerTypeEntity entity, Void field, Object o) {
        // grouping is not supported for featurer type
    }
}
