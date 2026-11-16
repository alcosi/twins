package org.twins.core.service.twinclassfield;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.MapUtils;
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
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.featurer.classfield.finder.FieldFinder;
import org.twins.core.featurer.classfield.sorter.FieldSorter;
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
public class TwinClassFieldSearchConfigService extends ConfiguredSearchService<TwinClassFieldSearchEntity, TwinClassFieldSearchPredicateEntity, TwinClassFieldSearch, TwinClassFieldEntity> {
    private final TwinClassFieldSearchRepository fieldSearchRepository;
    private final TwinClassFieldSearchPredicateRepository fieldSearchPredicateRepository;
    private final TwinClassFieldRepository twinClassFieldRepository;
    private final TwinClassFieldSearchService twinClassFieldSearchService;

    @Override
    public CrudRepository<TwinClassFieldSearchEntity, UUID> entityRepository() {
        return fieldSearchRepository;
    }

    @Override
    public Function<TwinClassFieldSearchEntity, UUID> entityGetIdFunction() {
        return TwinClassFieldSearchEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFieldSearchEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return checkDomainAccessDenied(entity.getDomainId(), entity.logNormal(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(TwinClassFieldSearchEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    protected UUID unlimitedSearchId() {
        return SystemIds.TwinClassFieldSearch.UNLIMITED;
    }

    @Override
    protected TwinClassFieldSearch emptySearch() {
        return new TwinClassFieldSearch()
                .setExcludeSystemFields(false);
    }

    @Override
    protected List<TwinClassFieldSearchPredicateEntity> loadPredicates(TwinClassFieldSearchEntity searchEntity) {
        return fieldSearchPredicateRepository.findByTwinClassFieldSearchId(searchEntity.getId());
    }

    @Override
    protected Class<? extends SearchFinder<TwinClassFieldSearch>> finderClass() {
        return FieldFinder.class;
    }

    @Override
    protected PaginationResult<TwinClassFieldEntity> runFreeSearch(TwinClassFieldSearch search, SimplePagination pagination) throws ServiceException {
        return twinClassFieldSearchService.search(search, pagination);
    }

    @Override
    protected PaginationResult<TwinClassFieldEntity> runConfiguredSearch(TwinClassFieldSearch mainSearch, SimplePagination pagination) throws ServiceException {
        TwinClassFieldSearchEntity searchEntity = mainSearch.getConfiguredSearch();
        // Apply FieldSorter for configured search; fall back to the free pipeline when no sorter applies
        if (searchEntity.getForceSorting() || pagination.getSort() == null) {
            FieldSorter fieldSorter = featurerService.getFeaturer(searchEntity.getFieldSorterFeaturerId(), FieldSorter.class);
            var sortFunction = fieldSorter.createSort(searchEntity.getFieldSorterParams());
            if (sortFunction != null) {
                ApiUser apiUser = authService.getApiUser();
                Specification<TwinClassFieldEntity> spec = twinClassFieldSearchService.createFilterSpecification(mainSearch, apiUser.getDomainId(), apiUser.getLocale());
                spec = sortFunction.apply(spec);
                pagination.setSort(null);
                Page<TwinClassFieldEntity> page = twinClassFieldRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
                return PaginationUtils.convertInPaginationResult(page, pagination);
            }
        }
        return twinClassFieldSearchService.findTwinClassField(mainSearch, pagination);
    }

    @Override
    protected void narrowSearch(TwinClassFieldSearch mainSearch, TwinClassFieldSearch narrowSearch) {
        if (narrowSearch == null)
            return;

        for (Pair<Function<TwinClassFieldSearch, Set>, BiConsumer<TwinClassFieldSearch, Set>> functionPair : TwinClassFieldSearch.SET_FIELDS) {
            Set mainSet = functionPair.getKey().apply(mainSearch);
            Set narrowSet = functionPair.getKey().apply(narrowSearch);
            functionPair.getValue().accept(mainSearch, narrowSet(mainSet, narrowSet));
        }
        for (Pair<Function<TwinClassFieldSearch, Ternary>, BiConsumer<TwinClassFieldSearch, Ternary>> functionPair : TwinClassFieldSearch.TERNARY_FIELD) {
            Ternary mainSet = functionPair.getKey().apply(mainSearch);
            Ternary narrowSet = functionPair.getKey().apply(narrowSearch);
            functionPair.getValue().accept(mainSearch, TernaryUtils.narrow(mainSet, narrowSet));
        }

        mainSearch.setTwinClassIdMap(MapUtils.narrowMapOfBooleans(mainSearch.getTwinClassIdMap(), narrowSearch.getTwinClassIdMap(), Boolean.TRUE));
        mainSearch.setTwinClassIdExcludeMap(MapUtils.narrowMapOfBooleans(mainSearch.getTwinClassIdExcludeMap(), narrowSearch.getTwinClassIdExcludeMap(), Boolean.TRUE));
    }
}
