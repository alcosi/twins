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
import org.cambium.featurer.dao.FeaturerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.twinclass.*;
import org.twins.core.domain.search.FieldProjectionSearch;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.projection.ProjectionFieldSelector;
import org.twins.core.enums.sort.TwinClassFieldGroupField;
import org.twins.core.enums.sort.TwinClassFieldSortField;
import org.twins.core.featurer.classfield.finder.FieldFinder;
import org.twins.core.featurer.classfield.sorter.FieldSorter;
import org.twins.core.service.EntitySearchService;
import org.twins.core.service.SystemEntityService;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.cambium.common.util.SetUtils.narrowSet;
import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.CommonSpecification.toSortSpecification;
import static org.twins.core.dao.specifications.twinclass.TwinClassFieldSpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassFieldSearchService extends EntitySearchService
        <TwinClassFieldSearch, TwinClassFieldEntity, TwinClassFieldSortField, TwinClassFieldGroupField> {

    private final TwinClassFieldRepository twinClassFieldRepository;
    private final TwinClassFieldSearchPredicateRepository fieldSearchPredicateRepository;
    private final TwinClassFieldSearchConfigService twinClassFieldSearchConfigService;
    private final TwinClassService twinClassService;
    private final FeaturerService featurerService;

    @Override
    public JpaSpecificationExecutor<TwinClassFieldEntity> jpaSpecificationExecutor() {
        return twinClassFieldRepository;
    }

    @Override
    public TwinClassFieldSearch emptySearch() {
        return new TwinClassFieldSearch().setExcludeSystemFields(false);
    }

    @Override
    protected TwinClassFieldEntity newEntity() {
        return new TwinClassFieldEntity();
    }

    @Override
    protected Class<TwinClassFieldEntity> entityClass() {
        return TwinClassFieldEntity.class;
    }

    @Override
    public Specification<TwinClassFieldEntity> createFilterSpecification(TwinClassFieldSearch search, UUID domainId) {
        Locale locale;
        try {
            locale = authService.getApiUser().getLocale();
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
        Collection<TwinClassService.ClassWithExtends> twinClassIdMapResolved;
        Collection<TwinClassService.ClassWithExtends> twinClassIdExcludeMapResolved;
        try {
            twinClassIdMapResolved = twinClassService.loadExtends(search.getTwinClassIdMap());
            twinClassIdExcludeMapResolved = twinClassService.loadExtends(search.getTwinClassIdExcludeMap());
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
        return Specification.allOf(
                checkUuid(domainId, false, !search.isExcludeSystemFields(), TwinClassFieldEntity.Fields.twinClass, TwinClassEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, TwinClassFieldEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinClassFieldEntity.Fields.id),
                checkTwinClassAndInheritable(twinClassIdMapResolved, false, TwinClassFieldEntity.Fields.twinClassId, TwinClassFieldEntity.Fields.inheritable),
                checkTwinClassAndInheritable(twinClassIdExcludeMapResolved, true, TwinClassFieldEntity.Fields.twinClassId, TwinClassFieldEntity.Fields.inheritable),
                checkFieldLikeIn(search.getKeyLikeList(), false, true, TwinClassFieldEntity.Fields.key),
                checkFieldLikeIn(search.getKeyNotLikeList(), true, true, TwinClassFieldEntity.Fields.key),
                joinAndSearchByI18NField(TwinClassFieldEntity.Fields.nameI18n, search.getNameI18nLikeList(), locale, true, false),
                joinAndSearchByI18NField(TwinClassFieldEntity.Fields.nameI18n, search.getNameI18nNotLikeList(), locale, true, true),
                joinAndSearchByI18NField(TwinClassFieldEntity.Fields.descriptionI18n, search.getDescriptionI18nLikeList(), locale, true, false),
                joinAndSearchByI18NField(TwinClassFieldEntity.Fields.descriptionI18n, search.getDescriptionI18nNotLikeList(), locale, true, true),
                checkIntegerIn(search.getFieldTyperIdList(), false, TwinClassFieldEntity.Fields.fieldTyperFeaturerId),
                checkIntegerIn(search.getFieldTyperIdExcludeList(), true, TwinClassFieldEntity.Fields.fieldTyperFeaturerId),
                checkIntegerIn(search.getFieldInitiatorIdList(), false, TwinClassFieldEntity.Fields.fieldInitializerFeaturerId),
                checkIntegerIn(search.getFieldInitiatorIdExcludeList(), true, TwinClassFieldEntity.Fields.fieldInitializerFeaturerId),
                checkIntegerIn(search.getTwinSorterIdList(), false, TwinClassFieldEntity.Fields.twinSorterFeaturerId),
                checkIntegerIn(search.getTwinSorterIdExcludeList(), true, TwinClassFieldEntity.Fields.twinSorterFeaturerId),
                checkUuidIn(search.getViewPermissionIdList(), false, false, TwinClassFieldEntity.Fields.viewPermissionId),
                checkUuidIn(search.getViewPermissionIdExcludeList(), true, true, TwinClassFieldEntity.Fields.viewPermissionId),
                checkUuidIn(search.getEditPermissionIdList(), false, false, TwinClassFieldEntity.Fields.editPermissionId),
                checkUuidIn(search.getEditPermissionIdExcludeList(), true, true, TwinClassFieldEntity.Fields.editPermissionId),
                checkTernary(search.getRequired(), TwinClassFieldEntity.Fields.required),
                checkTernary(search.getSystem(), TwinClassFieldEntity.Fields.system),
                checkTernary(search.getInheritable(), TwinClassFieldEntity.Fields.inheritable),
                checkTernary(search.getDependentField(), TwinClassFieldEntity.Fields.dependentField),
                checkTernary(search.getHasDependentFields(), TwinClassFieldEntity.Fields.hasDependentFields),
                checkTernary(search.getProjectionField(), TwinClassFieldEntity.Fields.projectionField),
                checkTernary(search.getHasProjectionFields(), TwinClassFieldEntity.Fields.hasProjectedFields),
                checkFieldLikeIn(search.getExternalIdLikeList(), false, true, TwinClassFieldEntity.Fields.externalId),
                checkFieldLikeIn(search.getExternalIdNotLikeList(), true, true, TwinClassFieldEntity.Fields.externalId),
                checkFieldLongRange(search.getOrderRange(), TwinClassFieldEntity.Fields.order),
                checkProjections(search)
        );
    }

    @Override
    public Specification<TwinClassFieldEntity> createSortSpecification(TwinClassFieldSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        if (sortField == null)
            sortField = TwinClassFieldSortField.order;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case order -> toSortSpecification(ascending, TwinClassFieldEntity.Fields.order);
            case key -> toSortSpecification(ascending, TwinClassFieldEntity.Fields.key);
            case name -> org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecification(ascending, locale, TwinClassFieldEntity.Fields.nameI18n);
            case description -> org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecification(ascending, locale, TwinClassFieldEntity.Fields.descriptionI18n);
            case externalId -> toSortSpecification(ascending, TwinClassFieldEntity.Fields.externalId);
            case required -> toSortSpecification(ascending, TwinClassFieldEntity.Fields.required);
            case inheritable -> toSortSpecification(ascending, TwinClassFieldEntity.Fields.inheritable);
            case system -> toSortSpecification(ascending, TwinClassFieldEntity.Fields.system);
            case dependentField -> toSortSpecification(ascending, TwinClassFieldEntity.Fields.dependentField);
            case hasDependentFields -> toSortSpecification(ascending, TwinClassFieldEntity.Fields.hasDependentFields);
            case projectionField -> toSortSpecification(ascending, TwinClassFieldEntity.Fields.projectionField);
            case hasProjectedFields -> toSortSpecification(ascending, TwinClassFieldEntity.Fields.hasProjectedFields);
            case twinClassName -> org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecification(ascending, locale, TwinClassFieldEntity.Fields.twinClass, TwinClassEntity.Fields.nameI18nSpecOnly);
            case fieldTyperFeaturerName -> toSortSpecification(ascending, TwinClassFieldEntity.Fields.fieldTyperFeaturerSpecOnly, FeaturerEntity.Fields.name);
            case fieldInitializerFeaturerName -> toSortSpecification(ascending, TwinClassFieldEntity.Fields.fieldInitializerFeaturerSpecOnly, FeaturerEntity.Fields.name);
            case twinSorterFeaturerName -> toSortSpecification(ascending, TwinClassFieldEntity.Fields.twinSorterFeaturerSpecOnly, FeaturerEntity.Fields.name);
            case viewPermissionName -> org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecification(ascending, locale, TwinClassFieldEntity.Fields.viewPermission, PermissionEntity.Fields.nameI18n);
            case editPermissionName -> org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecification(ascending, locale, TwinClassFieldEntity.Fields.editPermission, PermissionEntity.Fields.nameI18n);
        };
    }

    @Override
    public String convertToEntityField(TwinClassFieldGroupField groupField) {
        return switch (groupField) {
            case required -> TwinClassFieldEntity.Fields.required;
            case inheritable -> TwinClassFieldEntity.Fields.inheritable;
            case system -> TwinClassFieldEntity.Fields.system;
            case dependentField -> TwinClassFieldEntity.Fields.dependentField;
            case hasDependentFields -> TwinClassFieldEntity.Fields.hasDependentFields;
            case projectionField -> TwinClassFieldEntity.Fields.projectionField;
            case hasProjectedFields -> TwinClassFieldEntity.Fields.hasProjectedFields;
            case twinClassId -> TwinClassFieldEntity.Fields.twinClassId;
            case fieldTyperFeaturerId -> TwinClassFieldEntity.Fields.fieldTyperFeaturerId;
            case twinSorterFeaturerId -> TwinClassFieldEntity.Fields.twinSorterFeaturerId;
            case fieldInitializerFeaturerId -> TwinClassFieldEntity.Fields.fieldInitializerFeaturerId;
            case viewPermissionId -> TwinClassFieldEntity.Fields.viewPermissionId;
            case editPermissionId -> TwinClassFieldEntity.Fields.editPermissionId;
        };
    }

    @Override
    public void mapGroupedField(TwinClassFieldEntity entity, TwinClassFieldGroupField field, Object o) {
        switch (field) {
            case required -> entity.setRequired((Boolean) o);
            case inheritable -> entity.setInheritable((Boolean) o);
            case system -> entity.setSystem((Boolean) o);
            case dependentField -> entity.setDependentField((Boolean) o);
            case hasDependentFields -> entity.setHasDependentFields((Boolean) o);
            case projectionField -> entity.setProjectionField((Boolean) o);
            case hasProjectedFields -> entity.setHasProjectedFields((Boolean) o);
            case twinClassId -> entity.setTwinClassId((UUID) o);
            case fieldTyperFeaturerId -> entity.setFieldTyperFeaturerId((Integer) o);
            case twinSorterFeaturerId -> entity.setTwinSorterFeaturerId((Integer) o);
            case fieldInitializerFeaturerId -> entity.setFieldInitializerFeaturerId((Integer) o);
            case viewPermissionId -> entity.setViewPermissionId((UUID) o);
            case editPermissionId -> entity.setEditPermissionId((UUID) o);
        }
    }

    public PaginationResult<TwinClassFieldEntity> findTwinClassField(TwinClassFieldSearch search, SimplePagination pagination) throws ServiceException {
        if (search.isInactiveSearch())
            return PaginationResult.EMPTY;
        return search(search, pagination);
    }

    public List<TwinClassFieldEntity> findTwinClassField(TwinClassFieldSearch search) throws ServiceException {
        if (search.isInactiveSearch())
            return Collections.emptyList();
        return twinClassFieldRepository.findAll(createFilterSpecification(search, authService.getApiUser().getDomainId()));
    }

    public PaginationResult<TwinClassFieldEntity> findTwinClassField(UUID searchId, Map<String, String> namedParamsMap, TwinClassFieldSearch narrowSearch, SimplePagination pagination) throws ServiceException {
        if (SystemEntityService.TWIN_CLASS_FIELD_SEARCH_UNLIMITED.equals(searchId)) {
            return findTwinClassField(narrowSearch, pagination);
        }
        TwinClassFieldSearchEntity searchEntity = twinClassFieldSearchConfigService.findEntitySafe(searchId);
        List<TwinClassFieldSearchPredicateEntity> searchPredicates = fieldSearchPredicateRepository.findByTwinClassFieldSearchId(searchEntity.getId());
        TwinClassFieldSearch mainSearch = new TwinClassFieldSearch()
                .setExcludeSystemFields(false);
        for (TwinClassFieldSearchPredicateEntity predicate : searchPredicates) {
            FieldFinder fieldFinder = featurerService.getFeaturer(predicate.getFieldFinderFeaturerId(), FieldFinder.class);
            fieldFinder.concatSearch(predicate.getFieldFinderParams(), mainSearch, namedParamsMap);
        }
        narrowSearch(mainSearch, narrowSearch);
        mainSearch.setConfiguredSearch(searchEntity);
        // Apply FieldSorter for configured search
        if (searchEntity.isForceSorting() || pagination.getSort() == null) {
            FieldSorter fieldSorter = featurerService.getFeaturer(searchEntity.getFieldSorterFeaturerId(), FieldSorter.class);
            var sortFunction = fieldSorter.createSort(searchEntity.getFieldSorterParams());
            if (sortFunction != null) {
                Specification<TwinClassFieldEntity> spec = createFilterSpecification(mainSearch, authService.getApiUser().getDomainId());
                spec = sortFunction.apply(spec);
                pagination.setSort(null);
                Page<TwinClassFieldEntity> page = twinClassFieldRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
                return PaginationUtils.convertInPaginationResult(page, pagination);
            }
        }
        return findTwinClassField(mainSearch, pagination);
    }

    private Specification<TwinClassFieldEntity> checkProjections(TwinClassFieldSearch search) {
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
