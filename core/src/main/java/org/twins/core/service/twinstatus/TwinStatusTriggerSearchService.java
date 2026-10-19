package org.twins.core.service.twinstatus;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twin.TwinStatusTriggerEntity;
import org.twins.core.dao.twin.TwinStatusTriggerRepository;
import org.twins.core.domain.search.TwinStatusTriggerSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.TwinStatusTriggerGroupField;
import org.twins.core.enums.sort.TwinStatusTriggerSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecificationDirect;
import static org.twins.core.dao.specifications.CommonSpecification.*;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinStatusTriggerSearchService extends EntitySearchService
        <TwinStatusTriggerSearch, TwinStatusTriggerEntity, TwinStatusTriggerSortField, TwinStatusTriggerGroupField> {
    private final TwinStatusTriggerRepository twinStatusTriggerRepository;

    @Override
    public JpaSpecificationExecutor<TwinStatusTriggerEntity> jpaSpecificationExecutor() {
        return twinStatusTriggerRepository;
    }

    @Override
    public TwinStatusTriggerSearch emptySearch() {
        return new TwinStatusTriggerSearch();
    }

    @Override
    protected TwinStatusTriggerEntity newEntity() {
        return new TwinStatusTriggerEntity();
    }

    @Override
    protected Class<TwinStatusTriggerEntity> entityClass() {
        return TwinStatusTriggerEntity.class;
    }

    @Override
    public Specification<TwinStatusTriggerEntity> createFilterSpecification(TwinStatusTriggerSearch search, UUID domainId, Locale locale) throws ServiceException {
        return Specification.allOf(
                checkUuid(domainId, false, true, TwinStatusTriggerEntity.Fields.twinTriggerSpecOnly, TwinTriggerEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, TwinStatusTriggerEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinStatusTriggerEntity.Fields.id),
                checkUuidIn(search.getTwinStatusIdList(), false, true, TwinStatusTriggerEntity.Fields.twinStatusId),
                checkUuidIn(search.getTwinStatusIdExcludeList(), true, true, TwinStatusTriggerEntity.Fields.twinStatusId),
                checkTernary(search.getIncomingElseOutgoing(), TwinStatusTriggerEntity.Fields.incomingElseOutgoing),
                checkUuidIn(search.getTwinTriggerIdList(), false, true, TwinStatusTriggerEntity.Fields.twinTriggerId),
                checkUuidIn(search.getTwinTriggerIdExcludeList(), true, true, TwinStatusTriggerEntity.Fields.twinTriggerId),
                checkTernary(search.getActive(), TwinStatusTriggerEntity.Fields.active),
                checkTernary(search.getAsync(), TwinStatusTriggerEntity.Fields.async)
        );
    }

    @Override
    protected TwinStatusTriggerSortField defaultSortField() {
        return TwinStatusTriggerSortField.order;
    }

    @Override
    public Specification<TwinStatusTriggerEntity> createSortSpecification(TwinStatusTriggerSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case order -> toSortSpecification(ascending, TwinStatusTriggerEntity.Fields.order);
            case active -> toSortSpecification(ascending, TwinStatusTriggerEntity.Fields.active);
            case async -> toSortSpecification(ascending, TwinStatusTriggerEntity.Fields.async);
            case incomingElseOutgoing -> toSortSpecification(ascending, TwinStatusTriggerEntity.Fields.incomingElseOutgoing);
            case twinStatusName -> toSortSpecificationDirect(ascending, locale, TwinStatusTriggerEntity.Fields.twinStatusSpecOnly, TwinStatusEntity.Fields.nameI18nTranslationsSpecOnly);
            case twinTriggerName -> toSortSpecification(ascending, TwinStatusTriggerEntity.Fields.twinTriggerSpecOnly, TwinTriggerEntity.Fields.name);
        };
    }

    @Override
    public String convertToEntityField(TwinStatusTriggerGroupField groupField) throws ServiceException {
        return switch (groupField) {
            case twinStatusId -> TwinStatusTriggerEntity.Fields.twinStatusId;
            case twinTriggerId -> TwinStatusTriggerEntity.Fields.twinTriggerId;
            case active -> TwinStatusTriggerEntity.Fields.active;
            case async -> TwinStatusTriggerEntity.Fields.async;
            case incomingElseOutgoing -> TwinStatusTriggerEntity.Fields.incomingElseOutgoing;
        };
    }

    @Override
    public void mapGroupedField(TwinStatusTriggerEntity entity, TwinStatusTriggerGroupField field, Object o) {
        switch (field) {
            case twinStatusId -> entity.setTwinStatusId((UUID) o);
            case twinTriggerId -> entity.setTwinTriggerId((UUID) o);
            case active -> entity.setActive((Boolean) o);
            case async -> entity.setAsync((Boolean) o);
            case incomingElseOutgoing -> entity.setIncomingElseOutgoing((Boolean) o);
        }
    }
}
