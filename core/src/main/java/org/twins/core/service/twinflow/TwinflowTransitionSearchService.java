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
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionRepository;
import org.twins.core.domain.search.TransitionSearch;

import static org.springframework.data.jpa.domain.Specification.where;
import static org.twins.core.dao.specifications.twinflow.TransitionSpecification.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class TwinflowTransitionSearchService {
    private final TwinflowTransitionRepository twinflowTransitionRepository;

    private Specification<TwinflowTransitionEntity> createTwinflowTransitionEntitySearchSpecification(TransitionSearch transitionSearch) throws ServiceException {
        return where(
                (checkUuidTwinClassIn(transitionSearch.getTwinClassIdList(), false))
                        .and(checkUuidTwinClassIn(transitionSearch.getTwinClassIdExcludeList(), true))
                        .and(checkUuidIn(TwinflowTransitionEntity.Fields.twinflowId, transitionSearch.getTwinflowIdList(), false, false))
                        .and(checkUuidIn(TwinflowTransitionEntity.Fields.twinflowId, transitionSearch.getTwinflowIdExcludeList(), true, false))
                        .and(checkUuidIn(TwinflowTransitionEntity.Fields.srcTwinStatusId, transitionSearch.getSrcStatusIdList(), false, false))
                        .and(checkUuidIn(TwinflowTransitionEntity.Fields.srcTwinStatusId, transitionSearch.getSrcStatusIdExcludeList(), true, true))
                        .and(checkUuidIn(TwinflowTransitionEntity.Fields.dstTwinStatusId, transitionSearch.getDstStatusIdList(), false, false))
                        .and(checkUuidIn(TwinflowTransitionEntity.Fields.dstTwinStatusId, transitionSearch.getDstStatusIdExcludeList(), true, false))
                        .and(checkAliasLikeIn(transitionSearch.getAliasLikeList(), true))
                        .and(checkUuidIn(TwinflowTransitionEntity.Fields.permissionId, transitionSearch.getPermissionIdList(), false, false))
                        .and(checkUuidIn(TwinflowTransitionEntity.Fields.permissionId, transitionSearch.getPermissionIdExcludeList(), true, true))
                        .and(checkUuidIn(TwinflowTransitionEntity.Fields.inbuiltTwinFactoryId, transitionSearch.getInbuiltTwinFactoryIdList(), false, false))
                        .and(checkUuidIn(TwinflowTransitionEntity.Fields.inbuiltTwinFactoryId, transitionSearch.getInbuiltTwinFactoryIdExcludeList(), true, true))
                        .and(checkUuidIn(TwinflowTransitionEntity.Fields.draftingTwinFactoryId, transitionSearch.getDraftingTwinFactoryIdList(), false, false))
                        .and(checkUuidIn(TwinflowTransitionEntity.Fields.draftingTwinFactoryId, transitionSearch.getDraftingTwinFactoryIdExcludeList(), true, true))
        );
    }

    public PaginationResult<TwinflowTransitionEntity> findTransitions(TransitionSearch transitionSearch, SimplePagination pagination) throws ServiceException {
        Specification<TwinflowTransitionEntity> spec = createTwinflowTransitionEntitySearchSpecification(transitionSearch);
        Page<TwinflowTransitionEntity> ret = twinflowTransitionRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }
}
