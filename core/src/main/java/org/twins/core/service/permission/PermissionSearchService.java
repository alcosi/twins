package org.twins.core.service.permission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dao.permission.PermissionRepository;
import org.twins.core.domain.search.PermissionSearch;
import org.twins.core.service.auth.AuthService;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.cambium.i18n.dao.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.permission.PermissionSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionSearchService {
    private final AuthService authService;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public PermissionEntity findPermissionById(UUID id) throws ServiceException {
        Optional<PermissionEntity> entity = permissionRepository.findBy(
                Specification.allOf(
                        checkFieldUuid(authService.getApiUser().getDomainId(), PermissionEntity.Fields.permissionGroup, PermissionGroupEntity.Fields.domainId),
                        checkFieldUuid(id, PermissionEntity.Fields.id)
                ), FluentQuery.FetchableFluentQuery::one
        );
        return entity.orElse(null);
    }
    @Transactional(readOnly = true)
    public PermissionEntity findPermissionByKey(String key) throws ServiceException {
        Optional<PermissionEntity> entity = permissionRepository.findBy(
                Specification.allOf(
                        checkFieldUuid(authService.getApiUser().getDomainId(), PermissionEntity.Fields.permissionGroup, PermissionGroupEntity.Fields.domainId),
                        checkFieldStringLike(key, PermissionEntity.Fields.key)
                ), FluentQuery.FetchableFluentQuery::one
        );
        return entity.orElse(null);
    }

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
                checkFieldLikeContainsIn(PermissionEntity.Fields.key, search.getKeyLikeList(), false, true),
                checkFieldLikeContainsIn(PermissionEntity.Fields.key, search.getKeyNotLikeList(), true, true),
                checkUuidIn(PermissionEntity.Fields.id, search.getIdList(), false, true),
                checkUuidIn(PermissionEntity.Fields.id, search.getIdExcludeList(), true, false),
                joinAndSearchByI18NField(PermissionEntity.Fields.nameI18NId, search.getNameI18nLikeList(), locale, false, false),
                joinAndSearchByI18NField(PermissionEntity.Fields.nameI18NId, search.getNameI18nNotLikeList(), locale, true, true),
                joinAndSearchByI18NField(PermissionEntity.Fields.descriptionI18NId, search.getDescriptionI18nLikeList(), locale, false, false),
                joinAndSearchByI18NField(PermissionEntity.Fields.descriptionI18NId, search.getDescriptionI18nNotLikeList(), locale, true, true),
                checkUuidIn(PermissionEntity.Fields.permissionGroupId, search.getGroupIdList(), false, true),
                checkUuidIn(PermissionEntity.Fields.permissionGroupId, search.getGroupIdExcludeList(), true, true));
    }
}
