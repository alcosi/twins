package org.twins.core.service.datalist;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListSubsetEntity;
import org.twins.core.dao.datalist.DataListSubsetRepository;
import org.twins.core.domain.search.DataListSubsetSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.DataListSubsetGroupField;
import org.twins.core.enums.sort.DataListSubsetSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NFieldDirect;
import static org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecificationDirect;
import static org.twins.core.dao.specifications.CommonSpecification.*;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class DataListSubsetSearchService extends EntitySearchService
        <DataListSubsetSearch, DataListSubsetEntity, DataListSubsetSortField, DataListSubsetGroupField> {
    private final DataListSubsetRepository dataListSubsetRepository;

    @Override
    public JpaSpecificationExecutor<DataListSubsetEntity> jpaSpecificationExecutor() {
        return dataListSubsetRepository;
    }

    @Override
    public DataListSubsetSearch emptySearch() {
        return new DataListSubsetSearch();
    }

    @Override
    protected DataListSubsetEntity newEntity() {
        return new DataListSubsetEntity();
    }

    @Override
    protected Class<DataListSubsetEntity> entityClass() {
        return DataListSubsetEntity.class;
    }

    @Override
    public Specification<DataListSubsetEntity> createFilterSpecification(DataListSubsetSearch search, UUID domainId, Locale locale) throws ServiceException {
        return Specification.allOf(
                //domain isolation goes through the parent data list (same as TwinClassFieldSearchService does via twinClass)
                checkUuid(domainId, false, false, DataListSubsetEntity.Fields.dataListSpecOnly, DataListEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, DataListSubsetEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, DataListSubsetEntity.Fields.id),
                checkUuidIn(search.getDataListIdList(), false, false, DataListSubsetEntity.Fields.dataListId),
                checkUuidIn(search.getDataListIdExcludeList(), true, false, DataListSubsetEntity.Fields.dataListId),
                joinAndSearchByI18NFieldDirect(DataListSubsetEntity.Fields.nameI18nTranslationsSpecOnly, search.getNameLikeList(), locale, false, false),
                joinAndSearchByI18NFieldDirect(DataListSubsetEntity.Fields.nameI18nTranslationsSpecOnly, search.getNameNotLikeList(), locale, true, true),
                checkFieldLikeIn(search.getKeyLikeList(), false, true, DataListSubsetEntity.Fields.key),
                checkFieldLikeIn(search.getKeyNotLikeList(), true, true, DataListSubsetEntity.Fields.key));
    }

    @Override
    protected DataListSubsetSortField defaultSortField() {
        return DataListSubsetSortField.key;
    }

    @Override
    public Specification<DataListSubsetEntity> createSortSpecification(DataListSubsetSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case name -> toSortSpecificationDirect(ascending, locale, DataListSubsetEntity.Fields.nameI18nTranslationsSpecOnly);
            case key -> toSortSpecification(ascending, DataListSubsetEntity.Fields.key);
            case dataListName -> toSortSpecificationDirect(ascending, locale, DataListSubsetEntity.Fields.dataListSpecOnly, DataListEntity.Fields.nameI18nTranslationsSpecOnly);
        };
    }

    @Override
    public String convertToEntityField(DataListSubsetGroupField groupField) throws ServiceException {
        return switch (groupField) {
            case dataListId -> DataListSubsetEntity.Fields.dataListId;
        };
    }

    @Override
    public void mapGroupedField(DataListSubsetEntity entity, DataListSubsetGroupField field, Object o) {
        switch (field) {
            case dataListId -> entity.setDataListId((UUID) o);
        }
    }
}
