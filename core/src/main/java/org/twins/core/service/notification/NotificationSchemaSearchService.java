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
import org.twins.core.dao.notification.NotificationSchemaEntity;
import org.twins.core.dao.notification.NotificationSchemaRepository;
import org.twins.core.domain.search.NotificationSchemaSearch;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.*;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class NotificationSchemaSearchService {
    private final AuthService authService;
    private final NotificationSchemaRepository notificationSchemaRepository;

    public PaginationResult<NotificationSchemaEntity> findNotificationSchemasByDomain(NotificationSchemaSearch search, SimplePagination pagination) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        Specification<NotificationSchemaEntity> spec = createNotificationSchemaSearchSpecification(search)
                .and(checkFieldUuid(domainId, NotificationSchemaEntity.Fields.domainId));
        Page<NotificationSchemaEntity> ret = notificationSchemaRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<NotificationSchemaEntity> createNotificationSchemaSearchSpecification(NotificationSchemaSearch search) throws ServiceException {
        return Specification.allOf(
                checkFieldLikeContainsIn(search.getNameLikeList(), false, false, NotificationSchemaEntity.Fields.nameI18nId),
                checkFieldLikeContainsIn(search.getNameNotLikeList(), true, true, NotificationSchemaEntity.Fields.nameI18nId),
                checkUuidIn(search.getIdList(), false, true, NotificationSchemaEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, NotificationSchemaEntity.Fields.id),
                checkUuidIn(search.getCreatedByUserIdList(), false, true, NotificationSchemaEntity.Fields.createdByUserId),
                checkUuidIn(search.getCreatedByUserIdExcludeList(), true, true, NotificationSchemaEntity.Fields.createdByUserId));
    }
}
