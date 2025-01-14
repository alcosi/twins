package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.*;
import org.twins.core.domain.search.FactoryBranchSearch;

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.factory.FactoryBranchSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryBranchSearchService {
    private final TwinFactoryBranchRepository twinFactoryBranchRepository;

    public PaginationResult<TwinFactoryBranchEntity> findFactoryBranches(FactoryBranchSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinFactoryBranchEntity> spec = createFactoryBranchSearchSpecification(search);
        Page<TwinFactoryBranchEntity> ret = twinFactoryBranchRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinFactoryBranchEntity> createFactoryBranchSearchSpecification(FactoryBranchSearch search) {
        return Specification.where(
                checkFieldLikeIn(TwinFactoryBranchEntity.Fields.description, search.getDescriptionLikeList(), false, true)
                        .and(checkFieldLikeIn(TwinFactoryBranchEntity.Fields.description, search.getDescriptionNotLikeList(), true, true))
                        .and(checkUuidIn(TwinFactoryBranchEntity.Fields.id, search.getIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryBranchEntity.Fields.id, search.getIdExcludeList(), true, false))
                        .and(checkUuidIn(TwinFactoryBranchEntity.Fields.twinFactoryId, search.getFactoryIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryBranchEntity.Fields.twinFactoryId, search.getFactoryIdExcludeList(), true, false))
                        .and(checkUuidIn(TwinFactoryBranchEntity.Fields.twinFactoryConditionSetId, search.getFactoryConditionSetIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryBranchEntity.Fields.twinFactoryConditionSetId, search.getFactoryConditionSetIdExcludeList(), true, true))
                        .and(checkUuidIn(TwinFactoryBranchEntity.Fields.nextTwinFactoryId, search.getNextFactoryIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryBranchEntity.Fields.nextTwinFactoryId, search.getNextFactoryIdExcludeList(), true, false))
                        .and(checkTernary(TwinFactoryBranchEntity.Fields.active, search.getActive()))
        );
    }
}
