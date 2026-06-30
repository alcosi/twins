package org.twins.core.service.twinclass;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.Ternary;
import org.cambium.common.util.TernaryUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.i18n.specifications.I18nSpecification;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.twinclass.*;
import org.twins.core.domain.search.HierarchySearch;
import org.twins.core.domain.search.TwinClassSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.enums.sort.TwinClassGroupField;
import org.twins.core.enums.sort.TwinClassSortField;
import org.twins.core.featurer.classfinder.ClassFinder;
import org.twins.core.service.EntitySearchService;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.cambium.common.util.SetUtils.narrowSet;
import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NFieldDirect;
import static org.twins.core.dao.specifications.CommonSpecification.toSortSpecification;
import static org.twins.core.dao.specifications.twinclass.TwinClassSpecification.*;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassSearchService extends EntitySearchService
        <TwinClassSearch, TwinClassEntity, TwinClassSortField, TwinClassGroupField> {
    private final TwinClassRepository twinClassRepository;
    private final TwinClassSearchPredicateRepository classSearchPredicateRepository;
    private final TwinClassSearchConfigService twinClassSearchConfigService;
    private final FeaturerService featurerService;

    @Override
    public JpaSpecificationExecutor<TwinClassEntity> jpaSpecificationExecutor() {
        return twinClassRepository;
    }

    @Override
    public TwinClassSearch emptySearch() {
        return new TwinClassSearch();
    }

    @Override
    protected TwinClassEntity newEntity() {
        return new TwinClassEntity();
    }

    @Override
    protected Class<TwinClassEntity> entityClass() {
        return TwinClassEntity.class;
    }

    @Override
    public Specification<TwinClassEntity> createFilterSpecification(TwinClassSearch twinClassSearch, UUID domainId, Locale locale) {
        HierarchySearch headHierarchyChildrenForTwinClassSearch =
                java.util.Objects.requireNonNullElse(twinClassSearch.getHeadHierarchyChildsForTwinClassSearch(), HierarchySearch.EMPTY);
        HierarchySearch headHierarchyParentsForTwinClassSearch =
                java.util.Objects.requireNonNullElse(twinClassSearch.getHeadHierarchyParentsForTwinClassSearch(), HierarchySearch.EMPTY);
        HierarchySearch extendsHierarchyChildsForTwinClassSearch =
                java.util.Objects.requireNonNullElse(twinClassSearch.getExtendsHierarchyChildsForTwinClassSearch(), HierarchySearch.EMPTY);
        HierarchySearch extendsHierarchyParentsForTwinClassSearch =
                java.util.Objects.requireNonNullElse(twinClassSearch.getExtendsHierarchyParentsForTwinClassSearch(), HierarchySearch.EMPTY);

        return
                checkOwnerTypeIn(twinClassSearch.getOwnerTypeList(), false)
                        .and(checkUuid(domainId, false, false, TwinClassEntity.Fields.domainId))
                        .and(checkOwnerTypeIn(twinClassSearch.getOwnerTypeExcludeList(), true))
                        .and(checkUuidIn(twinClassSearch.getTwinClassIdList(), false, false, TwinClassEntity.Fields.id))
                        .and(checkUuidIn(twinClassSearch.getTwinClassIdExcludeList(), true, false, TwinClassEntity.Fields.id))
                        .and(checkFieldLikeIn(twinClassSearch.getTwinClassKeyLikeList(), false, true, TwinClassEntity.Fields.key))
                        .and(joinAndSearchByI18NFieldDirect(TwinClassEntity.Fields.nameI18nTranslationsSpecOnly, twinClassSearch.getNameI18nLikeList(), locale, false, false))
                        .and(joinAndSearchByI18NFieldDirect(TwinClassEntity.Fields.nameI18nTranslationsSpecOnly, twinClassSearch.getNameI18nNotLikeList(), locale, true, true))
                        .and(joinAndSearchByI18NFieldDirect(TwinClassEntity.Fields.descriptionI18nTranslationsSpecOnly, twinClassSearch.getDescriptionI18nLikeList(), locale, false, false))
                        .and(joinAndSearchByI18NFieldDirect(TwinClassEntity.Fields.descriptionI18nTranslationsSpecOnly, twinClassSearch.getDescriptionI18nNotLikeList(), locale, true, true))
                        .and(checkFieldLikeIn(twinClassSearch.getExternalIdLikeList(), false, true, TwinClassEntity.Fields.externalId))
                        .and(checkFieldLikeIn(twinClassSearch.getExternalIdNotLikeList(), true, true, TwinClassEntity.Fields.externalId))

                        .and(checkHeadTwinClassChildren(headHierarchyChildrenForTwinClassSearch.getIdList(), false, headHierarchyChildrenForTwinClassSearch.getDepth()))
                        .and(checkHeadTwinClassChildren(headHierarchyChildrenForTwinClassSearch.getIdExcludeList(), true, headHierarchyChildrenForTwinClassSearch.getDepth()))
                        .and(checkHeadTwinClassParents(headHierarchyParentsForTwinClassSearch.getIdList(), false, false, headHierarchyParentsForTwinClassSearch.getDepth()))
                        .and(checkHeadTwinClassParents(headHierarchyParentsForTwinClassSearch.getIdExcludeList(), true, true, headHierarchyParentsForTwinClassSearch.getDepth()))

                        .and(checkExtendsTwinClassChildren(extendsHierarchyChildsForTwinClassSearch.getIdList(), false, extendsHierarchyChildsForTwinClassSearch.getDepth()))
                        .and(checkExtendsTwinClassChildren(extendsHierarchyChildsForTwinClassSearch.getIdExcludeList(), true, extendsHierarchyChildsForTwinClassSearch.getDepth()))
                        .and(checkExtendsTwinClassParents(extendsHierarchyParentsForTwinClassSearch.getIdList(), false, false, extendsHierarchyParentsForTwinClassSearch.getDepth()))
                        .and(checkExtendsTwinClassParents(extendsHierarchyParentsForTwinClassSearch.getIdExcludeList(), true, true, extendsHierarchyParentsForTwinClassSearch.getDepth()))

                        .and(checkUuidIn(twinClassSearch.getMarkerDatalistIdList(), false, false, TwinClassEntity.Fields.markerDataListId))
                        .and(checkUuidIn(twinClassSearch.getMarkerDatalistIdExcludeList(), true, false, TwinClassEntity.Fields.markerDataListId))
                        .and(checkUuidIn(twinClassSearch.getTagDatalistIdList(), false, false, TwinClassEntity.Fields.tagDataListId))
                        .and(checkUuidIn(twinClassSearch.getTagDatalistIdExcludeList(), true, false, TwinClassEntity.Fields.tagDataListId))
                        .and(checkUuidIn(twinClassSearch.getFreezeIdList(), false, false, TwinClassEntity.Fields.twinClassFreezeId))
                        .and(checkUuidIn(twinClassSearch.getFreezeIdExcludeList(), true, false, TwinClassEntity.Fields.twinClassFreezeId))
                        .and(checkTernary(twinClassSearch.getAbstractt(), TwinClassEntity.Fields.abstractt))
                        .and(checkTernary(twinClassSearch.getPermissionSchemaSpace(), TwinClassEntity.Fields.permissionSchemaSpace))
                        .and(checkTernary(twinClassSearch.getTwinflowSchemaSpace(), TwinClassEntity.Fields.twinflowSchemaSpace))
                        .and(checkTernary(twinClassSearch.getTwinClassSchemaSpace(), TwinClassEntity.Fields.twinClassSchemaSpace))
                        .and(checkTernary(twinClassSearch.getAliasSpace(), TwinClassEntity.Fields.aliasSpace))
                        .and(checkTernary(twinClassSearch.getAssigneeRequired(), TwinClassEntity.Fields.assigneeRequired))
                        .and(checkTernary(twinClassSearch.getSegment(), TwinClassEntity.Fields.segment))
                        .and(checkTernary(twinClassSearch.getHasSegments(), TwinClassEntity.Fields.hasSegment))
                        .and(checkTernary(twinClassSearch.getUniqueName(), TwinClassEntity.Fields.uniqueName))
                        .and(checkUuidIn(twinClassSearch.getViewPermissionIdList(), false, false, TwinClassEntity.Fields.viewPermissionId))
                        .and(checkUuidIn(twinClassSearch.getViewPermissionIdExcludeList(), true, false, TwinClassEntity.Fields.viewPermissionId))
                        .and(checkUuidIn(twinClassSearch.getCreatePermissionIdList(), false, false, TwinClassEntity.Fields.createPermissionId))
                        .and(checkUuidIn(twinClassSearch.getCreatePermissionIdExcludeList(), true, false, TwinClassEntity.Fields.createPermissionId))
                        .and(checkFieldIntegerRange(twinClassSearch.getTwinCounterRange(), TwinClassEntity.Fields.twinCounter))
                        .and(checkIntegerIn(twinClassSearch.getHeadHunterFeaturerIdList(), false, TwinClassEntity.Fields.headHunterFeaturerId))
                        .and(checkTernary(twinClassSearch.getHasDynamicMarkers(), TwinClassEntity.Fields.hasDynamicMarkers))
                        .and(checkUuidIn(twinClassSearch.getBreadCrumbsFaceIdList(), false, false, TwinClassEntity.Fields.breadCrumbsFaceId))
                        .and(checkUuidIn(twinClassSearch.getBreadCrumbsFaceIdExcludeList(), true, false, TwinClassEntity.Fields.breadCrumbsFaceId))
                        .and(checkUuidIn(twinClassSearch.getPageFaceIdList(), false, false, TwinClassEntity.Fields.pageFaceId))
                        .and(checkUuidIn(twinClassSearch.getPageFaceIdExcludeList(), true, false, TwinClassEntity.Fields.pageFaceId));
    }

    @Override
    public Specification<TwinClassEntity> createSortSpecification(TwinClassSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        if (sortField == null)
            sortField = TwinClassSortField.key;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case key -> toSortSpecification(ascending, TwinClassEntity.Fields.key);
            case name -> I18nSpecification.toSortSpecificationDirect(ascending, locale, TwinClassEntity.Fields.nameI18nTranslationsSpecOnly);
            case description -> I18nSpecification.toSortSpecificationDirect(ascending, locale, TwinClassEntity.Fields.descriptionI18nTranslationsSpecOnly);
            case createdAt -> toSortSpecification(ascending, TwinClassEntity.Fields.createdAt);
            case externalId -> toSortSpecification(ascending, TwinClassEntity.Fields.externalId);
            case ownerType -> toSortSpecification(ascending, TwinClassEntity.Fields.ownerType);
            case twinCounter -> toSortSpecification(ascending, TwinClassEntity.Fields.twinCounter);
            case abstractt -> toSortSpecification(ascending, TwinClassEntity.Fields.abstractt);
            case segment -> toSortSpecification(ascending, TwinClassEntity.Fields.segment);
            case uniqueName -> toSortSpecification(ascending, TwinClassEntity.Fields.uniqueName);
            case headTwinClassName -> I18nSpecification.toSortSpecificationDirect(ascending, locale, TwinClassEntity.Fields.headTwinClassSpecOnly, TwinClassEntity.Fields.nameI18nTranslationsSpecOnly);
            case headhunterFeaturerName -> toSortSpecification(ascending, TwinClassEntity.Fields.headHunterFeaturerSpecOnly, FeaturerEntity.Fields.name);
            case extendsTwinClassName -> I18nSpecification.toSortSpecificationDirect(ascending, locale,  TwinClassEntity.Fields.extendsTwinClassSpecOnly, TwinClassEntity.Fields.nameI18nTranslationsSpecOnly);
            case markerDataListName -> I18nSpecification.toSortSpecificationDirect(ascending, locale, TwinClassEntity.Fields.markerDataListSpecOnly, DataListEntity.Fields.nameI18nTranslationsSpecOnly);
            case tagDataListName -> I18nSpecification.toSortSpecificationDirect(ascending, locale, TwinClassEntity.Fields.tagDataListSpecOnly, DataListEntity.Fields.nameI18nTranslationsSpecOnly);
            case twinflowSchemaSpace -> toSortSpecification(ascending, TwinClassEntity.Fields.twinflowSchemaSpace);
            case twinClassSchemaSpace -> toSortSpecification(ascending, TwinClassEntity.Fields.twinClassSchemaSpace);
            case aliasSpace -> toSortSpecification(ascending, TwinClassEntity.Fields.aliasSpace);
            case viewPermissionName -> I18nSpecification.toSortSpecificationDirect(ascending, locale, TwinClassEntity.Fields.viewPermissionSpecOnly, PermissionEntity.Fields.nameI18nTranslationsSpecOnly);
            case assigneeRequired -> toSortSpecification(ascending, TwinClassEntity.Fields.assigneeRequired);
            case hasDynamicMarkers -> toSortSpecification(ascending, TwinClassEntity.Fields.hasDynamicMarkers);
            case breadCrumbsFaceName -> toSortSpecification(ascending, TwinClassEntity.Fields.breadCrumbsFaceSpecOnly, FaceEntity.Fields.name);
            case pageFaceName -> toSortSpecification(ascending, TwinClassEntity.Fields.pageFaceSpecOnly, FaceEntity.Fields.name);
            case createdByUserName -> toSortSpecification(ascending, TwinClassEntity.Fields.createdByUserSpecOnly, FaceEntity.Fields.name);
        };
    }

    @Override
    public String convertToEntityField(TwinClassGroupField groupField) {
        return switch (groupField) {
            case ownerType -> TwinClassEntity.Fields.ownerType;
            case abstractt -> TwinClassEntity.Fields.abstractt;
            case segment -> TwinClassEntity.Fields.segment;
            case twinClassFreezeId -> TwinClassEntity.Fields.twinClassFreezeId;
            case headTwinClassId -> TwinClassEntity.Fields.headTwinClassId;
            case extendsTwinClassId -> TwinClassEntity.Fields.extendsTwinClassId;
            case markerDataListId -> TwinClassEntity.Fields.markerDataListId;
            case tagDataListId -> TwinClassEntity.Fields.tagDataListId;
            case twinflowSchemaSpace -> TwinClassEntity.Fields.twinflowSchemaSpace;
            case twinClassSchemaSpace -> TwinClassEntity.Fields.twinClassSchemaSpace;
            case aliasSpace -> TwinClassEntity.Fields.aliasSpace;
            case viewPermissionId -> TwinClassEntity.Fields.viewPermissionId;
            case createPermissionId -> TwinClassEntity.Fields.createPermissionId;
            case headHunterFeaturerId -> TwinClassEntity.Fields.headHunterFeaturerId;
            case assigneeRequired -> TwinClassEntity.Fields.assigneeRequired;
            case uniqueName -> TwinClassEntity.Fields.uniqueName;
            case hasDynamicMarkers -> TwinClassEntity.Fields.hasDynamicMarkers;
            case breadCrumbsFaceId -> TwinClassEntity.Fields.breadCrumbsFaceId;
            case pageFaceId -> TwinClassEntity.Fields.pageFaceId;
            case createdByUserId -> TwinClassEntity.Fields.createdByUserId;
        };
    }

    @Override
    public void mapGroupedField(TwinClassEntity entity, TwinClassGroupField field, Object o) {
        switch (field) {
            case ownerType -> entity.setOwnerType((org.twins.core.enums.twinclass.OwnerType) o);
            case abstractt -> entity.setAbstractt((Boolean) o);
            case segment -> entity.setSegment((Boolean) o);
            case twinClassFreezeId -> entity.setTwinClassFreezeId((UUID) o);
            case headTwinClassId -> entity.setHeadTwinClassId((UUID) o);
            case extendsTwinClassId -> entity.setExtendsTwinClassId((UUID) o);
            case markerDataListId -> entity.setMarkerDataListId((UUID) o);
            case tagDataListId -> entity.setTagDataListId((UUID) o);
            case twinflowSchemaSpace -> entity.setTwinflowSchemaSpace((Boolean) o);
            case twinClassSchemaSpace -> entity.setTwinClassSchemaSpace((Boolean) o);
            case aliasSpace -> entity.setAliasSpace((Boolean) o);
            case viewPermissionId -> entity.setViewPermissionId((UUID) o);
            case createPermissionId -> entity.setCreatePermissionId((UUID) o);
            case headHunterFeaturerId -> entity.setHeadHunterFeaturerId((Integer) o);
            case assigneeRequired -> entity.setAssigneeRequired((Boolean) o);
            case uniqueName -> entity.setUniqueName((Boolean) o);
            case hasDynamicMarkers -> entity.setHasDynamicMarkers((Boolean) o);
            case breadCrumbsFaceId -> entity.setBreadCrumbsFaceId((UUID) o);
            case pageFaceId -> entity.setPageFaceId((UUID) o);
            case createdByUserId -> entity.setCreatedByUserId((UUID) o);
        }
    }

    public PaginationResult<TwinClassEntity> findTwinClasses(UUID searchId, TwinClassSearch narrowSearch, SimplePagination pagination) throws ServiceException {
        if (SystemIds.TwinClassSearch.UNLIMITED.equals(searchId)) {
            return search(narrowSearch, pagination);
        }
        TwinClassSearchEntity searchEntity = twinClassSearchConfigService.findEntitySafe(searchId);
        List<TwinClassSearchPredicateEntity> searchPredicates = classSearchPredicateRepository.findByTwinClassSearchId(searchEntity.getId());
        TwinClassSearch mainSearch = new TwinClassSearch();
        for (TwinClassSearchPredicateEntity predicate : searchPredicates) {
            ClassFinder classFinder = featurerService.getFeaturer(predicate.getClassFinderFeaturerId(), ClassFinder.class);
            classFinder.concatSearch(predicate.getClassFinderParams(), mainSearch);
        }
        narrowSearch(mainSearch, narrowSearch);
        return search(mainSearch, pagination);
    }

    public List<TwinClassEntity> searchTwinClasses(TwinClassSearch twinClassSearch) throws ServiceException {
        if (twinClassSearch == null)
            twinClassSearch = new TwinClassSearch();
        var filter = createFilterSpecification(twinClassSearch, authService.getApiUser().getDomainId(),authService.getApiUser().getLocale());
        return twinClassRepository.findAll(filter);
    }

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
