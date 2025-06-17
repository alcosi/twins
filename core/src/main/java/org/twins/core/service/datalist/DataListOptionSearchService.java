package org.twins.core.service.datalist;

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
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.service.auth.AuthService;

import java.util.List;

import static org.cambium.common.util.EnumUtils.convertOrEmpty;
import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.datalist.DataListOptionSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class DataListOptionSearchService {
    private final AuthService authService;
    private final DataListOptionRepository dataListOptionRepository;

    public List<DataListOptionEntity> findDataListOptions(DataListOptionSearch search) throws ServiceException {
        Specification<DataListOptionEntity> spec = createDataListOptionSearchSpecification(search);
        return dataListOptionRepository.findAll(spec);
    }

    public PaginationResult<DataListOptionEntity> findDataListOptionForDomain(DataListOptionSearch search, SimplePagination pagination) throws ServiceException {
        Specification<DataListOptionEntity> spec = createDataListOptionSearchSpecification(search);
        Page<DataListOptionEntity> ret = dataListOptionRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<DataListOptionEntity> createDataListOptionSearchSpecification(DataListOptionSearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return Specification.allOf(
                checkFieldUuid(apiUser.getDomainId(),DataListOptionEntity.Fields.dataList,DataListEntity.Fields.domainId),
                createBusinessAccountSpecification(apiUser, search),
                checkUuidIn(search.getIdList(), false, false, DataListOptionEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, DataListOptionEntity.Fields.id),
                checkUuidIn(search.getDataListIdList(), false, false, DataListOptionEntity.Fields.dataListId),
                checkUuidIn(search.getDataListIdExcludeList(), true, true, DataListOptionEntity.Fields.dataListId),
                checkFieldLikeIn(search.getOptionLikeList(), false, true, DataListOptionEntity.Fields.option),
                checkFieldLikeIn(search.getOptionNotLikeList(), true, true, DataListOptionEntity.Fields.option),
                checkDataListKeyLikeIn(search.getDataListKeyList(), false, true),
                checkDataListKeyLikeIn(search.getDataListKeyExcludeList(), true, true),
                checkStatusLikeIn(convertOrEmpty(search.getStatusIdList()), false, true),
                checkStatusLikeIn(convertOrEmpty(search.getStatusIdExcludeList()), true, true),
                joinAndSearchByI18NField(DataListOptionEntity.Fields.optionI18n, search.getOptionI18nLikeList(), apiUser.getLocale(), true, false),
                joinAndSearchByI18NField(DataListOptionEntity.Fields.optionI18n, search.getOptionI18nNotLikeList(), apiUser.getLocale(), true, true),
                checkDataListSubset(search.getDataListSubsetIdList(), false),
                checkDataListSubset(search.getDataListSubsetIdExcludeList(), true),
                checkDataListSubsetKey(search.getDataListSubsetKeyList(), false, true),
                checkDataListSubsetKey(search.getDataListSubsetKeyExcludeList(), true, true),
                checkFieldLikeIn(search.getExternalIdLikeList(), false, false, DataListOptionEntity.Fields.externalId),
                checkFieldLikeIn(search.getExternalIdNotLikeList(), true, false, DataListOptionEntity.Fields.externalId));
    }

    private Specification<DataListOptionEntity> createBusinessAccountSpecification(ApiUser apiUser, DataListOptionSearch search) {
        if (apiUser.isBusinessAccountSpecified()) {
            return Specification.allOf(
                    checkUuidIn(search.getBusinessAccountIdList(), false, true, DataListOptionEntity.Fields.businessAccountId),
                    checkUuidIn(search.getBusinessAccountIdExcludeList(), true, true, DataListOptionEntity.Fields.businessAccountId)
            );
        } else {
            return Specification.where((root, query, cb) -> root.get(DataListOptionEntity.Fields.businessAccountId).isNull());
        }
    }
}
