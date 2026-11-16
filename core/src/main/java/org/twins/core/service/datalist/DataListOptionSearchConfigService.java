package org.twins.core.service.datalist;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.cambium.common.util.Ternary;
import org.cambium.common.util.TernaryUtils;
import org.cambium.service.EntitySmartService;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.featurer.datalist.finder.DataListOptionFinder;
import org.twins.core.featurer.datalist.sorter.DataListOptionSorter;
import org.twins.core.featurer.search.SearchFinder;
import org.twins.core.service.ConfiguredSearchService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.cambium.common.util.SetUtils.narrowSet;

@RequiredArgsConstructor
@Service
public class DataListOptionSearchConfigService extends ConfiguredSearchService<DataListOptionSearchEntity, DataListOptionSearchPredicateEntity, DataListOptionSearch, DataListOptionEntity> {
    private final DataListOptionSearchRepository dataListOptionSearchRepository;
    private final DataListOptionSearchPredicateRepository dataListOptionSearchPredicateRepository;
    private final DataListOptionRepository dataListOptionRepository;
    private final DataListOptionSearchService dataListOptionSearchService;

    @Override
    public CrudRepository<DataListOptionSearchEntity, UUID> entityRepository() {
        return dataListOptionSearchRepository;
    }

    @Override
    public Function<DataListOptionSearchEntity, UUID> entityGetIdFunction() {
        return DataListOptionSearchEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(DataListOptionSearchEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return checkDomainAccessDenied(entity.getDomainId(), entity.logNormal(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(DataListOptionSearchEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    protected UUID unlimitedSearchId() {
        return SystemIds.DataListOptionSearch.UNLIMITED;
    }

    @Override
    protected DataListOptionSearch emptySearch() {
        return new DataListOptionSearch();
    }

    @Override
    protected List<DataListOptionSearchPredicateEntity> loadPredicates(DataListOptionSearchEntity searchEntity) {
        return dataListOptionSearchPredicateRepository.findByDataListOptionSearchId(searchEntity.getId());
    }

    @Override
    protected Class<? extends SearchFinder<DataListOptionSearch>> finderClass() {
        return DataListOptionFinder.class;
    }

    @Override
    protected PaginationResult<DataListOptionEntity> runFreeSearch(DataListOptionSearch search, SimplePagination pagination) throws ServiceException {
        return dataListOptionSearchService.search(search, pagination);
    }

    @Override
    protected PaginationResult<DataListOptionEntity> runConfiguredSearch(DataListOptionSearch mainSearch, SimplePagination pagination) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        Specification<DataListOptionEntity> spec = dataListOptionSearchService.createFilterSpecification(mainSearch, apiUser.getDomainId(), apiUser.getLocale());
        spec = addConfiguredSorting(mainSearch, pagination, spec, DataListOptionSorter.class);
        Page<DataListOptionEntity> page = dataListOptionRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(page, pagination);
    }

    @Override
    protected void narrowSearch(DataListOptionSearch mainSearch, DataListOptionSearch narrowSearch) {
        if (narrowSearch == null) {
            return;
        }
        for (Pair<Function<DataListOptionSearch, Set>, BiConsumer<DataListOptionSearch, Set>> functionPair : DataListOptionSearch.SET_FIELDS) {
            Set mainSet = functionPair.getKey().apply(mainSearch);
            Set narrowSet = functionPair.getKey().apply(narrowSearch);
            functionPair.getValue().accept(mainSearch, narrowSet(mainSet, narrowSet));
        }
        for (Pair<Function<DataListOptionSearch, Ternary>, BiConsumer<DataListOptionSearch, Ternary>> functionPair : DataListOptionSearch.TERNARY_FIELD) {
            Ternary mainTernary = functionPair.getKey().apply(mainSearch);
            Ternary narrowTernary = functionPair.getKey().apply(narrowSearch);
            functionPair.getValue().accept(mainSearch, TernaryUtils.narrow(mainTernary, narrowTernary));
        }
    }
}
