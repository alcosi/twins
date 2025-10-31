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
import org.twins.core.dao.domain.DomainSubscriptionEventEntity;
import org.twins.core.dao.domain.DomainSubscriptionEventRepository;
import org.twins.core.domain.search.DomainSubscriptionEventSearch;

import static org.twins.core.dao.specifications.domain.DomainSubscriptionEventSpecification.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DomainSubscriptionEventSearchService {
    private final DomainSubscriptionEventRepository domainSubscriptionEventRepository;

    public PaginationResult<DomainSubscriptionEventEntity> findDomainSubscriptionEvent(DomainSubscriptionEventSearch search, SimplePagination pagination) throws ServiceException {
        Specification<DomainSubscriptionEventEntity> spec = createDomainSubscriptionEventSearchSpecification(search);
        Page<DomainSubscriptionEventEntity> ret = domainSubscriptionEventRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));

        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<DomainSubscriptionEventEntity> createDomainSubscriptionEventSearchSpecification(DomainSubscriptionEventSearch search) {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, DomainSubscriptionEventEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, DomainSubscriptionEventEntity.Fields.id),
                checkUuidIn(search.getDomainIdList(), false, false, DomainSubscriptionEventEntity.Fields.domainId),
                checkUuidIn(search.getDomainIdExcludeList(), true, false, DomainSubscriptionEventEntity.Fields.domainId),
                checkFieldLikeIn(search.getSubscriptionEventTypeList(), false, true, DomainSubscriptionEventEntity.Fields.subscriptionEventTypeId),
                checkFieldLikeIn(search.getSubscriptionEventTypeExcludeList(), true, false, DomainSubscriptionEventEntity.Fields.subscriptionEventTypeId),
                checkIntegerIn(search.getDispatcherFeaturerIdList(), false, DomainSubscriptionEventEntity.Fields.dispatcherFeaturerId),
                checkIntegerIn(search.getDispatcherFeaturerIdExcludeList(), true, DomainSubscriptionEventEntity.Fields.dispatcherFeaturerId)
        );
    }
}

