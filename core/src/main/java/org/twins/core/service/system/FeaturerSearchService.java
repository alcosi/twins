package org.twins.core.service.system;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.dao.FeaturerEntity;
import org.cambium.featurer.dao.FeaturerRepository;
import org.cambium.featurer.dao.FeaturerTypeEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.domain.search.FeaturerSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.FeaturerGroupField;
import org.twins.core.enums.sort.FeaturerSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.cambium.featurer.dao.specifications.FeaturerSpecification.checkIntegerIn;
import static org.twins.core.dao.specifications.CommonSpecification.*;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class FeaturerSearchService extends EntitySearchService
        <FeaturerSearch, FeaturerEntity, FeaturerSortField, FeaturerGroupField> {
    private final FeaturerRepository featurerRepository;

    @Override
    public JpaSpecificationExecutor<FeaturerEntity> jpaSpecificationExecutor() {
        return featurerRepository;
    }

    @Override
    public FeaturerSearch emptySearch() {
        return new FeaturerSearch();
    }

    @Override
    protected FeaturerEntity newEntity() {
        return new FeaturerEntity();
    }

    @Override
    protected Class<FeaturerEntity> entityClass() {
        return FeaturerEntity.class;
    }

    @Override
    public Specification<FeaturerEntity> createFilterSpecification(FeaturerSearch search, UUID domainId, Locale locale) throws ServiceException {
        return Specification.allOf(
                checkIntegerIn(FeaturerEntity.Fields.id, search.getIdList(), false),
                checkIntegerIn(FeaturerEntity.Fields.id, search.getIdExcludeList(), true),
                checkIntegerIn(FeaturerEntity.Fields.featurerTypeId, search.getTypeIdList(), false),
                checkIntegerIn(FeaturerEntity.Fields.featurerTypeId, search.getTypeIdExcludeList(), true),
                checkFieldLikeIn(search.getNameLikeList(), false, true, FeaturerEntity.Fields.name),
                checkNameOrIdLikeIn(search.getNameOrIdLikeList(), FeaturerEntity.Fields.name, FeaturerEntity.Fields.id),
                checkFieldLikeIn(search.getNameNotLikeList(), true, true, FeaturerEntity.Fields.name),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, FeaturerEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, FeaturerEntity.Fields.description),
                checkTernary(search.getDeprecated(), FeaturerEntity.Fields.deprecated));
    }

    @Override
    protected FeaturerSortField defaultSortField() {
        return FeaturerSortField.name;
    }

    @Override
    public Specification<FeaturerEntity> createSortSpecification(FeaturerSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case name -> toSortSpecification(ascending, FeaturerEntity.Fields.name);
            case description -> toSortSpecification(ascending, FeaturerEntity.Fields.description);
            case deprecated -> toSortSpecification(ascending, FeaturerEntity.Fields.deprecated);
            case featurerTypeName -> toSortSpecification(ascending, FeaturerEntity.Fields.featurerTypeSpecOnly, FeaturerTypeEntity.Fields.name);
        };
    }

    @Override
    public String convertToEntityField(FeaturerGroupField groupField) throws ServiceException {
        return switch (groupField) {
            case featurerTypeId -> FeaturerEntity.Fields.featurerTypeId;
            case deprecated -> FeaturerEntity.Fields.deprecated;
        };
    }

    @Override
    public void mapGroupedField(FeaturerEntity entity, FeaturerGroupField field, Object o) {
        switch (field) {
            case featurerTypeId -> entity.setFeaturerTypeId((Integer) o);
            case deprecated -> entity.setDeprecated((Boolean) o);
        }
    }
}
