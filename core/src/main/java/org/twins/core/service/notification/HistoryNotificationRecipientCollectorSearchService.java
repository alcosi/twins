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
import org.twins.core.dao.notification.HistoryNotificationRecipientCollectorEntity;
import org.twins.core.dao.notification.HistoryNotificationRecipientCollectorRepository;
import org.twins.core.dao.notification.HistoryNotificationRecipientEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.HistoryNotificationRecipientCollectorSearch;
import org.twins.core.service.auth.AuthService;

import static org.twins.core.dao.specifications.CommonSpecification.checkFieldUuid;
import static org.twins.core.dao.specifications.CommonSpecification.checkIntegerIn;
import static org.twins.core.dao.specifications.CommonSpecification.checkTernary;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
@Service
public class HistoryNotificationRecipientCollectorSearchService {
    private final AuthService authService;
    private final HistoryNotificationRecipientCollectorRepository recipientCollectorRepository;

    public PaginationResult<HistoryNotificationRecipientCollectorEntity> findHistoryNotificationRecipientCollectors(HistoryNotificationRecipientCollectorSearch search, SimplePagination pagination) throws ServiceException {
        Specification<HistoryNotificationRecipientCollectorEntity> spec = createDataListOptionSearchSpecification(search);
        Page<HistoryNotificationRecipientCollectorEntity> ret = recipientCollectorRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<HistoryNotificationRecipientCollectorEntity> createDataListOptionSearchSpecification(HistoryNotificationRecipientCollectorSearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return Specification.allOf(
                checkFieldUuid(apiUser.getDomainId(), HistoryNotificationRecipientCollectorEntity.Fields.historyNotificationRecipientEntity, HistoryNotificationRecipientEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, HistoryNotificationRecipientCollectorEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, HistoryNotificationRecipientCollectorEntity.Fields.id),
                checkUuidIn(search.getRecipientIdList(), false, false, HistoryNotificationRecipientCollectorEntity.Fields.historyNotificationRecipientId),
                checkUuidIn(search.getRecipientIdExcludeList(), true, false, HistoryNotificationRecipientCollectorEntity.Fields.historyNotificationRecipientId),
                checkIntegerIn(search.getRecipientResolverFeaturerIdList(), false, HistoryNotificationRecipientCollectorEntity.Fields.recipientResolverFeaturerId),
                checkIntegerIn(search.getRecipientResolverFeaturerIdExcludeList(), true, HistoryNotificationRecipientCollectorEntity.Fields.recipientResolverFeaturerId),
                checkTernary(search.getExclude(), HistoryNotificationRecipientCollectorEntity.Fields.exclude)
                );
    }
}
