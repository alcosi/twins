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
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dao.twinflow.TwinflowFactoryRepository;
import org.twins.core.domain.search.TwinflowFactorySearch;

import static org.twins.core.dao.specifications.twinflow.TwinflowFactorySpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class TwinflowFactorySearchService {
    private final TwinflowFactoryRepository twinflowFactoryRepository;

    public PaginationResult<TwinflowFactoryEntity> findTwinflowFactory(TwinflowFactorySearch search, SimplePagination pagination) throws ServiceException {
        Page<TwinflowFactoryEntity> ret = twinflowFactoryRepository.findAll(createTwinflowFactorySearchSpecification(search), PaginationUtils.pageableOffset(pagination));

        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinflowFactoryEntity> createTwinflowFactorySearchSpecification(TwinflowFactorySearch search) {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, TwinflowFactoryEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, true, TwinflowFactoryEntity.Fields.id),
                checkUuidIn(search.getTwinflowIdList(), false, false, TwinflowFactoryEntity.Fields.twinflowId),
                checkUuidIn(search.getTwinflowIdExcludeList(), true, true, TwinflowFactoryEntity.Fields.twinflowId),
                checkUuidIn(search.getTwinFactoryIdList(), false, false, TwinflowFactoryEntity.Fields.twinFactoryId),
                checkUuidIn(search.getTwinFactoryIdExcludeList(), true, true, TwinflowFactoryEntity.Fields.twinFactoryId),
                checkFieldLikeIn(search.getFactoryLauncherList(), false, false, TwinflowFactoryEntity.Fields.twinFactorylauncher),
                checkFieldLikeIn(search.getFactoryLauncherExcludeList(), true, true, TwinflowFactoryEntity.Fields.twinFactorylauncher));
    }
}
