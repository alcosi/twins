package org.twins.core.service.datalist;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListRepository;
import org.twins.core.enums.datalist.DataListStatus;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.DataListSearch;
import org.twins.core.service.auth.AuthService;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.doubleJoinAndSearchByI18NField;
import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.datalist.DataListSpecification.*;

//Log calls that took more than 2 seconds
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class DataListSearchService {
    private final AuthService authService;
    private final DataListRepository dataListRepository;

    public PaginationResult<DataListEntity> findDataListsForDomain(DataListSearch search, SimplePagination pagination) throws ServiceException {
        Specification<DataListEntity> spec = createDataListSpecification(search);
        Page<DataListEntity> ret = dataListRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<DataListEntity> createDataListSpecification(DataListSearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return Specification.allOf(
                checkFieldUuid(apiUser.getDomainId(), DataListEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, DataListEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, DataListEntity.Fields.id),
                joinAndSearchByI18NField(DataListEntity.Fields.nameI18n, search.getNameLikeList(), apiUser.getLocale(), false, false),
                joinAndSearchByI18NField(DataListEntity.Fields.nameI18n, search.getNameNotLikeList(), apiUser.getLocale(), true, true),
                joinAndSearchByI18NField(DataListEntity.Fields.descriptionI18n, search.getDescriptionLikeList(), apiUser.getLocale(), false, false),
                joinAndSearchByI18NField(DataListEntity.Fields.descriptionI18n, search.getDescriptionNotLikeList(), apiUser.getLocale(), true, true),
                checkFieldLikeIn(search.getKeyLikeList(), false, true, DataListEntity.Fields.key),
                checkFieldLikeIn(search.getKeyNotLikeList(), true, true, DataListEntity.Fields.key),
                checkDataListOptionUuidIn(DataListOptionEntity.Fields.id, search.getOptionSearch() != null ? search.getOptionSearch().getIdList() : null, false, false),
                checkDataListOptionUuidIn(DataListOptionEntity.Fields.id, search.getOptionSearch() != null ? search.getOptionSearch().getIdExcludeList() : null, true, false),
                checkDataListOptionUuidIn(DataListOptionEntity.Fields.dataListId, search.getOptionSearch() != null ? search.getOptionSearch().getDataListIdList() : null, false, false),
                checkDataListOptionUuidIn(DataListOptionEntity.Fields.dataListId, search.getOptionSearch() != null ? search.getOptionSearch().getDataListIdExcludeList() : null, true, false),
                checkDataListOptionFieldLikeIn(DataListOptionEntity.Fields.option, search.getOptionSearch() != null ? search.getOptionSearch().getOptionLikeList() : null, false, true),
                checkDataListOptionFieldLikeIn(DataListOptionEntity.Fields.option, search.getOptionSearch() != null ? search.getOptionSearch().getOptionNotLikeList() : null, true, true),
                checkDataListOptionFieldLikeIn(DataListOptionEntity.Fields.status, search.getOptionSearch() != null ? safeConvert(search.getOptionSearch().getStatusIdList()) : null, false, true),
                checkDataListOptionFieldLikeIn(DataListOptionEntity.Fields.status, search.getOptionSearch() != null ? safeConvert(search.getOptionSearch().getStatusIdExcludeList()) : null, true, true),
                doubleJoinAndSearchByI18NField(DataListEntity.Fields.dataListOptions, DataListOptionEntity.Fields.optionI18n, search.getOptionSearch() != null ? search.getOptionSearch().getOptionI18nLikeList() : null, apiUser.getLocale(), false, false),
                doubleJoinAndSearchByI18NField(DataListEntity.Fields.dataListOptions, DataListOptionEntity.Fields.optionI18n, search.getOptionSearch() != null ? search.getOptionSearch().getOptionI18nNotLikeList() : null, apiUser.getLocale(), true, true),
                checkDataListOptionUuidIn(DataListOptionEntity.Fields.businessAccountId, search.getOptionSearch() != null ? search.getOptionSearch().getBusinessAccountIdList() : null, false, false),
                checkDataListOptionUuidIn(DataListOptionEntity.Fields.businessAccountId, search.getOptionSearch() != null ? search.getOptionSearch().getBusinessAccountIdExcludeList() : null, true, true),
                checkFieldLikeIn(search.getExternalIdLikeList(), false, false, DataListEntity.Fields.externalId),
                checkFieldLikeIn(search.getExternalIdNotLikeList(), true, false, DataListEntity.Fields.externalId),
                checkUuidIn(search.getDefaultOptionIdList(), false, false, DataListEntity.Fields.defaultDataListOptionId),
                checkUuidIn(search.getDefaultOptionIdExcludeList(), true, false, DataListEntity.Fields.defaultDataListOptionId));
    }

    private Set<String> safeConvert(Set<DataListStatus> collection) {
        return collection == null ? Collections.emptySet() : collection.stream().map(Enum::name).collect(Collectors.toSet());
    }
}
