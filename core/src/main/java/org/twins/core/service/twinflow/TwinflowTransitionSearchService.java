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
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.twinflow.TransitionSpecification.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class TwinflowTransitionSearchService {
    private final TwinflowTransitionRepository twinflowTransitionRepository;

    private Specification<TwinflowTransitionEntity> createTwinflowTransitionEntitySearchSpecification(TransitionSearch transitionSearch) {
        return where(
                (checkUuidTwinClassIn(transitionSearch.getTwinClassIdList(), false))
                        .and(checkUuidIn(transitionSearch.getIdList(), false, false, TwinflowTransitionEntity.Fields.id))
                        .and(checkUuidIn(transitionSearch.getIdExcludeList(), true, false, TwinflowTransitionEntity.Fields.id))
                        .and(checkUuidTwinClassIn(transitionSearch.getTwinClassIdExcludeList(), true))
                        .and(checkUuidIn(transitionSearch.getTwinflowIdList(), false, false, TwinflowTransitionEntity.Fields.twinflowId))
                        .and(checkUuidIn(transitionSearch.getTwinflowIdExcludeList(), true, false, TwinflowTransitionEntity.Fields.twinflowId))
                        .and(checkUuidIn(transitionSearch.getSrcStatusIdList(), false, false, TwinflowTransitionEntity.Fields.srcTwinStatusId))
                        .and(checkUuidIn(transitionSearch.getSrcStatusIdExcludeList(), true, true, TwinflowTransitionEntity.Fields.srcTwinStatusId))
                        .and(checkUuidIn(transitionSearch.getDstStatusIdList(), false, false, TwinflowTransitionEntity.Fields.dstTwinStatusId))
                        .and(checkUuidIn(transitionSearch.getDstStatusIdExcludeList(), true, false, TwinflowTransitionEntity.Fields.dstTwinStatusId))
                        .and(checkAliasLikeIn(transitionSearch.getAliasLikeList(), true))
                        .and(checkUuidIn(transitionSearch.getPermissionIdList(), false, false, TwinflowTransitionEntity.Fields.permissionId))
                        .and(checkUuidIn(transitionSearch.getPermissionIdExcludeList(), true, true, TwinflowTransitionEntity.Fields.permissionId))
                        .and(checkUuidIn(transitionSearch.getInbuiltTwinFactoryIdList(), false, false, TwinflowTransitionEntity.Fields.inbuiltTwinFactoryId))
                        .and(checkUuidIn(transitionSearch.getInbuiltTwinFactoryIdExcludeList(), true, true, TwinflowTransitionEntity.Fields.inbuiltTwinFactoryId))
                        .and(checkUuidIn(transitionSearch.getDraftingTwinFactoryIdList(), false, false, TwinflowTransitionEntity.Fields.draftingTwinFactoryId))
                        .and(checkUuidIn(transitionSearch.getDraftingTwinFactoryIdExcludeList(), true, true, TwinflowTransitionEntity.Fields.draftingTwinFactoryId))
        );
    }

    public PaginationResult<TwinflowTransitionEntity> findTransitions(TransitionSearch transitionSearch, SimplePagination pagination) throws ServiceException {
        Specification<TwinflowTransitionEntity> spec = createTwinflowTransitionEntitySearchSpecification(transitionSearch);
        Page<TwinflowTransitionEntity> ret = twinflowTransitionRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }
}
