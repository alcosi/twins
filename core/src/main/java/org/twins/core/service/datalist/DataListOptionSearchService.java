package org.twins.core.service.datalist;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.*;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.datalist.DataListStatus;
import org.twins.core.enums.sort.DataListOptionGroupField;
import org.twins.core.enums.sort.DataListOptionSortField;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.datalist.finder.DataListOptionFinder;
import org.twins.core.featurer.datalist.sorter.DataListOptionSorter;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperList;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDatalist;
import org.twins.core.service.EntitySearchService;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.cambium.common.util.EnumUtils.convertOrEmpty;
import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NFieldDirect;
import static org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecificationDirect;
import static org.twins.core.dao.specifications.CommonSpecification.toSortSpecification;
import static org.twins.core.dao.specifications.datalist.DataListOptionSpecification.*;

//Log calls that took more than 2 seconds
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class DataListOptionSearchService extends EntitySearchService
        <DataListOptionSearch, DataListOptionEntity, DataListOptionSortField, DataListOptionGroupField> {
    private final AuthService authService;
    private final DataListOptionRepository dataListOptionRepository;
    @Lazy
    private final TwinClassFieldService twinClassFieldService;
    @Lazy
    private final FeaturerService featurerService;
    private final DataListOptionSearchRepository dataListOptionSearchRepository;
    private final DataListOptionSearchPredicateRepository dataListOptionSearchPredicateRepository;
    private final DataListOptionSearchConfigService dataListOptionSearchConfigService;

    @Override
    public JpaSpecificationExecutor<DataListOptionEntity> jpaSpecificationExecutor() {
        return dataListOptionRepository;
    }

    @Override
    public DataListOptionSearch emptySearch() {
        return new DataListOptionSearch();
    }

    @Override
    protected DataListOptionEntity newEntity() {
        return new DataListOptionEntity();
    }

    @Override
    protected Class<DataListOptionEntity> entityClass() {
        return DataListOptionEntity.class;
    }

    public List<DataListOptionEntity> findDataListOptions(DataListOptionSearch search) throws ServiceException {
        return dataListOptionRepository.findAll(createFilterSpecification(search));
    }

    public PaginationResult<DataListOptionEntity> findDataListOptions(UUID searchId, Map<String, String> namedParamsMap, DataListOptionSearch narrowSearch, SimplePagination pagination) throws ServiceException {
        if (SystemIds.DataListOptionSearch.UNLIMITED.equals(searchId)) {
            return search(narrowSearch, pagination);
        }

        DataListOptionSearchEntity searchEntity = dataListOptionSearchConfigService.findEntitySafe(searchId);
        List<DataListOptionSearchPredicateEntity> searchPredicates = dataListOptionSearchPredicateRepository.findByDataListOptionSearchId(searchId);

        DataListOptionSearch mainSearch = new DataListOptionSearch();
        for (DataListOptionSearchPredicateEntity predicate : searchPredicates) {
            DataListOptionFinder optionFinder = featurerService.getFeaturer(predicate.getOptionFinderFeaturerId(), DataListOptionFinder.class);
            optionFinder.concatSearch(predicate.getOptionFinderParams(), mainSearch, namedParamsMap);
        }

        narrowSearch(mainSearch, narrowSearch);
        mainSearch.setConfiguredSearch(searchEntity);

        return findDataListOptionsConfigured(mainSearch, pagination);
    }

    private PaginationResult<DataListOptionEntity> findDataListOptionsConfigured(DataListOptionSearch search, SimplePagination pagination) throws ServiceException {
        if (search == null)
            search = emptySearch();
        Specification<DataListOptionEntity> spec = createFilterSpecification(search);
        spec = addConfiguredSorting(search, pagination, spec);
        Page<DataListOptionEntity> page = dataListOptionRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(page, pagination);
    }

    @Override
    public Specification<DataListOptionEntity> createFilterSpecification(DataListOptionSearch search, UUID domainId, Locale locale) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        limitSearchByValidForTwinClassFieldIdList(search);
        return Specification.allOf(
                checkFieldUuid(domainId, DataListOptionEntity.Fields.dataList, DataListEntity.Fields.domainId),
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
                joinAndSearchByI18NFieldDirect(DataListOptionEntity.Fields.optionI18nTranslationsSpecOnly, search.getOptionI18nLikeList(), locale, true, false),
                joinAndSearchByI18NFieldDirect(DataListOptionEntity.Fields.optionI18nTranslationsSpecOnly, search.getOptionI18nNotLikeList(), locale, true, true),
                checkDataListSubset(search.getDataListSubsetIdList(), false),
                checkDataListSubset(search.getDataListSubsetIdExcludeList(), true),
                checkDataListSubsetKey(search.getDataListSubsetKeyList(), false, true),
                checkDataListSubsetKey(search.getDataListSubsetKeyExcludeList(), true, true),
                checkFieldIn(search.getExternalIdList(), false, true, false, DataListOptionEntity.Fields.externalId),
                checkFieldIn(search.getExternalIdExcludeList(), true, true, false, DataListOptionEntity.Fields.externalId),
                checkTernary(search.getCustom(), DataListOptionEntity.Fields.custom));
    }

    private Specification<DataListOptionEntity> createBusinessAccountSpecification(ApiUser apiUser, DataListOptionSearch search) {
        if (apiUser.isBusinessAccountSpecified()) {
            return Specification.allOf(
                    checkUuidIn(search.getBusinessAccountIdList(), false, true, DataListOptionEntity.Fields.businessAccountId),
                    checkUuidIn(search.getBusinessAccountIdExcludeList(), true, true, DataListOptionEntity.Fields.businessAccountId)
            );
        } else {
            return (root, query, cb) -> root.get(DataListOptionEntity.Fields.businessAccountId).isNull();
        }
    }

    public void limitSearchByValidForTwinClassFieldIdList(DataListOptionSearch search) throws ServiceException {
        if (CollectionUtils.isEmpty(search.getValidForTwinClassFieldIdList())) {
            return;
        }
        var twinClassFieldKit = twinClassFieldService.findEntitiesSafe(search.getValidForTwinClassFieldIdList());
        for (var fieldEntity : twinClassFieldKit.getCollection()) {
            FieldTyper<?, ?, ?, ?> fieldTyper = featurerService.getFeaturer(fieldEntity.getFieldTyperFeaturerId(), FieldTyper.class);

            if (fieldTyper.getStorageType() != TwinFieldStorageDatalist.class) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE, "Wrong fieldTyper for [" + fieldEntity.getId() + "]");
            }
            FieldTyperList fieldTyperList = (FieldTyperList) fieldTyper;
            Properties properties = fieldTyper.extractProperties(fieldEntity.getFieldTyperParams());
            search
                    .addDataListId(fieldTyperList.getDataListId(properties), false)
                    .setIdList(CollectionUtils.safeAdd(search.getIdList(), fieldTyperList.getDataListOptionIds(properties)))
                    .setIdExcludeList(CollectionUtils.safeAdd(search.getIdExcludeList(), fieldTyperList.getDataListOptionExcludeIds(properties)))
                    .setDataListSubsetIdList(CollectionUtils.safeAdd(search.getDataListSubsetIdList(), fieldTyperList.getDataListSubsetIds(properties)))
                    .setDataListSubsetIdExcludeList(CollectionUtils.safeAdd(search.getDataListSubsetIdExcludeList(), fieldTyperList.getDataListSubsetExcludeIds(properties)));
        }
    }

    @Override
    public Specification<DataListOptionEntity> createSortSpecification(DataListOptionSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        if (sortField == null)
            sortField = DataListOptionSortField.createdAt;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case createdAt ->
                    toSortSpecification(ascending, DataListOptionEntity.Fields.createdAt);
            case option ->
                    toSortSpecification(ascending, DataListOptionEntity.Fields.option);
            case externalId ->
                    toSortSpecification(ascending, DataListOptionEntity.Fields.externalId);
            case status ->
                    toSortSpecification(ascending, DataListOptionEntity.Fields.status);
            case optionName ->
                    toSortSpecificationDirect(ascending, locale, DataListOptionEntity.Fields.optionI18nTranslationsSpecOnly);
            case dataListKey ->
                    toSortSpecification(ascending, DataListOptionEntity.Fields.dataList, DataListEntity.Fields.key);
            case dataListName ->
                    toSortSpecificationDirect(ascending, locale, DataListOptionEntity.Fields.dataList, DataListEntity.Fields.nameI18nTranslationsSpecOnly);
        };
    }

    @Override
    public String convertToEntityField(DataListOptionGroupField groupField) throws ServiceException {
        return switch (groupField) {
            case dataListId -> DataListOptionEntity.Fields.dataListId;
            case businessAccountId -> DataListOptionEntity.Fields.businessAccountId;
            case status -> DataListOptionEntity.Fields.status;
            case custom -> DataListOptionEntity.Fields.custom;
        };
    }

    @Override
    public void mapGroupedField(DataListOptionEntity entity, DataListOptionGroupField field, Object o) {
        switch (field) {
            case dataListId -> entity.setDataListId((UUID) o);
            case businessAccountId -> entity.setBusinessAccountId((UUID) o);
            case status -> entity.setStatus((DataListStatus) o);
            case custom -> entity.setCustom((Boolean) o);
        }
    }

    protected void narrowSearch(DataListOptionSearch mainSearch, DataListOptionSearch narrowSearch) {
        if (narrowSearch == null) {
            return;
        }
        for (Pair<Function<DataListOptionSearch, Set>, BiConsumer<DataListOptionSearch, Set>> functionPair : DataListOptionSearch.SET_FIELDS) {
            Set mainSet = functionPair.getKey().apply(mainSearch);
            Set narrowSet = functionPair.getKey().apply(narrowSearch);
            functionPair.getValue().accept(mainSearch, SetUtils.narrowSet(mainSet, narrowSet));
        }
        for (Pair<Function<DataListOptionSearch, Ternary>, BiConsumer<DataListOptionSearch, Ternary>> functionPair : DataListOptionSearch.TERNARY_FIELD) {
            Ternary mainTernary = functionPair.getKey().apply(mainSearch);
            Ternary narrowTernary = functionPair.getKey().apply(narrowSearch);
            functionPair.getValue().accept(mainSearch, TernaryUtils.narrow(mainTernary, narrowTernary));
        }
    }

    private Specification<DataListOptionEntity> addConfiguredSorting(DataListOptionSearch search, SimplePagination pagination, Specification<DataListOptionEntity> spec) throws ServiceException {
        DataListOptionSearchEntity searchEntity = search.getConfiguredSearch();
        if (searchEntity != null && (searchEntity.getForceSorting() || pagination == null || pagination.getSort() == null)) {
            DataListOptionSorter optionSorter = featurerService.getFeaturer(searchEntity.getOptionSorterFeaturerId(), DataListOptionSorter.class);
            var sortFunction = optionSorter.createSort(searchEntity.getOptionSorterParams());
            if (sortFunction != null) {
                spec = sortFunction.apply(spec);
                if (pagination != null) {
                    pagination.setSort(null);
                }
            }
        }
        return spec;
    }
}
