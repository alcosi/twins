package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.KitUtils;
import org.cambium.common.util.MapUtils;
import org.cambium.service.EntitySmartService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.search.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.domain.search.TwinSort;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.featurer.search.SearchFinder;
import org.twins.core.featurer.search.SearchSorter;
import org.twins.core.featurer.twin.finder.TwinFinder;
import org.twins.core.featurer.twin.sorter.TwinSorter;
import org.twins.core.service.ConfiguredSearchService;
import org.twins.core.service.search.TwinSearchSortService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Configured-search service for saved twin searches (see docs/configured_search_architecture.md).
 * The inherited findConfigured/countConfigured pipelines run predicates through TwinFinder featurers;
 * note they do not yet support head_twin_search_id (head search and concat executors are ignored).
 */
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
public class TwinSearchServiceV3 extends ConfiguredSearchService<TwinSearchEntity, TwinSearchPredicateEntity, BasicSearch, TwinEntity, UUID> {
    private final TwinSearchRepository twinSearchRepository;
    private final TwinSearchPredicateRepository twinSearchPredicateRepository;
    private final TwinRepository twinRepository;
    private final TwinSearchServiceV2 twinSearchServiceV2;
    private final TwinSearchSortService twinSearchSortService;
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public CrudRepository<TwinSearchEntity, UUID> entityRepository() {
        return twinSearchRepository;
    }

    @Override
    public Function<TwinSearchEntity, UUID> entityGetIdFunction() {
        return TwinSearchEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinSearchEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        // NULL domain_id = global (system) search, readable cross-domain; see V1.4.xxx.02 migration
        return checkDomainAccessDenied(entity.getDomainId(), entity.logShort(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(TwinSearchEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    protected UUID unlimitedSearchId() {
        return SystemIds.TwinSearch.UNLIMITED;
    }

    @Override
    protected List<TwinSearchPredicateEntity> loadPredicates(TwinSearchEntity searchEntity) {
        return twinSearchPredicateRepository.findByTwinSearchId(searchEntity.getId());
    }

    /**
     * TwinFinder featurers are declared on {@link TwinSearch}; {@link BasicSearch} inherits it, so
     * every finder safely consumes the narrower search object (consumer-side downcast).
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Class<? extends SearchFinder<BasicSearch>> finderClass() {
        return (Class<? extends SearchFinder<BasicSearch>>) (Class<?>) TwinFinder.class;
    }

    /**
     * Twin saved searches store sorting as twin_search_sort rows (one TwinSorter featurer per sorted
     * twin class field) — no single SearchSorter exists; the overridden {@link #addConfiguredSorting}
     * applies them, so this hook is never consulted by the flow.
     */
    @Override
    protected Class<? extends SearchSorter<TwinEntity>> sorterClass() {
        return null;
    }

    @Override
    protected JpaSpecificationExecutor<TwinEntity> searchExecutor() {
        return twinRepository;
    }

    @Override
    protected TwinSearchServiceV2 entitySearchService() {
        return twinSearchServiceV2;
    }

    /**
     * Set-typed fields are narrowed by the generic SearchFilter scan (see {@link TwinSearch});
     * link maps are Map&lt;UUID, Set&lt;UUID&gt;&gt;, which the scan does not cover (its Map branch
     * is Map&lt;UUID, Boolean&gt; only), so they are narrowed here.
     */
    @Override
    protected void narrowSearchSpecific(BasicSearch mainSearch, BasicSearch narrowSearch) {
        mainSearch.setDstLinksAnyOfList(MapUtils.narrowMapOfSets(mainSearch.getDstLinksAnyOfList(), narrowSearch.getDstLinksAnyOfList()));
        mainSearch.setDstLinksNoAnyOfList(MapUtils.narrowMapOfSets(mainSearch.getDstLinksNoAnyOfList(), narrowSearch.getDstLinksNoAnyOfList()));
        mainSearch.setSrcLinksAnyOfList(MapUtils.narrowMapOfSets(mainSearch.getSrcLinksAnyOfList(), narrowSearch.getSrcLinksAnyOfList()));
        mainSearch.setSrcLinksNoAnyOfList(MapUtils.narrowMapOfSets(mainSearch.getSrcLinksNoAnyOfList(), narrowSearch.getSrcLinksNoAnyOfList()));
    }

    /**
     * Applies the configured twin_search_sort rows (each via its own TwinSorter featurer) when sorting
     * is forced or the client did not request a sort (neither request-body sorts nor the pagination
     * query sort); client sorts are twinClassFieldId→direction maps applied via the base
     * {@link #addClientSorting}. Replaces the base sorter-featurer resolution, which does not exist
     * for twin searches. Sort precedence: client sorts > configured sorts (unless force_sorting) >
     * pagination query sort > entity default.
     */
    @Override
    protected Specification<TwinEntity> addConfiguredSorting(BasicSearch mainSearch, Map<UUID, SortDirection> clientSorts, SimplePagination pagination, Specification<TwinEntity> spec) throws ServiceException {
        TwinSearchEntity searchEntity = mainSearch.getConfiguredSearch();
        boolean hasClientSorts = MapUtils.isNotEmpty(clientSorts);
        boolean hasPaginationSort = pagination != null && pagination.getSort() != null;
        if (searchEntity != null && (Boolean.TRUE.equals(searchEntity.getForceSorting()) || (!hasClientSorts && !hasPaginationSort))) {
            twinSearchSortService.loadSorts(searchEntity);
            if (KitUtils.isNotEmpty(searchEntity.getSortKit())) {
                List<TwinSort> sorts = new ArrayList<>();
                for (TwinSearchSortEntity sortEntity : searchEntity.getSortKit().getList())
                    sorts.add(new TwinSort().setTwinClassFieldId(sortEntity.getTwinClassFieldId()).setDirection(sortEntity.getDirection()));
                twinClassFieldService.loadTwinClassFieldsForTwinSorts(sorts);
                boolean sortApplied = false;
                for (TwinSort twinSort : sorts) {
                    TwinSorter sorter = featurerService.getFeaturer(twinSort.getTwinClassField().getTwinSorterFeaturerId(), TwinSorter.class);
                    var sortFunction = sorter.createSort(twinSort.getTwinClassField().getTwinSorterParams(), twinSort.getTwinClassField(), twinSort.getDirection());
                    if (sortFunction != null) {
                        spec = sortFunction.apply(spec);
                        sortApplied = true;
                        if (pagination != null)
                            pagination.setSort(null);
                    }
                }
                if (sortApplied)
                    return spec;
            }
        }
        if (hasClientSorts) {
            spec = addClientSorting(clientSorts, spec);
            if (pagination != null)
                pagination.setSort(null);
            return spec;
        }
        // No configured sort applied and the client did not request one: entity default sort keeps
        // offset pagination deterministic.
        if (!hasPaginationSort) {
            Specification<TwinEntity> defaultSort = createDefaultSortSpecification(authService.getApiUser().getLocale());
            if (defaultSort != null)
                spec = spec.and(defaultSort);
        }
        return spec;
    }
}
