package org.twins.core.service.twintrigger;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.dao.FeaturerEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dao.trigger.TwinTriggerRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.TwinTriggerSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.TwinTriggerGroupField;
import org.twins.core.enums.sort.TwinTriggerSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecificationDirect;
import static org.twins.core.dao.specifications.CommonSpecification.*;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
@Service
public class TwinTriggerSearchService extends EntitySearchService
        <TwinTriggerSearch, TwinTriggerEntity, TwinTriggerSortField, TwinTriggerGroupField> {
    private final TwinTriggerRepository twinTriggerRepository;

    @Override
    public JpaSpecificationExecutor<TwinTriggerEntity> jpaSpecificationExecutor() {
        return twinTriggerRepository;
    }

    @Override
    public TwinTriggerSearch emptySearch() {
        return new TwinTriggerSearch();
    }

    @Override
    protected TwinTriggerEntity newEntity() {
        return new TwinTriggerEntity();
    }

    @Override
    protected Class<TwinTriggerEntity> entityClass() {
        return TwinTriggerEntity.class;
    }

    @Override
    public Specification<TwinTriggerEntity> createFilterSpecification(TwinTriggerSearch search, UUID domainId, Locale locale) throws ServiceException {
        return Specification.allOf(
                checkUuid(domainId, false, true, TwinTriggerEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, TwinTriggerEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinTriggerEntity.Fields.id),
                checkIntegerIn(search.getTriggerFeaturerIdList(), false, TwinTriggerEntity.Fields.twinTriggerFeaturerId),
                checkIntegerIn(search.getTriggerFeaturerIdExcludeList(), true, TwinTriggerEntity.Fields.twinTriggerFeaturerId),
                checkTernary(search.getActive(), TwinTriggerEntity.Fields.active),
                checkFieldLikeIn(search.getNameLikeList(), false, true, TwinTriggerEntity.Fields.name),
                checkUuidIn(search.getJobTwinClassIdList(), false, true, TwinTriggerEntity.Fields.jobTwinClassId),
                checkUuidIn(search.getJobTwinClassIdExcludeList(), true, true, TwinTriggerEntity.Fields.jobTwinClassId)
        );
    }

    @Override
    protected TwinTriggerSortField defaultSortField() {
        return TwinTriggerSortField.name;
    }

    @Override
    public Specification<TwinTriggerEntity> createSortSpecification(TwinTriggerSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case name -> toSortSpecification(ascending, TwinTriggerEntity.Fields.name);
            case description -> toSortSpecification(ascending, TwinTriggerEntity.Fields.description);
            case active -> toSortSpecification(ascending, TwinTriggerEntity.Fields.active);
            case jobTwinClassName -> toSortSpecificationDirect(ascending, locale, TwinTriggerEntity.Fields.jobTwinClassSpecOnly, TwinClassEntity.Fields.nameI18nTranslationsSpecOnly);
            case triggerFeaturerName -> toSortSpecification(ascending, TwinTriggerEntity.Fields.twinTriggerFeaturerSpecOnly, FeaturerEntity.Fields.name);
        };
    }

    @Override
    public String convertToEntityField(TwinTriggerGroupField groupField) throws ServiceException {
        return switch (groupField) {
            case triggerFeaturerId -> TwinTriggerEntity.Fields.twinTriggerFeaturerId;
            case active -> TwinTriggerEntity.Fields.active;
            case jobTwinClassId -> TwinTriggerEntity.Fields.jobTwinClassId;
        };
    }

    @Override
    public void mapGroupedField(TwinTriggerEntity entity, TwinTriggerGroupField field, Object o) {
        switch (field) {
            case triggerFeaturerId -> entity.setTwinTriggerFeaturerId((Integer) o);
            case active -> entity.setActive((Boolean) o);
            case jobTwinClassId -> entity.setJobTwinClassId((UUID) o);
        }
    }
}
