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
import org.twins.core.dao.notification.HistoryNotificationRecipientEntity;
import org.twins.core.dao.notification.HistoryNotificationRecipientRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.HistoryNotificationRecipientSearch;
import org.twins.core.service.auth.AuthService;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.CommonSpecification.checkFieldUuid;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
@Service
public class HistoryNotificationRecipientSearchService {
    private final AuthService authService;
    private final HistoryNotificationRecipientRepository recipientRepository;

    public PaginationResult<HistoryNotificationRecipientEntity> findHistoryNotificationRecipientForDomain(HistoryNotificationRecipientSearch search, SimplePagination pagination) throws ServiceException {
        Specification<HistoryNotificationRecipientEntity> spec = createDataListOptionSearchSpecification(search);
        Page<HistoryNotificationRecipientEntity> ret = recipientRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<HistoryNotificationRecipientEntity> createDataListOptionSearchSpecification(HistoryNotificationRecipientSearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return Specification.allOf(
                checkFieldUuid(apiUser.getDomainId(), HistoryNotificationRecipientEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, HistoryNotificationRecipientEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, HistoryNotificationRecipientEntity.Fields.id),
                joinAndSearchByI18NField(HistoryNotificationRecipientEntity.Fields.nameI18n, search.getNameLikeList(), apiUser.getLocale(), false, false),
                joinAndSearchByI18NField(HistoryNotificationRecipientEntity.Fields.nameI18n, search.getNameNotLikeList(), apiUser.getLocale(), true, true),
                joinAndSearchByI18NField(HistoryNotificationRecipientEntity.Fields.descriptionI18n, search.getDescriptionLikeList(), apiUser.getLocale(), false, false),
                joinAndSearchByI18NField(HistoryNotificationRecipientEntity.Fields.descriptionI18n, search.getDescriptionNotLikeList(), apiUser.getLocale(), true, true));
    }
}
