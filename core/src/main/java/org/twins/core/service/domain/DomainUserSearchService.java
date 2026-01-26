package org.twins.core.service.domain;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dao.domain.DomainUserRepository;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.DomainUserSearch;
import org.twins.core.enums.domain.DomainType;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;

import static org.twins.core.dao.specifications.domain.DomainUserSpecification.*;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class DomainUserSearchService {
    private final DomainUserRepository domainUserRepository;
    private final AuthService authService;
    private final PermissionService permissionService;

    public PaginationResult<DomainUserEntity> findDomainUser(DomainUserSearch search, SimplePagination pagination) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (apiUser.getDomain().getDomainType().equals(DomainType.b2b) && !permissionService.currentUserHasPermission(Permissions.DOMAIN_USER_MANAGE)) {
            if (apiUser.isBusinessAccountSpecified()) {
                search.addBusinessAccountId(apiUser.getBusinessAccountId(), false);
            } else {
                log.warn("BusinessAccount is not specified for domain user {}", apiUser.getUserId());
                return new PaginationResult<>();
            }
        }

        Specification<DomainUserEntity> spec = createDomainUserSearchSpecification(search).and(checkFieldUuid(apiUser.getDomainId(), DomainUserEntity.Fields.domain, DomainEntity.Fields.id));
        Page<DomainUserEntity> ret = domainUserRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<DomainUserEntity> createDomainUserSearchSpecification(DomainUserSearch search) {
        return Specification.allOf(checkBusinessAccountIn(search.getBusinessAccountIdList(), false), checkBusinessAccountIn(search.getBusinessAccountIdExcludeList(), true), checkDomainUserFieldLikeIn(UserEntity.Fields.name, search.getNameLikeList(), true), checkDomainUserFieldNotLikeIn(UserEntity.Fields.name, search.getNameNotLikeList(), true), checkDomainUserFieldLikeIn(UserEntity.Fields.email, search.getEmailLikeList(), true), checkDomainUserFieldNotLikeIn(UserEntity.Fields.email, search.getEmailNotLikeList(), true), checkUserStatusIn(search.getStatusIdList(), false), checkUserStatusIn(search.getStatusIdExcludeList(), true), checkUuidIn(search.getUserIdList(), false, false, DomainUserEntity.Fields.userId), checkUuidIn(search.getUserIdExcludeList(), true, false, DomainUserEntity.Fields.userId));
    }

}
