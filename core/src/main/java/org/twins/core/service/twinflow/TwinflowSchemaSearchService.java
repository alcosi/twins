package org.twins.core.service.twinflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaRepository;
import org.twins.core.domain.search.TwinflowSchemaSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.specifications.twinflow.TwinflowSchemaSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class TwinflowSchemaSearchService {
    private final AuthService authService;
    private final TwinflowSchemaRepository twinflowSchemaRepository;

    public PaginationResult<TwinflowSchemaEntity> findTwinflowSchemaForDomain(TwinflowSchemaSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<TwinflowSchemaEntity> spec = createTwinflowSchemaSearchSpecification(search)
                .and(checkDomainId(domainId));
        Page<TwinflowSchemaEntity> ret = twinflowSchemaRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinflowSchemaEntity> createTwinflowSchemaSearchSpecification(TwinflowSchemaSearch search) throws ServiceException {
        return Specification.where(
                checkFieldLikeIn(TwinflowSchemaEntity.Fields.name, search.getNameLikeList(), false, true)
                        .and(checkFieldLikeIn(TwinflowSchemaEntity.Fields.name, search.getNameNotLikeList(), true, true))
                        .and(checkFieldLikeIn(TwinflowSchemaEntity.Fields.description, search.getDescriptionLikeList(), false, true))
                        .and(checkFieldLikeIn(TwinflowSchemaEntity.Fields.description, search.getDescriptionNotLikeList(), true, true))
                        .and(checkUuidIn(TwinflowSchemaEntity.Fields.id, search.getIdList(), false, false))
                        .and(checkUuidIn(TwinflowSchemaEntity.Fields.id, search.getIdExcludeList(), true, true))
                        .and(checkUuidIn(TwinflowSchemaEntity.Fields.businessAccountId, search.getBusinessAccountIdList(), false, true))
                        .and(checkUuidIn(TwinflowSchemaEntity.Fields.businessAccountId, search.getBusinessAccountIdExcludeList(), true, true))
                        .and(checkUuidIn(TwinflowSchemaEntity.Fields.createdByUserId, search.getCreatedByUserIdList(), false, false))
                        .and(checkUuidIn(TwinflowSchemaEntity.Fields.createdByUserId, search.getCreatedByUserIdExcludeList(), true, true))
        );
    }
}
