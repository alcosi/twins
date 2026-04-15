package org.twins.core.service.action;

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
import org.twins.core.dao.action.ActionRestrictionReasonEntity;
import org.twins.core.dao.action.ActionRestrictionReasonRepository;
import org.twins.core.dao.notification.HistoryNotificationRecipientEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.ActionRestrictionReasonSearch;
import org.twins.core.service.auth.AuthService;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.CommonSpecification.*;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class ActionRestrictionReasonSearchService {
    private final AuthService authService;
    private final ActionRestrictionReasonRepository actionRestrictionReasonRepository;

    public PaginationResult<ActionRestrictionReasonEntity> findActionRestrictionReasons(ActionRestrictionReasonSearch search, SimplePagination pagination) throws ServiceException {
        Specification<ActionRestrictionReasonEntity> spec = createActionRestrictionReasonSpecification(search);
        Page<ActionRestrictionReasonEntity> ret = actionRestrictionReasonRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<ActionRestrictionReasonEntity> createActionRestrictionReasonSpecification(ActionRestrictionReasonSearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return Specification.allOf(
                checkFieldUuid(apiUser.getDomainId(), ActionRestrictionReasonEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, ActionRestrictionReasonEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, ActionRestrictionReasonEntity.Fields.id),
                checkFieldLikeIn(search.getTypeLikeList(), false, true, ActionRestrictionReasonEntity.Fields.type),
                checkFieldLikeIn(search.getTypeNotLikeList(), true, true, ActionRestrictionReasonEntity.Fields.type),
                joinAndSearchByI18NField(ActionRestrictionReasonEntity.Fields.descriptionI18n, search.getDescriptionLikeList(), apiUser.getLocale(), false, false),
                joinAndSearchByI18NField(ActionRestrictionReasonEntity.Fields.descriptionI18n, search.getDescriptionNotLikeList(), apiUser.getLocale(), true, true)
        );
    }
}
