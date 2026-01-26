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
import org.twins.core.dao.domain.DomainRepository;
import org.twins.core.domain.search.DomainSearch;

import static org.twins.core.dao.specifications.CommonSpecification.checkFieldLikeIn;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class DomainSearchService {
    private final DomainRepository domainRepository;

    public PaginationResult<DomainEntity> findDomains(DomainSearch search, SimplePagination pagination) throws ServiceException {
        Specification<DomainEntity> spec = createDomainSearchSpecification(search);
        Page<DomainEntity> ret = domainRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<DomainEntity> createDomainSearchSpecification(DomainSearch search) {
        return Specification.allOf(
                checkFieldLikeIn(search.getKeyLikeList(), false, false, DomainEntity.Fields.key),
                checkFieldLikeIn(search.getKeyNotLikeList(), true, true, DomainEntity.Fields.key)
        );
    }
}
