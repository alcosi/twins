package org.twins.core.service.twinvalidator;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.dao.validator.TwinValidatorSetRepository;
import org.twins.core.domain.search.TwinValidatorSetSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.TwinValidatorSetGroupField;
import org.twins.core.enums.sort.TwinValidatorSetSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.*;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinValidatorSetSearchService extends EntitySearchService
        <TwinValidatorSetSearch, TwinValidatorSetEntity, TwinValidatorSetSortField, TwinValidatorSetGroupField> {
    private final TwinValidatorSetRepository twinValidatorSetRepository;

    @Override
    public JpaSpecificationExecutor<TwinValidatorSetEntity> jpaSpecificationExecutor() {
        return twinValidatorSetRepository;
    }

    @Override
    public TwinValidatorSetSearch emptySearch() {
        return new TwinValidatorSetSearch();
    }

    @Override
    protected TwinValidatorSetEntity newEntity() {
        return new TwinValidatorSetEntity();
    }

    @Override
    protected Class<TwinValidatorSetEntity> entityClass() {
        return TwinValidatorSetEntity.class;
    }

    @Override
    public Specification<TwinValidatorSetEntity> createFilterSpecification(TwinValidatorSetSearch search, UUID domainId, Locale locale) throws ServiceException {
        return Specification.allOf(
                checkFieldUuid(domainId, TwinValidatorSetEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, TwinValidatorSetEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinValidatorSetEntity.Fields.id),
                checkFieldLikeIn(search.getNameLikeList(), false, true, TwinValidatorSetEntity.Fields.name),
                checkFieldLikeIn(search.getNameNotLikeList(), true, true, TwinValidatorSetEntity.Fields.name),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TwinValidatorSetEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinValidatorSetEntity.Fields.description),
                checkTernary(search.getInvert(), TwinValidatorSetEntity.Fields.invert),
                checkFieldIntegerRange(search.getUsageCountRange(), TwinValidatorSetEntity.Fields.usageCount)
        );
    }

    @Override
    public Specification<TwinValidatorSetEntity> createSortSpecification(TwinValidatorSetSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        if (sortField == null)
            sortField = TwinValidatorSetSortField.name;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case name -> toSortSpecification(ascending, TwinValidatorSetEntity.Fields.name);
            case description -> toSortSpecification(ascending, TwinValidatorSetEntity.Fields.description);
            case invert -> toSortSpecification(ascending, TwinValidatorSetEntity.Fields.invert);
        };
    }

    @Override
    public String convertToEntityField(TwinValidatorSetGroupField groupField) throws ServiceException {
        return switch (groupField) {
            case invert -> TwinValidatorSetEntity.Fields.invert;
        };
    }

    @Override
    public void mapGroupedField(TwinValidatorSetEntity entity, TwinValidatorSetGroupField field, Object o) {
        switch (field) {
            case invert -> entity.setInvert((Boolean) o);
        }
    }
}
