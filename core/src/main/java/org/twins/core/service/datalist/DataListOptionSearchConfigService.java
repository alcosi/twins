package org.twins.core.service.datalist;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.service.EntitySmartService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.*;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.featurer.datalist.finder.DataListOptionFinder;
import org.twins.core.featurer.datalist.sorter.DataListOptionSorter;
import org.twins.core.featurer.search.SearchFinder;
import org.twins.core.featurer.search.SearchSorter;
import org.twins.core.service.ConfiguredSearchService;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

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
    protected DataListOptionSearch newSearch() {
        return dataListOptionSearchService.emptySearch();
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
    protected Class<? extends SearchSorter<DataListOptionEntity>> sorterClass() {
        return DataListOptionSorter.class;
    }

    @Override
    protected Specification<DataListOptionEntity> createFilterSpecification(DataListOptionSearch search, UUID domainId, Locale locale) throws ServiceException {
        return dataListOptionSearchService.createFilterSpecification(search, domainId, locale);
    }

    @Override
    protected JpaSpecificationExecutor<DataListOptionEntity> searchExecutor() {
        return dataListOptionRepository;
    }

    @Override
    protected PaginationResult<DataListOptionEntity> runFreeSearch(DataListOptionSearch search, SimplePagination pagination) throws ServiceException {
        return dataListOptionSearchService.search(search, pagination);
    }
}
