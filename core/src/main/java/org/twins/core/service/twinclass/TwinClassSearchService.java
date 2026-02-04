package org.twins.core.service.twinclass;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.cambium.common.util.Ternary;
import org.cambium.common.util.TernaryUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.twinclass.*;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.domain.search.HierarchySearch;
import org.twins.core.domain.search.TwinClassSearch;
import org.twins.core.featurer.classfinder.ClassFinder;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.cambium.common.util.SetUtils.narrowSet;
import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.twinclass.TwinClassSpecification.*;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassSearchService extends EntitySecureFindServiceImpl<TwinClassSearchEntity> {
    private final TwinClassRepository twinClassRepository;
    private final TwinClassSearchRepository classSearchRepository;
    private final TwinClassSearchPredicateRepository classSearchPredicateRepository;

    private final AuthService authService;
    private final FeaturerService featurerService;

    public PaginationResult<TwinClassEntity> findTwinClasses(TwinClassSearch twinClassSearch, SimplePagination pagination) throws ServiceException {
        if (twinClassSearch == null)
            twinClassSearch = new TwinClassSearch(); //no filters
        Page<TwinClassEntity> twinClassList = twinClassRepository.findAll(createTwinClassEntitySearchSpecification(twinClassSearch), PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(twinClassList, pagination);
    }

    public PaginationResult<TwinClassEntity> findTwinClasses(UUID searchId, TwinClassSearch narrowSearch, SimplePagination pagination) throws ServiceException {
        if (SystemEntityService.TWIN_CLASS_SEARCH_UNLIMITED.equals(searchId)) {
            return findTwinClasses(narrowSearch, pagination);
        }
        TwinClassSearchEntity searchEntity = findEntitySafe(searchId);
        List<TwinClassSearchPredicateEntity> searchPredicates = classSearchPredicateRepository.findByTwinClassSearchId(searchEntity.getId());
        TwinClassSearch mainSearch = new TwinClassSearch();
        for (TwinClassSearchPredicateEntity predicate : searchPredicates) {
            ClassFinder classFinder = featurerService.getFeaturer(predicate.getClassFinderFeaturerId(), ClassFinder.class);
            classFinder.concatSearch(predicate.getClassFinderParams(), mainSearch);
        }
        narrowSearch(mainSearch, narrowSearch);
        return findTwinClasses(mainSearch, pagination);
    }

    public List<TwinClassEntity> searchTwinClasses(TwinClassSearch twinClassSearch) throws ServiceException {
        if (twinClassSearch == null)
            twinClassSearch = new TwinClassSearch(); //no filters
        return twinClassRepository.findAll(createTwinClassEntitySearchSpecification(twinClassSearch));
    }

    public Specification<TwinClassEntity> createTwinClassEntitySearchSpecification(TwinClassSearch twinClassSearch) throws ServiceException {
        Locale locale = authService.getApiUser().getLocale();
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
                        .and(checkUuid(authService.getApiUser().getDomainId(), false, false, TwinClassEntity.Fields.domainId))
                        .and(checkOwnerTypeIn(twinClassSearch.getOwnerTypeExcludeList(), true))
                        .and(checkUuidIn(twinClassSearch.getTwinClassIdList(), false, false, TwinClassEntity.Fields.id))
                        .and(checkUuidIn(twinClassSearch.getTwinClassIdExcludeList(), true, false, TwinClassEntity.Fields.id))
                        .and(checkFieldLikeIn(twinClassSearch.getTwinClassKeyLikeList(), false, true, TwinClassEntity.Fields.key))
                        .and(joinAndSearchByI18NField(TwinflowEntity.Fields.nameI18n, twinClassSearch.getNameI18nLikeList(), locale, false, false))
                        .and(joinAndSearchByI18NField(TwinflowEntity.Fields.nameI18n, twinClassSearch.getNameI18nNotLikeList(), locale, true, true))
                        .and(joinAndSearchByI18NField(TwinflowEntity.Fields.descriptionI18n, twinClassSearch.getDescriptionI18nLikeList(), locale, false, false))
                        .and(joinAndSearchByI18NField(TwinflowEntity.Fields.descriptionI18n, twinClassSearch.getDescriptionI18nNotLikeList(), locale, true, true))
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
                        .and(checkUuidIn(twinClassSearch.getEditPermissionIdList(), false, false, TwinClassEntity.Fields.editPermissionId))
                        .and(checkUuidIn(twinClassSearch.getEditPermissionIdExcludeList(), true, false, TwinClassEntity.Fields.editPermissionId))
                        .and(checkUuidIn(twinClassSearch.getDeletePermissionIdList(), false, false, TwinClassEntity.Fields.deletePermissionId))
                        .and(checkUuidIn(twinClassSearch.getDeletePermissionIdExcludeList(), true, false, TwinClassEntity.Fields.deletePermissionId));
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
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = entity.getDomainId() != null && !entity.getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.logNormal() + " is not allowed in" + domain.logShort());
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(TwinClassSearchEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
