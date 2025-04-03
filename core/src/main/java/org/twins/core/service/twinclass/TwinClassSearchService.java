package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.domain.search.HierarchySearch;
import org.twins.core.domain.search.TwinClassSearch;
import org.twins.core.service.auth.AuthService;

import java.util.List;
import java.util.Locale;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.springframework.data.jpa.domain.Specification.where;
import static org.twins.core.dao.specifications.twinclass.TwinClassSpecification.*;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class TwinClassSearchService {
    private final TwinClassRepository twinClassRepository;
    private final AuthService authService;

    public PaginationResult<TwinClassEntity> findTwinClasses(TwinClassSearch twinClassSearch, SimplePagination pagination) throws ServiceException {
        if (twinClassSearch == null)
            twinClassSearch = new TwinClassSearch(); //no filters
        Page<TwinClassEntity> twinClassList = twinClassRepository.findAll(createTwinClassEntitySearchSpecification(twinClassSearch), PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(twinClassList, pagination);
    }

    public List<TwinClassEntity> searchTwinClasses(TwinClassSearch twinClassSearch) throws ServiceException {
        if (twinClassSearch == null)
            twinClassSearch = new TwinClassSearch(); //no filters
        return twinClassRepository.findAll(createTwinClassEntitySearchSpecification(twinClassSearch));
    }

    public Specification<TwinClassEntity> createTwinClassEntitySearchSpecification(TwinClassSearch twinClassSearch) throws ServiceException {
        Locale locale = authService.getApiUser().getLocale();
        HierarchySearch headHierarchyChildsForTwinClassSearch = twinClassSearch.getHeadHierarchyChildsForTwinClassSearch();
        HierarchySearch headHierarchyParentsForTwinClassSearch = twinClassSearch.getHeadHierarchyParentsForTwinClassSearch();
        HierarchySearch extendsHierarchyChildsForTwinClassSearch = twinClassSearch.getExtendsHierarchyChildsForTwinClassSearch();
        HierarchySearch extendsHierarchyParentsForTwinClassSearch = twinClassSearch.getExtendsHierarchyParentsForTwinClassSearch();

        return where(
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

                        .and(checkHeadTwinClassChilds(headHierarchyChildsForTwinClassSearch.getIdList(), false, false, headHierarchyChildsForTwinClassSearch.getDepth()))
                        .and(checkHeadTwinClassChilds(headHierarchyChildsForTwinClassSearch.getIdExcludeList(), true, true, headHierarchyChildsForTwinClassSearch.getDepth()))
                        .and(checkHeadTwinClassParents(headHierarchyParentsForTwinClassSearch.getIdList(), false, false, headHierarchyParentsForTwinClassSearch.getDepth()))
                        .and(checkHeadTwinClassParents(headHierarchyParentsForTwinClassSearch.getIdExcludeList(), true, true, headHierarchyParentsForTwinClassSearch.getDepth()))

                        .and(checkExtendsTwinClassChilds(extendsHierarchyChildsForTwinClassSearch.getIdList(), false, false, extendsHierarchyChildsForTwinClassSearch.getDepth()))
                        .and(checkExtendsTwinClassChilds(extendsHierarchyChildsForTwinClassSearch.getIdExcludeList(), true, true, extendsHierarchyChildsForTwinClassSearch.getDepth()))
                        .and(checkExtendsTwinClassParents(extendsHierarchyParentsForTwinClassSearch.getIdList(), false, false, extendsHierarchyParentsForTwinClassSearch.getDepth()))
                        .and(checkExtendsTwinClassParents(extendsHierarchyParentsForTwinClassSearch.getIdExcludeList(), true, true, extendsHierarchyParentsForTwinClassSearch.getDepth()))


                        .and(checkUuidIn(twinClassSearch.getMarkerDatalistIdList(), false, false, TwinClassEntity.Fields.markerDataListId))
                        .and(checkUuidIn(twinClassSearch.getMarkerDatalistIdExcludeList(), true, false, TwinClassEntity.Fields.markerDataListId))
                        .and(checkUuidIn(twinClassSearch.getTagDatalistIdList(), false, false, TwinClassEntity.Fields.tagDataListId))
                        .and(checkUuidIn(twinClassSearch.getTagDatalistIdExcludeList(), true, false, TwinClassEntity.Fields.tagDataListId))
                        .and(checkTernary(twinClassSearch.getAbstractt(), TwinClassEntity.Fields.abstractt))
                        .and(checkTernary(twinClassSearch.getPermissionSchemaSpace(), TwinClassEntity.Fields.permissionSchemaSpace))
                        .and(checkTernary(twinClassSearch.getTwinflowSchemaSpace(), TwinClassEntity.Fields.twinflowSchemaSpace))
                        .and(checkTernary(twinClassSearch.getTwinClassSchemaSpace(), TwinClassEntity.Fields.twinClassSchemaSpace))
                        .and(checkTernary(twinClassSearch.getAliasSpace(), TwinClassEntity.Fields.aliasSpace))
                        .and(checkTernary(twinClassSearch.getAssigneeRequired(), TwinClassEntity.Fields.assigneeRequired))
                        .and(checkUuidIn(twinClassSearch.getViewPermissionIdList(), false, false, TwinClassEntity.Fields.viewPermissionId))
                        .and(checkUuidIn(twinClassSearch.getViewPermissionIdExcludeList(), true, false, TwinClassEntity.Fields.viewPermissionId))
                        .and(checkUuidIn(twinClassSearch.getCreatePermissionIdList(), false, false, TwinClassEntity.Fields.createPermissionId))
                        .and(checkUuidIn(twinClassSearch.getCreatePermissionIdExcludeList(), true, false, TwinClassEntity.Fields.createPermissionId))
                        .and(checkUuidIn(twinClassSearch.getEditPermissionIdList(), false, false, TwinClassEntity.Fields.editPermissionId))
                        .and(checkUuidIn(twinClassSearch.getEditPermissionIdExcludeList(), true, false, TwinClassEntity.Fields.editPermissionId))
                        .and(checkUuidIn(twinClassSearch.getDeletePermissionIdList(), false, false, TwinClassEntity.Fields.deletePermissionId))
                        .and(checkUuidIn(twinClassSearch.getDeletePermissionIdExcludeList(), true, false, TwinClassEntity.Fields.deletePermissionId))
        );
    }
}
