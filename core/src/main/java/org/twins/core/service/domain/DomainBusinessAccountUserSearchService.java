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
import org.twins.core.dao.domain.DomainBusinessAccountUserEntity;
import org.twins.core.dao.domain.DomainBusinessAccountUserRepository;
import org.twins.core.domain.search.DomainBusinessAccountUserSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.checkFieldLocalDateTimeBetween;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.domain.DomainBusinessAccountUserSpecification.checkUserGroupIdIn;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class DomainBusinessAccountUserSearchService {
    private final DomainBusinessAccountUserRepository domainBusinessAccountUserRepository;
    private final AuthService authService;

    public PaginationResult<DomainBusinessAccountUserEntity> findDomainBusinessAccountUsers(DomainBusinessAccountUserSearch search, SimplePagination pagination) throws ServiceException {
        if (search == null)
            search = new DomainBusinessAccountUserSearch();
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<DomainBusinessAccountUserEntity> spec = createSearchSpecification(search, domainId);
        Page<DomainBusinessAccountUserEntity> page = domainBusinessAccountUserRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(page, pagination);
    }

    private Specification<DomainBusinessAccountUserEntity> createSearchSpecification(DomainBusinessAccountUserSearch search, UUID domainId) {
        return Specification.allOf(
                checkFieldUuid(domainId, DomainBusinessAccountUserEntity.Fields.domainId),
                checkUuidIn(search.getUserIdList(), false, false, DomainBusinessAccountUserEntity.Fields.userId),
                checkUuidIn(search.getUserIdExcludeList(), true, false, DomainBusinessAccountUserEntity.Fields.userId),
                checkUuidIn(search.getBusinessAccountIdList(), false, false, DomainBusinessAccountUserEntity.Fields.businessAccountId),
                checkUuidIn(search.getBusinessAccountIdExcludeList(), true, false, DomainBusinessAccountUserEntity.Fields.businessAccountId),
                checkUserGroupIdIn(search.getUserGroupIdList(), false),
                checkUserGroupIdIn(search.getUserGroupIdExcludeList(), true),
                checkFieldLocalDateTimeBetween(search.getLastActivityAtRange(), DomainBusinessAccountUserEntity.Fields.lastActivityAt),
                checkFieldLocalDateTimeBetween(search.getCreatedAtRange(), DomainBusinessAccountUserEntity.Fields.createdAt)
        );
    }

    private static Specification<DomainBusinessAccountUserEntity> checkFieldUuid(UUID value, String field) {
        return (root, query, cb) -> value == null ? cb.conjunction() : cb.equal(root.get(field), value);
    }
}
