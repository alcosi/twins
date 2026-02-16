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
import org.twins.core.dao.notification.HistoryNotificationSchemaMapEntity;
import org.twins.core.dao.notification.HistoryNotificationSchemaMapRepository;
import org.twins.core.domain.search.HistoryNotificationSchemaMapSearch;

import static org.twins.core.dao.specifications.CommonSpecification.checkFieldIn;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.CommonSpecification.checkTernary;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
@Service
public class HistoryNotificationSchemaMapSearchService {
    private final HistoryNotificationSchemaMapRepository repository;

    public PaginationResult<HistoryNotificationSchemaMapEntity> findHistoryNotificationSchemaMap(HistoryNotificationSchemaMapSearch search, SimplePagination pagination) throws ServiceException {
        Specification<HistoryNotificationSchemaMapEntity> spec = createSearchSpecification(search);
        Page<HistoryNotificationSchemaMapEntity> ret = repository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<HistoryNotificationSchemaMapEntity> createSearchSpecification(HistoryNotificationSchemaMapSearch search) throws ServiceException {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, HistoryNotificationSchemaMapEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, HistoryNotificationSchemaMapEntity.Fields.id),
                checkFieldIn(search.getHistoryTypeIdList(), false, false, false, HistoryNotificationSchemaMapEntity.Fields.historyTypeId),
                checkFieldIn(search.getHistoryTypeIdExcludeList(), true, false, false, HistoryNotificationSchemaMapEntity.Fields.historyTypeId),
                checkUuidIn(search.getTwinClassIdList(), false, false, HistoryNotificationSchemaMapEntity.Fields.twinClassId),
                checkUuidIn(search.getTwinClassIdExcludeList(), true, false, HistoryNotificationSchemaMapEntity.Fields.twinClassId),
                checkUuidIn(search.getTwinClassFieldIdList(), false, false, HistoryNotificationSchemaMapEntity.Fields.twinClassFieldId),
                checkUuidIn(search.getTwinClassFieldIdExcludeList(), true, false, HistoryNotificationSchemaMapEntity.Fields.twinClassFieldId),
                checkUuidIn(search.getTwinValidatorSetIdList(), false, false, HistoryNotificationSchemaMapEntity.Fields.twinValidatorSetId),
                checkUuidIn(search.getTwinValidatorSetIdExcludeList(), true, false, HistoryNotificationSchemaMapEntity.Fields.twinValidatorSetId),
                checkTernary(search.getTwinValidatorSetInvert(), HistoryNotificationSchemaMapEntity.Fields.twinValidatorSetInvert),
                checkUuidIn(search.getNotificationSchemaIdList(), false, false, HistoryNotificationSchemaMapEntity.Fields.notificationSchemaId),
                checkUuidIn(search.getNotificationSchemaIdExcludeList(), true, false, HistoryNotificationSchemaMapEntity.Fields.notificationSchemaId),
                checkUuidIn(search.getHistoryNotificationRecipientIdList(), false, false, HistoryNotificationSchemaMapEntity.Fields.historyNotificationRecipientId),
                checkUuidIn(search.getHistoryNotificationRecipientIdExcludeList(), true, false, HistoryNotificationSchemaMapEntity.Fields.historyNotificationRecipientId),
                checkUuidIn(search.getNotificationChannelEventIdList(), false, false, HistoryNotificationSchemaMapEntity.Fields.notificationChannelEventId),
                checkUuidIn(search.getNotificationChannelEventIdExcludeList(), true, false, HistoryNotificationSchemaMapEntity.Fields.notificationChannelEventId)
        );
    }
}
