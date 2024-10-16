package org.twins.core.service.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dao.domain.DomainUserRepository;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.search.DomainUserSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.specifications.domain.DomainUserSpecification.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DomainUserSearchService {
    private final DomainUserRepository domainUserRepository;
    private final AuthService authService;

    public PaginationResult<DomainUserEntity> findDomainUser(DomainUserSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<DomainUserEntity> spec = createDomainUserSearchSpecification(search)
                .and(checkDomainId(domainId));
        Page<DomainUserEntity> ret = domainUserRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<DomainUserEntity> createDomainUserSearchSpecification(DomainUserSearch search) {
        return Specification.where(
                checkUuidIn(DomainUserEntity.Fields.userId, search.getUserIdList(), false, false)
                        .and(checkUuidIn(DomainUserEntity.Fields.userId, search.getUserIdExcludeList(), true, false))
                        .and(checkFieldLikeIn(UserEntity.Fields.name, search.getNameLikeList(), true))
                        .and(checkFieldNotLikeIn(UserEntity.Fields.name, search.getNameNotLikeList(), true))
                        .and(checkFieldLikeIn(UserEntity.Fields.email, search.getEmailLikeList(), true))
                        .and(checkFieldNotLikeIn(UserEntity.Fields.email, search.getEmailNotLikeList(), true))
                        .and(checkUserStatusIn(search.getStatusIdList(), false))
                        .and(checkUserStatusIn(search.getStatusIdExcludeList(), true))
                        .and(checkBusinessAccountIn(search.getBusinessAccountIdList(), false))
                        .and(checkBusinessAccountIn(search.getBusinessAccountIdExcludeList(), true))
        );
    }

}
