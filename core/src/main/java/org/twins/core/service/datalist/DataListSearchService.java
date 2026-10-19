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
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListRepository;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.domain.search.DataListSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.datalist.DataListStatus;
import org.twins.core.enums.sort.DataListGroupField;
import org.twins.core.enums.sort.DataListSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.*;
import static org.twins.core.dao.specifications.CommonSpecification.*;
import static org.twins.core.dao.specifications.datalist.DataListSpecification.checkDataListOptionFieldLikeIn;
import static org.twins.core.dao.specifications.datalist.DataListSpecification.checkDataListOptionUuidIn;

//Log calls that took more than 2 seconds
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class DataListSearchService extends EntitySearchService
        <DataListSearch, DataListEntity, DataListSortField, DataListGroupField> {
    private final DataListRepository dataListRepository;

    @Override
    public JpaSpecificationExecutor<DataListEntity> jpaSpecificationExecutor() {
        return dataListRepository;
    }

    @Override
    public DataListSearch emptySearch() {
        return new DataListSearch();
    }

    @Override
    protected DataListEntity newEntity() {
        return new DataListEntity();
    }

    @Override
    protected Class<DataListEntity> entityClass() {
        return DataListEntity.class;
    }

    @Override
    public Specification<DataListEntity> createFilterSpecification(DataListSearch search, UUID domainId, Locale locale) throws ServiceException {
        DataListOptionSearch optionSearch = search.getOptionSearch();
        return Specification.allOf(
                checkFieldUuid(domainId, DataListEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, DataListEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, DataListEntity.Fields.id),
                joinAndSearchByI18NFieldDirect(DataListEntity.Fields.nameI18nTranslationsSpecOnly, search.getNameLikeList(), locale, false, false),
                joinAndSearchByI18NFieldDirect(DataListEntity.Fields.nameI18nTranslationsSpecOnly, search.getNameNotLikeList(), locale, true, true),
                joinAndSearchByI18NFieldDirect(DataListEntity.Fields.descriptionI18nTranslationsSpecOnly, search.getDescriptionLikeList(), locale, false, false),
                joinAndSearchByI18NFieldDirect(DataListEntity.Fields.descriptionI18nTranslationsSpecOnly, search.getDescriptionNotLikeList(), locale, true, true),
                checkFieldLikeIn(search.getKeyLikeList(), false, true, DataListEntity.Fields.key),
                checkFieldLikeIn(search.getKeyNotLikeList(), true, true, DataListEntity.Fields.key),
                checkDataListOptionUuidIn(DataListOptionEntity.Fields.id, optionSearch != null ? optionSearch.getIdList() : null, false, false),
                checkDataListOptionUuidIn(DataListOptionEntity.Fields.id, optionSearch != null ? optionSearch.getIdExcludeList() : null, true, false),
                checkDataListOptionUuidIn(DataListOptionEntity.Fields.dataListId, optionSearch != null ? optionSearch.getDataListIdList() : null, false, false),
                checkDataListOptionUuidIn(DataListOptionEntity.Fields.dataListId, optionSearch != null ? optionSearch.getDataListIdExcludeList() : null, true, false),
                checkDataListOptionFieldLikeIn(DataListOptionEntity.Fields.option, optionSearch != null ? optionSearch.getOptionLikeList() : null, false, true),
                checkDataListOptionFieldLikeIn(DataListOptionEntity.Fields.option, optionSearch != null ? optionSearch.getOptionNotLikeList() : null, true, true),
                checkDataListOptionFieldLikeIn(DataListOptionEntity.Fields.status, optionSearch != null ? safeConvert(optionSearch.getStatusIdList()) : null, false, true),
                checkDataListOptionFieldLikeIn(DataListOptionEntity.Fields.status, optionSearch != null ? safeConvert(optionSearch.getStatusIdExcludeList()) : null, true, true),
                doubleJoinAndSearchByI18NFieldDirect(DataListEntity.Fields.dataListOptionsSpecOnly, DataListOptionEntity.Fields.optionI18nTranslationsSpecOnly, optionSearch != null ? optionSearch.getOptionI18nLikeList() : null, locale, false, false),
                doubleJoinAndSearchByI18NFieldDirect(DataListEntity.Fields.dataListOptionsSpecOnly, DataListOptionEntity.Fields.optionI18nTranslationsSpecOnly, optionSearch != null ? optionSearch.getOptionI18nNotLikeList() : null, locale, true, true),
                checkDataListOptionUuidIn(DataListOptionEntity.Fields.businessAccountId, optionSearch != null ? optionSearch.getBusinessAccountIdList() : null, false, false),
                checkDataListOptionUuidIn(DataListOptionEntity.Fields.businessAccountId, optionSearch != null ? optionSearch.getBusinessAccountIdExcludeList() : null, true, true),
                checkFieldLikeIn(search.getExternalIdLikeList(), false, false, DataListEntity.Fields.externalId),
                checkFieldLikeIn(search.getExternalIdNotLikeList(), true, false, DataListEntity.Fields.externalId),
                checkUuidIn(search.getDefaultOptionIdList(), false, false, DataListEntity.Fields.defaultDataListOptionId),
                checkUuidIn(search.getDefaultOptionIdExcludeList(), true, false, DataListEntity.Fields.defaultDataListOptionId));
    }

    private Set<String> safeConvert(Set<DataListStatus> collection) {
        return collection == null ? Collections.emptySet() : collection.stream().map(Enum::name).collect(Collectors.toSet());
    }

    @Override
    protected DataListSortField defaultSortField() {
        return DataListSortField.key;
    }

    @Override
    public Specification<DataListEntity> createSortSpecification(DataListSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case key -> toSortSpecification(ascending, DataListEntity.Fields.key);
            case name -> toSortSpecificationDirect(ascending, locale, DataListEntity.Fields.nameI18nTranslationsSpecOnly);
            case description -> toSortSpecificationDirect(ascending, locale, DataListEntity.Fields.descriptionI18nTranslationsSpecOnly);
            case createdAt -> toSortSpecification(ascending, DataListEntity.Fields.createdAt);
            case updatedAt -> toSortSpecification(ascending, DataListEntity.Fields.updatedAt);
            case externalId -> toSortSpecification(ascending, DataListEntity.Fields.externalId);
            case createdByUserName -> toSortSpecification(ascending, DataListEntity.Fields.createdByUserSpecOnly, UserEntity.Fields.name);
        };
    }

    @Override
    public String convertToEntityField(DataListGroupField groupField) throws ServiceException {
        return switch (groupField) {
            case createdByUserId -> DataListEntity.Fields.createdByUserId;
        };
    }

    @Override
    public void mapGroupedField(DataListEntity entity, DataListGroupField field, Object o) {
        switch (field) {
            case createdByUserId -> entity.setCreatedByUserId((UUID) o);
        }
    }
}
