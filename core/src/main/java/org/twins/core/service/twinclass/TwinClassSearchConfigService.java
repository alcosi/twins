package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.twins.core.dao.twinclass.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.TwinClassSearch;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.featurer.classfinder.ClassFinder;
import org.twins.core.featurer.classfinder.sorter.ClassSorter;
import org.twins.core.featurer.search.SearchFinder;
import org.twins.core.service.ConfiguredSearchService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.cambium.common.util.SetUtils.narrowSet;

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
    protected TwinClassSearch emptySearch() {
        return new TwinClassSearch();
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
    protected PaginationResult<TwinClassEntity> runFreeSearch(TwinClassSearch search, SimplePagination pagination) throws ServiceException {
        return twinClassSearchService.search(search, pagination);
    }

    @Override
    protected PaginationResult<TwinClassEntity> runConfiguredSearch(TwinClassSearch mainSearch, SimplePagination pagination) throws ServiceException {
        TwinClassSearchEntity searchEntity = mainSearch.getConfiguredSearch();
        // Apply ClassSorter for configured search; fall back to the free pipeline when no sorter applies
        if (searchEntity.getForceSorting() || pagination.getSort() == null) {
            ClassSorter classSorter = featurerService.getFeaturer(searchEntity.getClassSorterFeaturerId(), ClassSorter.class);
            var sortFunction = classSorter.createSort(searchEntity.getClassSorterParams());
            if (sortFunction != null) {
                ApiUser apiUser = authService.getApiUser();
                Specification<TwinClassEntity> spec = twinClassSearchService.createFilterSpecification(mainSearch, apiUser.getDomainId(), apiUser.getLocale());
                spec = sortFunction.apply(spec);
                pagination.setSort(null);
                Page<TwinClassEntity> page = twinClassRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
                return PaginationUtils.convertInPaginationResult(page, pagination);
            }
        }
        return twinClassSearchService.search(mainSearch, pagination);
    }

    @Override
    protected void narrowSearch(TwinClassSearch mainSearch, TwinClassSearch narrowSearch) {
        if (narrowSearch == null)
            return;
        for (Pair<Function<TwinClassSearch, Set>, BiConsumer<TwinClassSearch, Set>> functionPair : TwinClassSearch.SET_FIELD) {
            Set mainSet = functionPair.getKey().apply(mainSearch);
            Set narrowSet = functionPair.getKey().apply(narrowSearch);
            functionPair.getValue().accept(mainSearch, narrowSet(mainSet, narrowSet));
        }
        for (Pair<Function<TwinClassSearch, Ternary>, BiConsumer<TwinClassSearch, Ternary>> functionPair : TwinClassSearch.TERNARY_FIELD) {
            Ternary mainSet = functionPair.getKey().apply(mainSearch);
            Ternary narrowSet = functionPair.getKey().apply(narrowSearch);
            functionPair.getValue().accept(mainSearch, TernaryUtils.narrow(mainSet, narrowSet));
        }
    }
}
