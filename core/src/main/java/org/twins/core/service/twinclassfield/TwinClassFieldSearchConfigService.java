package org.twins.core.service.twinclassfield;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.MapUtils;
import org.cambium.common.util.Ternary;
import org.cambium.common.util.TernaryUtils;
import org.cambium.service.EntitySmartService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.*;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.featurer.classfield.finder.FieldFinder;
import org.twins.core.featurer.classfield.sorter.FieldSorter;
import org.twins.core.featurer.search.SearchFinder;
import org.twins.core.featurer.search.SearchSorter;
import org.twins.core.service.ConfiguredSearchService;

import java.util.List;
import java.util.Locale;
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
    protected TwinClassFieldSearch newSearch() {
        return twinClassFieldSearchService.emptySearch();
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
    protected Class<? extends SearchSorter<TwinClassFieldEntity>> sorterClass() {
        return FieldSorter.class;
    }

    @Override
    protected Specification<TwinClassFieldEntity> createFilterSpecification(TwinClassFieldSearch search, UUID domainId, Locale locale) throws ServiceException {
        return twinClassFieldSearchService.createFilterSpecification(search, domainId, locale);
    }

    @Override
    protected JpaSpecificationExecutor<TwinClassFieldEntity> searchExecutor() {
        return twinClassFieldRepository;
    }

    @Override
    protected PaginationResult<TwinClassFieldEntity> runFreeSearch(TwinClassFieldSearch search, SimplePagination pagination) throws ServiceException {
        return twinClassFieldSearchService.search(search, pagination);
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
