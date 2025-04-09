package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.service.auth.AuthService;

import java.util.HashSet;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NField;


import static org.twins.core.dao.specifications.twinclass.TwinClassFieldSpecification.*;
import static org.twins.core.service.SystemEntityService.getSystemFieldsIds;


@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassFieldSearchService {
    private final AuthService authService;
    private final TwinClassFieldRepository twinClassFieldRepository;
    private final TwinClassService twinClassService;

    public PaginationResult<TwinClassFieldEntity> findTwinClassField(TwinClassFieldSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinClassFieldEntity> spec = createTwinClassFieldSearchSpecification(search);
        Page<TwinClassFieldEntity> ret = twinClassFieldRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinClassFieldEntity> createTwinClassFieldSearchSpecification(TwinClassFieldSearch search) throws ServiceException {
        excludeSystemFields(search);
        ApiUser apiUser = authService.getApiUser();
        return Specification.allOf(
                checkFieldUuid(apiUser.getDomainId(), TwinClassFieldEntity.Fields.twinClass, TwinClassEntity.Fields.domainId),
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
                checkFieldTyperIdIn(search.getFieldTyperIdList(), false, false),
                checkFieldTyperIdIn(search.getFieldTyperIdExcludeList(), true, true),
                checkUuidIn(search.getViewPermissionIdList(), false, false, TwinClassFieldEntity.Fields.viewPermissionId),
                checkUuidIn(search.getViewPermissionIdExcludeList(), true, true, TwinClassFieldEntity.Fields.viewPermissionId),
                checkUuidIn(search.getViewPermissionIdList(), false, false, TwinClassFieldEntity.Fields.editPermissionId),
                checkUuidIn(search.getViewPermissionIdExcludeList(), true, true, TwinClassFieldEntity.Fields.editPermissionId),
                checkTernary(search.getRequired()));
    }

    private void excludeSystemFields(TwinClassFieldSearch search) {
        if (search.getIdExcludeList() == null) {
            search.setIdExcludeList(new HashSet<>(getSystemFieldsIds()));
        } else {
            search.getIdExcludeList().addAll(getSystemFieldsIds());
        }
    }

}
