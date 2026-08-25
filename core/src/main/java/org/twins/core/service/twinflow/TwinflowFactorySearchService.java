package org.twins.core.service.twinflow;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dao.twinflow.TwinflowFactoryRepository;
import org.twins.core.domain.search.TwinflowFactorySearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.TwinflowFactoryGroupField;
import org.twins.core.enums.sort.TwinflowFactorySortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecificationDirect;
import static org.twins.core.dao.specifications.CommonSpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinflowFactorySearchService extends EntitySearchService
        <TwinflowFactorySearch, TwinflowFactoryEntity, TwinflowFactorySortField, TwinflowFactoryGroupField> {
    private final TwinflowFactoryRepository twinflowFactoryRepository;

    @Override
    public JpaSpecificationExecutor<TwinflowFactoryEntity> jpaSpecificationExecutor() {
        return twinflowFactoryRepository;
    }

    @Override
    public TwinflowFactorySearch emptySearch() {
        return new TwinflowFactorySearch();
    }

    @Override
    protected TwinflowFactoryEntity newEntity() {
        return new TwinflowFactoryEntity();
    }

    @Override
    protected Class<TwinflowFactoryEntity> entityClass() {
        return TwinflowFactoryEntity.class;
    }

    @Override
    public Specification<TwinflowFactoryEntity> createFilterSpecification(TwinflowFactorySearch search, UUID domainId, Locale locale) throws ServiceException {
        return Specification.allOf(
                checkUuid(domainId, false, true, TwinflowFactoryEntity.Fields.twinflow, TwinflowEntity.Fields.twinClass, TwinClassEntity.Fields.domainId),
                checkUuidIn(search.getIdSet(), false, false, TwinflowFactoryEntity.Fields.id),
                checkUuidIn(search.getIdExcludeSet(), true, true, TwinflowFactoryEntity.Fields.id),
                checkUuidIn(search.getTwinflowIdSet(), false, false, TwinflowFactoryEntity.Fields.twinflowId),
                checkUuidIn(search.getTwinflowIdExcludeSet(), true, true, TwinflowFactoryEntity.Fields.twinflowId),
                checkUuidIn(search.getTwinFactoryIdSet(), false, false, TwinflowFactoryEntity.Fields.twinFactoryId),
                checkUuidIn(search.getTwinFactoryIdExcludeSet(), true, true, TwinflowFactoryEntity.Fields.twinFactoryId),
                checkFieldLikeIn(search.getFactoryLauncherSet(), false, false, TwinflowFactoryEntity.Fields.twinFactoryLauncher),
                checkFieldLikeIn(search.getFactoryLauncherExcludeSet(), true, true, TwinflowFactoryEntity.Fields.twinFactoryLauncher)
        );
    }

    @Override
    public Specification<TwinflowFactoryEntity> createSortSpecification(TwinflowFactorySortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        if (sortField == null)
            sortField = TwinflowFactorySortField.twinFactoryLauncherId;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case twinFactoryLauncherId -> toSortSpecification(ascending, TwinflowFactoryEntity.Fields.twinFactoryLauncher);
            case twinflowName -> toSortSpecificationDirect(ascending, locale, TwinflowFactoryEntity.Fields.twinflow, TwinflowEntity.Fields.nameI18nTranslationsSpecOnly);
            case factoryName -> toSortSpecificationDirect(ascending, locale, TwinflowFactoryEntity.Fields.twinFactory, TwinFactoryEntity.Fields.nameI18nTranslationsSpecOnly);
        };
    }

    @Override
    public String convertToEntityField(TwinflowFactoryGroupField groupField) throws ServiceException {
        return switch (groupField) {
            case twinflowId -> TwinflowFactoryEntity.Fields.twinflowId;
            case factoryId -> TwinflowFactoryEntity.Fields.twinFactoryId;
            case twinFactoryLauncherId -> TwinflowFactoryEntity.Fields.twinFactoryLauncher;
        };
    }

    @Override
    public void mapGroupedField(TwinflowFactoryEntity entity, TwinflowFactoryGroupField field, Object o) {
        switch (field) {
            case twinflowId -> entity.setTwinflowId((UUID) o);
            case factoryId -> entity.setTwinFactoryId((UUID) o);
            case twinFactoryLauncherId -> entity.setTwinFactoryLauncher((org.twins.core.enums.factory.FactoryLauncher) o);
        }
    }
}
