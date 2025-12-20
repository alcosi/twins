package org.twins.core.service.twinclass;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
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
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.twinclass.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.FieldProjectionSearch;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.enums.projection.ProjectionFieldSelector;
import org.twins.core.featurer.classfield.finder.FieldFinder;
import org.twins.core.featurer.classfield.sorter.FieldSorter;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.cambium.common.util.SetUtils.narrowSet;
import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.twinclass.TwinClassFieldSpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassFieldSearchService extends EntitySecureFindServiceImpl<TwinClassFieldSearchEntity> {

    private final TwinClassFieldRepository twinClassFieldRepository;
    private final TwinClassFieldSearchRepository fieldSearchRepository;
    private final TwinClassFieldSearchPredicateRepository fieldSearchPredicateRepository;

    private final AuthService authService;
    private final TwinClassService twinClassService;
    private final FeaturerService featurerService;

    public PaginationResult<TwinClassFieldEntity> findTwinClassField(TwinClassFieldSearch search, SimplePagination pagination) throws ServiceException {
        if (search.isInactiveSearch())
            return PaginationResult.EMPTY;
        Specification<TwinClassFieldEntity> specification = createTwinClassFieldSearchSpecification(search);
        specification = addSorting(search, pagination, specification);
        Page<TwinClassFieldEntity> ret = twinClassFieldRepository.findAll(specification, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    public List<TwinClassFieldEntity> findTwinClassField(TwinClassFieldSearch search) throws ServiceException {
        if (search.isInactiveSearch())
            return Collections.emptyList();
        Specification<TwinClassFieldEntity> spec = createTwinClassFieldSearchSpecification(search);
        spec = addSorting(search, null, spec);
        return twinClassFieldRepository.findAll(spec);
    }

    public PaginationResult<TwinClassFieldEntity> findTwinClassField(UUID searchId, Map<String, String> namedParamsMap, TwinClassFieldSearch narrowSearch, SimplePagination pagination) throws ServiceException {
        if (SystemEntityService.TWIN_CLASS_FIELD_SEARCH_UNLIMITED.equals(searchId)) {
            return findTwinClassField(narrowSearch, pagination);
        }
        TwinClassFieldSearchEntity searchEntity = findEntitySafe(searchId);
        List<TwinClassFieldSearchPredicateEntity> searchPredicates = fieldSearchPredicateRepository.findByTwinClassFieldSearchId(searchEntity.getId());
        TwinClassFieldSearch mainSearch = new TwinClassFieldSearch()
                .setExcludeSystemFields(false);
        for (TwinClassFieldSearchPredicateEntity predicate : searchPredicates) {
            FieldFinder fieldFinder = featurerService.getFeaturer(predicate.getFieldFinderFeaturerId(), FieldFinder.class);
            fieldFinder.concatSearch(predicate.getFieldFinderParams(), mainSearch, namedParamsMap);
        }
        narrowSearch(mainSearch, narrowSearch);
        mainSearch.setConfiguredSearch(searchEntity);
        return findTwinClassField(mainSearch, pagination);
    }

    private Specification<TwinClassFieldEntity> createTwinClassFieldSearchSpecification(TwinClassFieldSearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return Specification.allOf(
                checkUuid(apiUser.getDomainId(), false, !search.isExcludeSystemFields(), TwinClassFieldEntity.Fields.twinClass, TwinClassEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, TwinClassFieldEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinClassFieldEntity.Fields.id),
                checkUuidIn(twinClassService.loadExtendsHierarchyClasses(search.getTwinClassIdMap()), false, false, TwinClassFieldEntity.Fields.twinClassId),
                checkUuidIn(twinClassService.loadExtendsHierarchyClasses(search.getTwinClassIdExcludeMap()), true, false, TwinClassFieldEntity.Fields.twinClassId),
                checkFieldLikeIn(search.getKeyLikeList(), false, true, TwinClassFieldEntity.Fields.key),
                checkFieldLikeIn(search.getKeyNotLikeList(), true, true, TwinClassFieldEntity.Fields.key),
                joinAndSearchByI18NField(TwinClassFieldEntity.Fields.nameI18n, search.getNameI18nLikeList(), apiUser.getLocale(), true, false),
                joinAndSearchByI18NField(TwinClassFieldEntity.Fields.nameI18n, search.getNameI18nNotLikeList(), apiUser.getLocale(), true, true),
                joinAndSearchByI18NField(TwinClassFieldEntity.Fields.descriptionI18n, search.getDescriptionI18nLikeList(), apiUser.getLocale(), true, false),
                joinAndSearchByI18NField(TwinClassFieldEntity.Fields.descriptionI18n, search.getDescriptionI18nNotLikeList(), apiUser.getLocale(), true, true),
                checkIntegerIn(search.getFieldTyperIdList(), false, TwinClassFieldEntity.Fields.fieldTyperFeaturerId),
                checkIntegerIn(search.getFieldTyperIdExcludeList(), true, TwinClassFieldEntity.Fields.fieldTyperFeaturerId),
                checkIntegerIn(search.getTwinSorterIdList(), false, TwinClassFieldEntity.Fields.twinSorterFeaturerId),
                checkIntegerIn(search.getTwinSorterIdExcludeList(), true, TwinClassFieldEntity.Fields.twinSorterFeaturerId),
                checkUuidIn(search.getViewPermissionIdList(), false, false, TwinClassFieldEntity.Fields.viewPermissionId),
                checkUuidIn(search.getViewPermissionIdExcludeList(), true, true, TwinClassFieldEntity.Fields.viewPermissionId),
                checkUuidIn(search.getViewPermissionIdList(), false, false, TwinClassFieldEntity.Fields.editPermissionId),
                checkUuidIn(search.getViewPermissionIdExcludeList(), true, true, TwinClassFieldEntity.Fields.editPermissionId),
                checkTernary(search.getRequired(), TwinClassFieldEntity.Fields.required),
                checkTernary(search.getSystem(), TwinClassFieldEntity.Fields.system),
                checkTernary(search.getDependentField(), TwinClassFieldEntity.Fields.dependentField),
                checkTernary(search.getHasDependentFields(), TwinClassFieldEntity.Fields.hasDependentFields),
                checkTernary(search.getProjectionField(), TwinClassFieldEntity.Fields.projectionField),
                checkTernary(search.getHasProjectionFields(), TwinClassFieldEntity.Fields.hasProjectedFields),
                checkFieldLikeIn(search.getExternalIdLikeList(), false, true, TwinClassFieldEntity.Fields.externalId),
                checkFieldLikeIn(search.getExternalIdNotLikeList(), true, true, TwinClassFieldEntity.Fields.externalId),
                checkFieldLongRange(search.getOrderRange(), TwinClassFieldEntity.Fields.order),
                checkProjections(search));

    }

    private Specification<TwinClassFieldEntity> checkProjections(TwinClassFieldSearch search) throws ServiceException {
        FieldProjectionSearch projectionSearch = search.getFieldProjectionSearch();
        if (projectionSearch == null) {
            return (root, query, cb) -> cb.conjunction();
        }

        ProjectionFieldSelector selector = projectionSearch.getProjectionFieldSelector();
        if (selector == null) {
            selector = ProjectionFieldSelector.all;
        }

        return switch (selector) {
            case src -> buildProjectionSpec(TwinClassFieldEntity.Fields.projectionsBySrc, projectionSearch.getSrcIdList(), projectionSearch.getDstIdList(), projectionSearch.getProjectionTypeIdList());
            case dst -> buildProjectionSpec(TwinClassFieldEntity.Fields.projectionsByDst, projectionSearch.getSrcIdList(), projectionSearch.getDstIdList(), projectionSearch.getProjectionTypeIdList());
            case all -> {
                Specification<TwinClassFieldEntity> srcSpec =
                        buildProjectionSpec(TwinClassFieldEntity.Fields.projectionsBySrc, projectionSearch.getSrcIdList(), projectionSearch.getDstIdList(), projectionSearch.getProjectionTypeIdList());
                Specification<TwinClassFieldEntity> dstSpec =
                        buildProjectionSpec(TwinClassFieldEntity.Fields.projectionsByDst, projectionSearch.getSrcIdList(), projectionSearch.getDstIdList(), projectionSearch.getProjectionTypeIdList());

                yield Specification.anyOf(srcSpec, dstSpec);
            }
        };
    }

    private Specification<TwinClassFieldEntity> addSorting(TwinClassFieldSearch search, SimplePagination pagination, Specification<TwinClassFieldEntity> specification) throws ServiceException {
        TwinClassFieldSearchEntity searchEntity = search.getConfiguredSearch();
        if (searchEntity != null &&
                (searchEntity.isForceSorting() || pagination == null || pagination.getSort() == null)) {
            FieldSorter fieldSorter = featurerService.getFeaturer(searchEntity.getFieldSorterFeaturerId(), FieldSorter.class);
            var sortFunction = fieldSorter.createSort(searchEntity.getFieldSorterParams());
            if (sortFunction != null) {
                specification = sortFunction.apply(specification);
                if (pagination != null)
                    pagination.setSort(null);
            }
        }
        return specification;
    }

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
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = entity.getDomainId() != null && !entity.getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.logNormal() + " is not allowed in" + domain.logShort());
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(TwinClassFieldSearchEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

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
