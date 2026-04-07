package org.twins.core.service.trigger;

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
import org.twins.core.dao.trigger.TwinTriggerTaskEntity;
import org.twins.core.dao.trigger.TwinTriggerTaskRepository;
import org.twins.core.domain.search.TwinTriggerTaskSearch;
import org.twins.core.service.businessaccount.BusinessAccountService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.user.UserService;

import java.util.Collection;
import java.util.Collections;

import static org.twins.core.dao.specifications.CommonSpecification.checkFieldLikeIn;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
@Service
public class TwinTriggerTaskSearchService {
    private final TwinTriggerTaskRepository twinTriggerTaskRepository;
    private final BusinessAccountService businessAccountService;
    private final TwinTriggerService twinTriggerService;
    private final TwinStatusService twinStatusService;
    private final TwinService twinService;
    private final UserService userService;

    public PaginationResult<TwinTriggerTaskEntity> findTwinTriggerTasks(TwinTriggerTaskSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinTriggerTaskEntity> spec = createTwinTriggerTaskSearchSpecification(search);
        Page<TwinTriggerTaskEntity> ret = twinTriggerTaskRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinTriggerTaskEntity> createTwinTriggerTaskSearchSpecification(TwinTriggerTaskSearch search) throws ServiceException {
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

                checkFieldLikeIn(search.getStatusIdList().stream().map(Enum::name).toList(), false, false, TwinTriggerTaskEntity.Fields.statusId),
                checkFieldLikeIn(search.getStatusIdExcludeList().stream().map(Enum::name).toList(), true, false, TwinTriggerTaskEntity.Fields.statusId)
        );
    }

    public void loadBusinessAccount(TwinTriggerTaskEntity src) throws ServiceException {
        loadBusinessAccounts(Collections.singletonList(src));
    }

    public void loadBusinessAccounts(Collection<TwinTriggerTaskEntity> srcCollection) throws ServiceException {
        businessAccountService.load(srcCollection,
                TwinTriggerTaskEntity::getId,
                TwinTriggerTaskEntity::getBusinessAccountId,
                TwinTriggerTaskEntity::getBusinessAccount,
                TwinTriggerTaskEntity::setBusinessAccount);
    }

    public void loadTwin(TwinTriggerTaskEntity src) throws ServiceException {
        loadTwins(Collections.singletonList(src));
    }

    public void loadTwins(Collection<TwinTriggerTaskEntity> srcCollection) throws ServiceException {
        twinService.load(srcCollection,
                TwinTriggerTaskEntity::getId,
                TwinTriggerTaskEntity::getTwinId,
                TwinTriggerTaskEntity::getTwin,
                TwinTriggerTaskEntity::setTwin);
    }

    public void loadTwinTrigger(TwinTriggerTaskEntity src) throws ServiceException {
        loadTwinTriggers(Collections.singletonList(src));
    }

    public void loadTwinTriggers(Collection<TwinTriggerTaskEntity> srcCollection) throws ServiceException {
        twinTriggerService.load(srcCollection,
                TwinTriggerTaskEntity::getId,
                TwinTriggerTaskEntity::getTwinTriggerId,
                TwinTriggerTaskEntity::getTwinTrigger,
                TwinTriggerTaskEntity::setTwinTrigger);
    }

    public void loadTwinStatus(TwinTriggerTaskEntity src) throws ServiceException {
        loadTwinStatuses(Collections.singletonList(src));
    }

    public void loadTwinStatuses(Collection<TwinTriggerTaskEntity> srcCollection) throws ServiceException {
        twinStatusService.load(srcCollection,
                TwinTriggerTaskEntity::getId,
                TwinTriggerTaskEntity::getPreviousTwinStatusId,
                TwinTriggerTaskEntity::getPreviousTwinStatus,
                TwinTriggerTaskEntity::setPreviousTwinStatus);
    }

    public void loadCreatedByUser(TwinTriggerTaskEntity src) throws ServiceException {
        loadCreatedByUser(Collections.singletonList(src));
    }

    public void loadCreatedByUser(Collection<TwinTriggerTaskEntity> srcCollection) throws ServiceException {
        userService.load(srcCollection,
                TwinTriggerTaskEntity::getId,
                TwinTriggerTaskEntity::getCreatedByUserId,
                TwinTriggerTaskEntity::getCreatedByUser,
                TwinTriggerTaskEntity::setCreatedByUser);
    }
}
