package org.twins.core.service.twinclassfield;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.dao.FeaturerEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.validator.TwinClassFieldValidatorEntity;
import org.twins.core.dao.validator.TwinClassFieldValidatorRepository;
import org.twins.core.domain.search.TwinClassFieldValidatorSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.TwinClassFieldValidatorGroupField;
import org.twins.core.enums.sort.TwinClassFieldValidatorSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.*;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassFieldValidatorSearchService extends EntitySearchService
        <TwinClassFieldValidatorSearch, TwinClassFieldValidatorEntity, TwinClassFieldValidatorSortField, TwinClassFieldValidatorGroupField> {
    private final TwinClassFieldValidatorRepository twinClassFieldValidatorRepository;

    @Override
    public JpaSpecificationExecutor<TwinClassFieldValidatorEntity> jpaSpecificationExecutor() {
        return twinClassFieldValidatorRepository;
    }

    @Override
    public TwinClassFieldValidatorSearch emptySearch() {
        return new TwinClassFieldValidatorSearch();
    }

    @Override
    protected TwinClassFieldValidatorEntity newEntity() {
        return new TwinClassFieldValidatorEntity();
    }

    @Override
    protected Class<TwinClassFieldValidatorEntity> entityClass() {
        return TwinClassFieldValidatorEntity.class;
    }

    @Override
    public Specification<TwinClassFieldValidatorEntity> createFilterSpecification(TwinClassFieldValidatorSearch search, UUID domainId, Locale locale) throws ServiceException {
        // Domain isolation via twin_class_field -> twin_class.domain_id (no domain_id on twin_class_field_validator).
        return Specification.allOf(
                checkFieldUuid(domainId, TwinClassFieldValidatorEntity.Fields.twinClassFieldSpecOnly, TwinClassFieldEntity.Fields.twinClass, TwinClassEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, TwinClassFieldValidatorEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinClassFieldValidatorEntity.Fields.id),
                checkUuidIn(search.getTwinClassFieldIdList(), false, false, TwinClassFieldValidatorEntity.Fields.twinClassFieldId),
                checkUuidIn(search.getTwinClassFieldIdExcludeList(), true, false, TwinClassFieldValidatorEntity.Fields.twinClassFieldId),
                checkIntegerIn(search.getFieldValidatorFeaturerIdList(), false, TwinClassFieldValidatorEntity.Fields.fieldValidatorFeaturerId),
                checkIntegerIn(search.getFieldValidatorFeaturerIdExcludeList(), true, TwinClassFieldValidatorEntity.Fields.fieldValidatorFeaturerId)
        );
    }

    @Override
    public Specification<TwinClassFieldValidatorEntity> createSortSpecification(TwinClassFieldValidatorSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        if (sortField == null)
            sortField = TwinClassFieldValidatorSortField.twinClassFieldId;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case twinClassFieldId -> toSortSpecification(ascending, TwinClassFieldValidatorEntity.Fields.twinClassFieldId);
            case fieldValidatorFeaturerId -> toSortSpecification(ascending, TwinClassFieldValidatorEntity.Fields.fieldValidatorFeaturerId);
            case twinClassFieldKey -> toSortSpecification(ascending, TwinClassFieldValidatorEntity.Fields.twinClassFieldSpecOnly, TwinClassFieldEntity.Fields.key);
            case fieldValidatorFeaturerName -> toSortSpecification(ascending, TwinClassFieldValidatorEntity.Fields.fieldValidatorFeaturerSpecOnly, FeaturerEntity.Fields.name);
        };
    }

    @Override
    public String convertToEntityField(TwinClassFieldValidatorGroupField groupField) throws ServiceException {
        return switch (groupField) {
            case twinClassFieldId -> TwinClassFieldValidatorEntity.Fields.twinClassFieldId;
            case fieldValidatorFeaturerId -> TwinClassFieldValidatorEntity.Fields.fieldValidatorFeaturerId;
        };
    }

    @Override
    public void mapGroupedField(TwinClassFieldValidatorEntity entity, TwinClassFieldValidatorGroupField field, Object o) {
        switch (field) {
            case twinClassFieldId -> entity.setTwinClassFieldId((UUID) o);
            case fieldValidatorFeaturerId -> entity.setFieldValidatorFeaturerId((Integer) o);
        }
    }
}
