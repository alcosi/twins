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
import org.twins.core.dao.datalist.DataListRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.DataListSearch;
import org.twins.core.service.auth.AuthService;

import static org.cambium.i18n.dao.specifications.I18nSpecification.doubleJoinAndSearchByI18NField;
import static org.cambium.i18n.dao.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.datalist.DataListSpecification.*;


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
        return Specification.where(
                checkDomainId(apiUser.getDomainId())
                        .and(checkUuidIn(DataListEntity.Fields.id, search.getIdList(), false, false))
                        .and(checkUuidIn(DataListEntity.Fields.id, search.getIdExcludeList(), true, false))
                        .and(joinAndSearchByI18NField(DataListEntity.Fields.nameI18n, search.getNameLikeList(), apiUser.getLocale(),false, true))
                        .and(joinAndSearchByI18NField(DataListEntity.Fields.nameI18n, search.getNameNotLikeList(), apiUser.getLocale(),true, true))
                        .and(joinAndSearchByI18NField(DataListEntity.Fields.descriptionI18n, search.getDescriptionLikeList(), apiUser.getLocale(),false, true))
                        .and(joinAndSearchByI18NField(DataListEntity.Fields.descriptionI18n, search.getDescriptionNotLikeList(), apiUser.getLocale(),true, true))
                        .and(checkFieldLikeIn(DataListEntity.Fields.key, search.getKeyLikeList(), false, true))
                        .and(checkFieldLikeIn(DataListEntity.Fields.key, search.getKeyNotLikeList(), true, true))
                        .and(checkDataListOptionUuidIn(DataListOptionEntity.Fields.id, search.getOptionSearch() != null ? search.getOptionSearch().getIdList() : null,false, false))
                        .and(checkDataListOptionUuidIn(DataListOptionEntity.Fields.id, search.getOptionSearch() != null ? search.getOptionSearch().getIdExcludeList() : null,true, false))
                        .and(checkDataListOptionUuidIn(DataListOptionEntity.Fields.dataListId, search.getOptionSearch() != null ? search.getOptionSearch().getDataListIdList() : null,false, false))
                        .and(checkDataListOptionUuidIn(DataListOptionEntity.Fields.dataListId, search.getOptionSearch() != null ? search.getOptionSearch().getDataListIdExcludeList() : null,true, false))
                        .and(checkDataListOptionFieldLikeIn(DataListOptionEntity.Fields.option, search.getOptionSearch() != null ? search.getOptionSearch().getOptionLikeList() : null, false, true))
                        .and(checkDataListOptionFieldLikeIn(DataListOptionEntity.Fields.option, search.getOptionSearch() != null ? search.getOptionSearch().getOptionNotLikeList() : null, true, true))
                        .and(doubleJoinAndSearchByI18NField(DataListEntity.Fields.dataListOptions, DataListOptionEntity.Fields.optionI18n, search.getOptionSearch() != null ? search.getOptionSearch().getOptionI18nLikeList() : null, apiUser.getLocale(),false, false))
                        .and(doubleJoinAndSearchByI18NField(DataListEntity.Fields.dataListOptions, DataListOptionEntity.Fields.optionI18n, search.getOptionSearch() != null ? search.getOptionSearch().getOptionI18nNotLikeList() : null, apiUser.getLocale(),true, true))
                        .and(checkDataListOptionUuidIn(DataListOptionEntity.Fields.businessAccountId, search.getOptionSearch() != null ? search.getOptionSearch().getBusinessAccountIdList() : null,false, false))
                        .and(checkDataListOptionUuidIn(DataListOptionEntity.Fields.businessAccountId, search.getOptionSearch() != null ? search.getOptionSearch().getBusinessAccountIdExcludeList() : null,true, true))
        );
    }
}
