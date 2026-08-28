package org.twins.core.service.datalist;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionProjectionEntity;
import org.twins.core.dao.datalist.DataListOptionProjectionRepository;
import org.twins.core.dao.projection.ProjectionTypeEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.search.DataListOptionProjectionSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.DataListOptionProjectionGroupField;
import org.twins.core.enums.sort.DataListOptionProjectionSortField;
import org.twins.core.service.EntitySearchService;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.*;

// Log calls that took more than 2 seconds
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class DataListOptionProjectionSearchService extends EntitySearchService
        <DataListOptionProjectionSearch, DataListOptionProjectionEntity, DataListOptionProjectionSortField, DataListOptionProjectionGroupField> {
    private final DataListOptionProjectionRepository dataListOptionProjectionRepository;

    @Override
    public JpaSpecificationExecutor<DataListOptionProjectionEntity> jpaSpecificationExecutor() {
        return dataListOptionProjectionRepository;
    }

    @Override
    public DataListOptionProjectionSearch emptySearch() {
        return new DataListOptionProjectionSearch();
    }

    @Override
    protected DataListOptionProjectionEntity newEntity() {
        return new DataListOptionProjectionEntity();
    }

    @Override
    protected Class<DataListOptionProjectionEntity> entityClass() {
        return DataListOptionProjectionEntity.class;
    }

    public List<DataListOptionProjectionEntity> findDataListOptionProjections(DataListOptionProjectionSearch search) throws ServiceException {
        return dataListOptionProjectionRepository.findAll(createFilterSpecification(search));
    }

    @Override
    public Specification<DataListOptionProjectionEntity> createFilterSpecification(DataListOptionProjectionSearch search, UUID domainId, Locale locale) throws ServiceException {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, DataListOptionProjectionEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, DataListOptionProjectionEntity.Fields.id),
                checkUuidIn(search.getProjectionTypeIdList(), false, false, DataListOptionProjectionEntity.Fields.projectionTypeId),
                checkUuidIn(search.getProjectionTypeIdExcludeList(), true, false, DataListOptionProjectionEntity.Fields.projectionTypeId),
                checkUuidIn(search.getSrcDataListOptionIdList(), false, false, DataListOptionProjectionEntity.Fields.srcDataListOptionId),
                checkUuidIn(search.getSrcDataListOptionIdExcludeList(), true, false, DataListOptionProjectionEntity.Fields.srcDataListOptionId),
                checkUuidIn(search.getDstDataListOptionIdList(), false, false, DataListOptionProjectionEntity.Fields.dstDataListOptionId),
                checkUuidIn(search.getDstDataListOptionIdExcludeList(), true, false, DataListOptionProjectionEntity.Fields.dstDataListOptionId),
                checkUuidIn(search.getSavedByUserIdList(), false, false, DataListOptionProjectionEntity.Fields.savedByUserId),
                checkUuidIn(search.getSavedByUserIdExcludeList(), true, false, DataListOptionProjectionEntity.Fields.savedByUserId),
                checkFieldLocalDateTimeBetween(search.getChangedAt(), DataListOptionProjectionEntity.Fields.changedAt)
        );
    }

    @Override
    public Specification<DataListOptionProjectionEntity> createSortSpecification(DataListOptionProjectionSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        if (sortField == null)
            sortField = DataListOptionProjectionSortField.changedAt;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case changedAt ->
                    toSortSpecification(ascending, DataListOptionProjectionEntity.Fields.changedAt);
            case savedByUserName ->
                    toSortSpecification(ascending, DataListOptionProjectionEntity.Fields.savedByUserSpecOnly, UserEntity.Fields.name);
            case projectionTypeName ->
                    toSortSpecification(ascending, DataListOptionProjectionEntity.Fields.projectionTypeSpecOnly, ProjectionTypeEntity.Fields.name);
            case srcDataListOptionName ->
                    toSortSpecification(ascending, DataListOptionProjectionEntity.Fields.srcDataListOptionSpecOnly, DataListOptionEntity.Fields.option);
            case dstDataListOptionName ->
                    toSortSpecification(ascending, DataListOptionProjectionEntity.Fields.dstDataListOptionSpecOnly, DataListOptionEntity.Fields.option);
        };
    }

    @Override
    public String convertToEntityField(DataListOptionProjectionGroupField groupField) throws ServiceException {
        return switch (groupField) {
            case projectionTypeId -> DataListOptionProjectionEntity.Fields.projectionTypeId;
            case srcDataListOptionId -> DataListOptionProjectionEntity.Fields.srcDataListOptionId;
            case dstDataListOptionId -> DataListOptionProjectionEntity.Fields.dstDataListOptionId;
            case savedByUserId -> DataListOptionProjectionEntity.Fields.savedByUserId;
        };
    }

    @Override
    public void mapGroupedField(DataListOptionProjectionEntity entity, DataListOptionProjectionGroupField field, Object o) {
        switch (field) {
            case projectionTypeId -> entity.setProjectionTypeId((UUID) o);
            case srcDataListOptionId -> entity.setSrcDataListOptionId((UUID) o);
            case dstDataListOptionId -> entity.setDstDataListOptionId((UUID) o);
            case savedByUserId -> entity.setSavedByUserId((UUID) o);
        }
    }
}
