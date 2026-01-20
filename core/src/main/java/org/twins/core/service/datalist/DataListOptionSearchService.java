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
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.datalist.finder.DataListOptionFinder;
import org.twins.core.featurer.datalist.sorter.DataListOptionSorter;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperList;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDatalist;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.cambium.common.util.EnumUtils.convertOrEmpty;
import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.datalist.DataListOptionSpecification.*;

//Log calls that took more than 2 seconds
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class DataListOptionSearchService extends EntitySecureFindServiceImpl<DataListOptionSearchEntity> {
    private final AuthService authService;
    private final DataListOptionRepository dataListOptionRepository;
    @Lazy
    private final TwinClassFieldService twinClassFieldService;
    @Lazy
    private final FeaturerService featurerService;
    private final DataListOptionSearchRepository dataListOptionSearchRepository;
    private final DataListOptionSearchPredicateRepository dataListOptionSearchPredicateRepository;

    public List<DataListOptionEntity> findDataListOptions(DataListOptionSearch search) throws ServiceException {
        Specification<DataListOptionEntity> spec = createDataListOptionSearchSpecification(search);
        return dataListOptionRepository.findAll(spec);
    }

    public PaginationResult<DataListOptionEntity> findDataListOptionForDomain(DataListOptionSearch search, SimplePagination pagination) throws ServiceException {
        Specification<DataListOptionEntity> spec = createDataListOptionSearchSpecification(search);
        spec = addSorting(search, pagination, spec);
        Page<DataListOptionEntity> ret = dataListOptionRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    public PaginationResult<DataListOptionEntity> findDataListOptions(UUID searchId, Map<String, String> namedParamsMap, DataListOptionSearch narrowSearch, SimplePagination pagination) throws ServiceException {
        if (SystemEntityService.DATA_LIST_OPTION_SEARCH_UNLIMITED.equals(searchId)) {
            return findDataListOptionForDomain(narrowSearch, pagination);
        }

        DataListOptionSearchEntity searchEntity = findEntitySafe(searchId);
        List<DataListOptionSearchPredicateEntity> searchPredicates = dataListOptionSearchPredicateRepository.findByDataListOptionSearchId(searchId);

        DataListOptionSearch mainSearch = new DataListOptionSearch();
        for (DataListOptionSearchPredicateEntity predicate : searchPredicates) {
            DataListOptionFinder optionFinder = featurerService.getFeaturer(predicate.getOptionFinderFeaturerId(), DataListOptionFinder.class);
            optionFinder.concatSearch(predicate.getOptionFinderParams(), mainSearch, namedParamsMap);
        }

        narrowSearch(mainSearch, narrowSearch);
        mainSearch.setConfiguredSearch(searchEntity);

        return findDataListOptionForDomain(mainSearch, pagination);
    }

    private Specification<DataListOptionEntity> createDataListOptionSearchSpecification(DataListOptionSearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        limitSearchByValidForTwinClassFieldIdList(search);
        return Specification.allOf(
                checkFieldUuid(apiUser.getDomainId(), DataListOptionEntity.Fields.dataList, DataListEntity.Fields.domainId),
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
    public CrudRepository<DataListOptionSearchEntity, UUID> entityRepository() {
        return dataListOptionSearchRepository;
    }

    @Override
    public Function<DataListOptionSearchEntity, UUID> entityGetIdFunction() {
        return DataListOptionSearchEntity::getId;
    }


    @Override
    public boolean isEntityReadDenied(DataListOptionSearchEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        if (entity.getDomainId() != null && !entity.getDomainId().equals(domainId)) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logNormal() + " is not allowed in domain[" + domainId + "]");
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(DataListOptionSearchEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
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

    private Specification<DataListOptionEntity> addSorting(DataListOptionSearch search, SimplePagination pagination, Specification<DataListOptionEntity> spec) throws ServiceException {
        DataListOptionSearchEntity searchEntity = search.getConfiguredSearch();
        if (searchEntity != null && (searchEntity.getForceSorting() || pagination == null || pagination.getSort() == null)) {
            DataListOptionSorter optionSorter = featurerService.getFeaturer(searchEntity.getOptionSorterFeaturerId(), DataListOptionSorter.class);
            var sortFunction = optionSorter.createSort(searchEntity.getOptionSorterParams());
            if (sortFunction != null) {
                spec = sortFunction.apply(spec);
                if(pagination != null) {
                    pagination.setSort(null);
                }
            }
        }
        return spec;
    }
}
