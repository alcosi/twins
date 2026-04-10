package org.twins.core.service.trigger;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dao.trigger.TwinTriggerTaskEntity;
import org.twins.core.dao.trigger.TwinTriggerTaskRepository;
import org.twins.core.domain.search.TwinTriggerTaskSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.checkFieldLikeIn;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
@Service
public class TwinTriggerTaskSearchService {
    private final TwinTriggerTaskRepository twinTriggerTaskRepository;
    private final AuthService authService;

    public PaginationResult<TwinTriggerTaskEntity> findTwinTriggerTasks(TwinTriggerTaskSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinTriggerTaskEntity> spec = createTwinTriggerTaskSearchSpecification(search);
        Page<TwinTriggerTaskEntity> ret = twinTriggerTaskRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinTriggerTaskEntity> createTwinTriggerTaskSearchSpecification(TwinTriggerTaskSearch search) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, TwinTriggerTaskEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinTriggerTaskEntity.Fields.id),
                checkUuidIn(search.getTwinIdList(), false, false, TwinTriggerTaskEntity.Fields.twinId),
                checkUuidIn(search.getTwinIdExcludeList(), true, false, TwinTriggerTaskEntity.Fields.twinId),
                checkUuidIn(search.getTwinTriggerIdList(), false, false, TwinTriggerTaskEntity.Fields.twinTriggerId),
                checkUuidIn(search.getTwinTriggerIdExcludeList(), true, false, TwinTriggerTaskEntity.Fields.twinTriggerId),
                checkUuidIn(search.getPreviousTwinStatusIdList(), false, false, TwinTriggerTaskEntity.Fields.previousTwinStatusId),
                checkUuidIn(search.getPreviousTwinStatusIdExcludeList(), true, false, TwinTriggerTaskEntity.Fields.previousTwinStatusId),
                checkUuidIn(search.getCreatedByUserIdList(), false, false, TwinTriggerTaskEntity.Fields.createdByUserId),
                checkUuidIn(search.getCreatedByUserIdExcludeList(), true, false, TwinTriggerTaskEntity.Fields.createdByUserId),
                checkUuidIn(search.getBusinessAccountIdList(), false, false, TwinTriggerTaskEntity.Fields.businessAccountId),
                checkUuidIn(search.getBusinessAccountIdExcludeList(), true, false, TwinTriggerTaskEntity.Fields.businessAccountId),

                checkFieldLikeIn(search.getStatusIdList() == null ? null : search.getStatusIdList().stream().map(Enum::name).toList(), false, false, TwinTriggerTaskEntity.Fields.statusId),
                checkFieldLikeIn(search.getStatusIdList() == null ? null : search.getStatusIdList().stream().map(Enum::name).toList(), true, false, TwinTriggerTaskEntity.Fields.statusId),
                (root, query, cb) -> {
                    Join<TwinTriggerTaskEntity, TwinTriggerEntity> triggerJoin = root.join("twinTrigger", JoinType.INNER);
                    return cb.equal(triggerJoin.get("domainId"), domainId);
                }
        );
    }
}
