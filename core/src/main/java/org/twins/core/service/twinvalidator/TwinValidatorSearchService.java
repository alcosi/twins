package org.twins.core.service.twinvalidator;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.dao.FeaturerEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.validator.TwinValidatorRepository;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.domain.search.TwinValidatorSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.TwinValidatorGroupField;
import org.twins.core.enums.sort.TwinValidatorSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.*;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinValidatorSearchService extends EntitySearchService
        <TwinValidatorSearch, TwinValidatorEntity, TwinValidatorSortField, TwinValidatorGroupField> {
    private final TwinValidatorRepository twinValidatorRepository;

    @Override
    public JpaSpecificationExecutor<TwinValidatorEntity> jpaSpecificationExecutor() {
        return twinValidatorRepository;
    }

    @Override
    public TwinValidatorSearch emptySearch() {
        return new TwinValidatorSearch();
    }

    @Override
    protected TwinValidatorEntity newEntity() {
        return new TwinValidatorEntity();
    }

    @Override
    protected Class<TwinValidatorEntity> entityClass() {
        return TwinValidatorEntity.class;
    }

    @Override
    public Specification<TwinValidatorEntity> createFilterSpecification(TwinValidatorSearch search, UUID domainId, Locale locale) throws ServiceException {
        // Domain isolation: twin_validator has no domain_id column, so join the parent twin_validator_set
        // (read-only twinValidatorSetSpecOnly association) and constrain by its domainId. INNER join is
        // safe — twin_validator_set_id is NOT NULL with an FK.
        return Specification.allOf(
                checkFieldUuid(domainId, TwinValidatorEntity.Fields.twinValidatorSetSpecOnly, TwinValidatorSetEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, TwinValidatorEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinValidatorEntity.Fields.id),
                checkUuidIn(search.getTwinValidatorSetIdList(), false, false, TwinValidatorEntity.Fields.twinValidatorSetId),
                checkUuidIn(search.getTwinValidatorSetIdExcludeList(), true, false, TwinValidatorEntity.Fields.twinValidatorSetId),
                checkIntegerIn(search.getValidatorFeaturerIdList(), false, TwinValidatorEntity.Fields.twinValidatorFeaturerId),
                checkIntegerIn(search.getValidatorFeaturerIdExcludeList(), true, TwinValidatorEntity.Fields.twinValidatorFeaturerId),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TwinValidatorEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinValidatorEntity.Fields.description),
                checkTernary(search.getInvert(), TwinValidatorEntity.Fields.invert),
                checkTernary(search.getActive(), TwinValidatorEntity.Fields.active)
        );
    }

    @Override
    public Specification<TwinValidatorEntity> createSortSpecification(TwinValidatorSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        if (sortField == null)
            sortField = TwinValidatorSortField.order;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case order -> toSortSpecification(ascending, TwinValidatorEntity.Fields.order);
            case description -> toSortSpecification(ascending, TwinValidatorEntity.Fields.description);
            case invert -> toSortSpecification(ascending, TwinValidatorEntity.Fields.invert);
            case active -> toSortSpecification(ascending, TwinValidatorEntity.Fields.active);
            case twinValidatorSetName -> toSortSpecification(ascending, TwinValidatorEntity.Fields.twinValidatorSetSpecOnly, TwinValidatorSetEntity.Fields.name);
            case twinValidatorFeaturerName -> toSortSpecification(ascending, TwinValidatorEntity.Fields.twinValidatorFeaturerSpecOnly, FeaturerEntity.Fields.name);
        };
    }

    @Override
    public String convertToEntityField(TwinValidatorGroupField groupField) throws ServiceException {
        return switch (groupField) {
            case invert -> TwinValidatorEntity.Fields.invert;
            case active -> TwinValidatorEntity.Fields.active;
            case twinValidatorSetId -> TwinValidatorEntity.Fields.twinValidatorSetId;
            case validatorFeaturerId -> TwinValidatorEntity.Fields.twinValidatorFeaturerId;
        };
    }

    @Override
    public void mapGroupedField(TwinValidatorEntity entity, TwinValidatorGroupField field, Object o) {
        switch (field) {
            case invert -> entity.setInvert((Boolean) o);
            case active -> entity.setActive((Boolean) o);
            case twinValidatorSetId -> entity.setTwinValidatorSetId((UUID) o);
            case validatorFeaturerId -> entity.setTwinValidatorFeaturerId((Integer) o);
        }
    }
}
