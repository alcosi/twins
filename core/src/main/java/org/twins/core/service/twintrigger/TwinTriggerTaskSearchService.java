package org.twins.core.service.twintrigger;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dao.trigger.TwinTriggerTaskEntity;
import org.twins.core.dao.trigger.TwinTriggerTaskRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.search.TwinTriggerTaskSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.TwinTriggerTaskGroupField;
import org.twins.core.enums.sort.TwinTriggerTaskSortField;
import org.twins.core.enums.trigger.TwinTriggerTaskStatus;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecificationDirect;
import static org.twins.core.dao.specifications.CommonSpecification.*;
import static org.twins.core.dao.trigger.TwinTriggerTaskSpecification.checkStatusLikeIn;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
@Service
public class TwinTriggerTaskSearchService extends EntitySearchService
        <TwinTriggerTaskSearch, TwinTriggerTaskEntity, TwinTriggerTaskSortField, TwinTriggerTaskGroupField> {
    private final TwinTriggerTaskRepository twinTriggerTaskRepository;

    @Override
    public JpaSpecificationExecutor<TwinTriggerTaskEntity> jpaSpecificationExecutor() {
        return twinTriggerTaskRepository;
    }

    @Override
    public TwinTriggerTaskSearch emptySearch() {
        return new TwinTriggerTaskSearch();
    }

    @Override
    protected TwinTriggerTaskEntity newEntity() {
        return new TwinTriggerTaskEntity();
    }

    @Override
    protected Class<TwinTriggerTaskEntity> entityClass() {
        return TwinTriggerTaskEntity.class;
    }

    @Override
    public Specification<TwinTriggerTaskEntity> createFilterSpecification(TwinTriggerTaskSearch search, UUID domainId, Locale locale) throws ServiceException {
        return Specification.allOf(
                checkFieldUuid(domainId, TwinTriggerTaskEntity.Fields.twinTriggerSpecOnly, TwinTriggerEntity.Fields.domainId),
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
                checkStatusLikeIn(search.getStatusIdList(), false),
                checkStatusLikeIn(search.getStatusIdExcludeList(), true)
        );
    }

    @Override
    public Specification<TwinTriggerTaskEntity> createSortSpecification(TwinTriggerTaskSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        if (sortField == null)
            sortField = TwinTriggerTaskSortField.createdAt;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case createdAt -> toSortSpecification(ascending, TwinTriggerTaskEntity.Fields.createdAt);
            case doneAt -> toSortSpecification(ascending, TwinTriggerTaskEntity.Fields.doneAt);
            case statusId -> toSortSpecification(ascending, TwinTriggerTaskEntity.Fields.statusId);
            case statusDetails -> toSortSpecification(ascending, TwinTriggerTaskEntity.Fields.statusDetails);
            case twinName -> toSortSpecification(ascending, TwinTriggerTaskEntity.Fields.twinSpecOnly, TwinEntity.Fields.name);
            case createdByUserName -> toSortSpecification(ascending, TwinTriggerTaskEntity.Fields.createdByUserSpecOnly, UserEntity.Fields.name);
            case twinTriggerName -> toSortSpecification(ascending, TwinTriggerTaskEntity.Fields.twinTriggerSpecOnly, TwinTriggerEntity.Fields.name);
            case previousTwinStatusName -> toSortSpecificationDirect(ascending, locale, TwinTriggerTaskEntity.Fields.previousTwinStatusSpecOnly, TwinStatusEntity.Fields.nameI18nTranslationsSpecOnly);
            case businessAccountName -> toSortSpecification(ascending, TwinTriggerTaskEntity.Fields.businessAccountSpecOnly, BusinessAccountEntity.Fields.name);
        };
    }

    @Override
    public String convertToEntityField(TwinTriggerTaskGroupField groupField) throws ServiceException {
        return switch (groupField) {
            case twinId -> TwinTriggerTaskEntity.Fields.twinId;
            case twinTriggerId -> TwinTriggerTaskEntity.Fields.twinTriggerId;
            case previousTwinStatusId -> TwinTriggerTaskEntity.Fields.previousTwinStatusId;
            case createdByUserId -> TwinTriggerTaskEntity.Fields.createdByUserId;
            case businessAccountId -> TwinTriggerTaskEntity.Fields.businessAccountId;
            case statusId -> TwinTriggerTaskEntity.Fields.statusId;
        };
    }

    @Override
    public void mapGroupedField(TwinTriggerTaskEntity entity, TwinTriggerTaskGroupField field, Object o) {
        switch (field) {
            case twinId -> entity.setTwinId((UUID) o);
            case twinTriggerId -> entity.setTwinTriggerId((UUID) o);
            case previousTwinStatusId -> entity.setPreviousTwinStatusId((UUID) o);
            case createdByUserId -> entity.setCreatedByUserId((UUID) o);
            case businessAccountId -> entity.setBusinessAccountId((UUID) o);
            case statusId -> entity.setStatusId((TwinTriggerTaskStatus) o);
        }
    }
}
