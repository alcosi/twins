package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.service.EntitySmartService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.*;
import org.twins.core.domain.search.TwinClassSearch;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.featurer.classfinder.ClassFinder;
import org.twins.core.featurer.classfinder.sorter.ClassSorter;
import org.twins.core.featurer.search.SearchFinder;
import org.twins.core.featurer.search.SearchSorter;
import org.twins.core.service.ConfiguredSearchService;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassSearchConfigService extends ConfiguredSearchService<TwinClassSearchEntity, TwinClassSearchPredicateEntity, TwinClassSearch, TwinClassEntity> {
    private final TwinClassSearchRepository classSearchRepository;
    private final TwinClassSearchPredicateRepository classSearchPredicateRepository;
    private final TwinClassRepository twinClassRepository;
    private final TwinClassSearchService twinClassSearchService;

    @Override
    public CrudRepository<TwinClassSearchEntity, UUID> entityRepository() {
        return classSearchRepository;
    }

    @Override
    public Function<TwinClassSearchEntity, UUID> entityGetIdFunction() {
        return TwinClassSearchEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassSearchEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return checkDomainAccessDenied(entity.getDomainId(), entity.logNormal(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(TwinClassSearchEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    protected UUID unlimitedSearchId() {
        return SystemIds.TwinClassSearch.UNLIMITED;
    }

    @Override
    protected TwinClassSearch newSearch() {
        return twinClassSearchService.emptySearch();
    }

    @Override
    protected List<TwinClassSearchPredicateEntity> loadPredicates(TwinClassSearchEntity searchEntity) {
        return classSearchPredicateRepository.findByTwinClassSearchId(searchEntity.getId());
    }

    @Override
    protected Class<? extends SearchFinder<TwinClassSearch>> finderClass() {
        return ClassFinder.class;
    }

    @Override
    protected Class<? extends SearchSorter<TwinClassEntity>> sorterClass() {
        return ClassSorter.class;
    }

    @Override
    protected Specification<TwinClassEntity> createFilterSpecification(TwinClassSearch search, UUID domainId, Locale locale) throws ServiceException {
        return twinClassSearchService.createFilterSpecification(search, domainId, locale);
    }

    @Override
    protected JpaSpecificationExecutor<TwinClassEntity> searchExecutor() {
        return twinClassRepository;
    }

    @Override
    protected PaginationResult<TwinClassEntity> runFreeSearch(TwinClassSearch search, SimplePagination pagination) throws ServiceException {
        return twinClassSearchService.search(search, pagination);
    }
}
