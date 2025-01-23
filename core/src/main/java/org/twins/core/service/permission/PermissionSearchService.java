package org.twins.core.service.permission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionRepository;
import org.twins.core.domain.search.PermissionSearch;
import org.twins.core.service.auth.AuthService;

import java.util.Locale;
import java.util.UUID;

import static org.cambium.i18n.dao.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.permission.PermissionSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionSearchService {
    private final AuthService authService;
    private final PermissionRepository permissionRepository;

    public PaginationResult<PermissionEntity> findPermissionForDomain(PermissionSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<PermissionEntity> spec = createPermissionSearchSpecification(search)
                .and(checkDomainId(domainId));
        Page<PermissionEntity> ret = permissionRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<PermissionEntity> createPermissionSearchSpecification(PermissionSearch search) throws ServiceException {
        Locale locale = authService.getApiUser().getLocale();
        return Specification.allOf(
                checkFieldLikeContainsIn(search.getKeyLikeList(), false, true, PermissionEntity.Fields.key),
                checkFieldLikeContainsIn(search.getKeyNotLikeList(), true, true, PermissionEntity.Fields.key),
                checkUuidIn(search.getIdList(), false, true, PermissionEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, PermissionEntity.Fields.id),
                joinAndSearchByI18NField(PermissionEntity.Fields.nameI18NId, search.getNameI18nLikeList(), locale, false, false),
                joinAndSearchByI18NField(PermissionEntity.Fields.nameI18NId, search.getNameI18nNotLikeList(), locale, true, true),
                joinAndSearchByI18NField(PermissionEntity.Fields.descriptionI18NId, search.getDescriptionI18nLikeList(), locale, false, false),
                joinAndSearchByI18NField(PermissionEntity.Fields.descriptionI18NId, search.getDescriptionI18nNotLikeList(), locale, true, true),
                checkUuidIn(search.getGroupIdList(), false, true, PermissionEntity.Fields.permissionGroupId),
                checkUuidIn(search.getGroupIdExcludeList(), true, true, PermissionEntity.Fields.permissionGroupId));
    }
}
