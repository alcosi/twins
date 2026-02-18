package org.twins.core.service.notification;

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
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.dao.notification.HistoryNotificationRepository;
import org.twins.core.domain.search.HistoryNotificationSearch;

import static org.twins.core.dao.specifications.CommonSpecification.checkFieldIn;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.CommonSpecification.checkTernary;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
@Service
public class HistoryNotificationSearchService {
    private final HistoryNotificationRepository repository;

    public PaginationResult<HistoryNotificationEntity> findHistoryNotification(HistoryNotificationSearch search, SimplePagination pagination) throws ServiceException {
        Specification<HistoryNotificationEntity> spec = createSearchSpecification(search);
        Page<HistoryNotificationEntity> ret = repository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<HistoryNotificationEntity> createSearchSpecification(HistoryNotificationSearch search) throws ServiceException {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, HistoryNotificationEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, HistoryNotificationEntity.Fields.id),
                checkFieldIn(search.getHistoryTypeIdList(), false, false, false, HistoryNotificationEntity.Fields.historyTypeId),
                checkFieldIn(search.getHistoryTypeIdExcludeList(), true, false, false, HistoryNotificationEntity.Fields.historyTypeId),
                checkUuidIn(search.getTwinClassIdList(), false, false, HistoryNotificationEntity.Fields.twinClassId),
                checkUuidIn(search.getTwinClassIdExcludeList(), true, false, HistoryNotificationEntity.Fields.twinClassId),
                checkUuidIn(search.getTwinClassFieldIdList(), false, false, HistoryNotificationEntity.Fields.twinClassFieldId),
                checkUuidIn(search.getTwinClassFieldIdExcludeList(), true, false, HistoryNotificationEntity.Fields.twinClassFieldId),
                checkUuidIn(search.getTwinValidatorSetIdList(), false, false, HistoryNotificationEntity.Fields.twinValidatorSetId),
                checkUuidIn(search.getTwinValidatorSetIdExcludeList(), true, false, HistoryNotificationEntity.Fields.twinValidatorSetId),
                checkTernary(search.getTwinValidatorSetInvert(), HistoryNotificationEntity.Fields.twinValidatorSetInvert),
                checkUuidIn(search.getNotificationSchemaIdList(), false, false, HistoryNotificationEntity.Fields.notificationSchemaId),
                checkUuidIn(search.getNotificationSchemaIdExcludeList(), true, false, HistoryNotificationEntity.Fields.notificationSchemaId),
                checkUuidIn(search.getHistoryNotificationRecipientIdList(), false, false, HistoryNotificationEntity.Fields.historyNotificationRecipientId),
                checkUuidIn(search.getHistoryNotificationRecipientIdExcludeList(), true, false, HistoryNotificationEntity.Fields.historyNotificationRecipientId),
                checkUuidIn(search.getNotificationChannelEventIdList(), false, false, HistoryNotificationEntity.Fields.notificationChannelEventId),
                checkUuidIn(search.getNotificationChannelEventIdExcludeList(), true, false, HistoryNotificationEntity.Fields.notificationChannelEventId)
        );
    }
}
