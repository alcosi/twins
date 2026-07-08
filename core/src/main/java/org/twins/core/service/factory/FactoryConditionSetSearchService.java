package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryConditionSetRepository;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.search.FactoryConditionSetSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.FactoryConditionSetGroupField;
import org.twins.core.enums.sort.FactoryConditionSetSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecificationDirect;
import static org.twins.core.dao.specifications.CommonSpecification.*;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class FactoryConditionSetSearchService extends EntitySearchService
        <FactoryConditionSetSearch, TwinFactoryConditionSetEntity, FactoryConditionSetSortField, FactoryConditionSetGroupField> {
    private final TwinFactoryConditionSetRepository twinFactoryConditionSetRepository;

    @Override
    public JpaSpecificationExecutor<TwinFactoryConditionSetEntity> jpaSpecificationExecutor() {
        return twinFactoryConditionSetRepository;
    }

    @Override
    public FactoryConditionSetSearch emptySearch() {
        return new FactoryConditionSetSearch();
    }

    @Override
    protected TwinFactoryConditionSetEntity newEntity() {
        return new TwinFactoryConditionSetEntity();
    }

    @Override
    protected Class<TwinFactoryConditionSetEntity> entityClass() {
        return TwinFactoryConditionSetEntity.class;
    }

    @Override
    public Specification<TwinFactoryConditionSetEntity> createFilterSpecification(FactoryConditionSetSearch search, UUID domainId, Locale locale) {
        return Specification.allOf(
                checkFieldUuid(domainId, TwinFactoryConditionSetEntity.Fields.domainId),
                checkFieldLikeIn(search.getNameLikeList(), false, true, TwinFactoryConditionSetEntity.Fields.name),
                checkFieldLikeIn(search.getNameNotLikeList(), true, true, TwinFactoryConditionSetEntity.Fields.name),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TwinFactoryConditionSetEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinFactoryConditionSetEntity.Fields.description),
                checkUuidIn(search.getIdList(), false, false, TwinFactoryConditionSetEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinFactoryConditionSetEntity.Fields.id),
                checkUuidIn(search.getTwinFactoryIdList(), false, false, TwinFactoryConditionSetEntity.Fields.twinFactoryId),
                checkUuidIn(search.getTwinFactoryIdExcludeList(), true, false, TwinFactoryConditionSetEntity.Fields.twinFactoryId),
                checkTernary(search.getCachable(), TwinFactoryConditionSetEntity.Fields.cachable)
        );
    }

    @Override
    public Specification<TwinFactoryConditionSetEntity> createSortSpecification(FactoryConditionSetSortField sortField, SortDirection sortDirection, Locale locale) {
        if (sortField == null)
            sortField = FactoryConditionSetSortField.createdAt;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case name ->
                    toSortSpecification(ascending, TwinFactoryConditionSetEntity.Fields.name);
            case description ->
                    toSortSpecification(ascending, TwinFactoryConditionSetEntity.Fields.description);
            case createdAt ->
                    toSortSpecification(ascending, TwinFactoryConditionSetEntity.Fields.createdAt);
            case updatedAt ->
                    toSortSpecification(ascending, TwinFactoryConditionSetEntity.Fields.updatedAt);
            case cachable ->
                    toSortSpecification(ascending, TwinFactoryConditionSetEntity.Fields.cachable);
            case createdByUserName ->
                    toSortSpecification(ascending, TwinFactoryConditionSetEntity.Fields.createdByUserSpecOnly, UserEntity.Fields.name);
            case twinFactoryName ->
                    toSortSpecificationDirect(ascending, locale, TwinFactoryConditionSetEntity.Fields.twinFactorySpecOnly, TwinFactoryEntity.Fields.nameI18nTranslationsSpecOnly);
        };
    }

    @Override
    public String convertToEntityField(FactoryConditionSetGroupField groupField) {
        return switch (groupField) {
            case twinFactoryId -> TwinFactoryConditionSetEntity.Fields.twinFactoryId;
            case cachable -> TwinFactoryConditionSetEntity.Fields.cachable;
            case createdByUserId -> TwinFactoryConditionSetEntity.Fields.createdByUserId;
        };
    }

    @Override
    public void mapGroupedField(TwinFactoryConditionSetEntity entity, FactoryConditionSetGroupField field, Object o) {
        switch (field) {
            case twinFactoryId -> entity.setTwinFactoryId((UUID) o);
            case cachable -> entity.setCachable((Boolean) o);
            case createdByUserId -> entity.setCreatedByUserId((UUID) o);
        }
    }
}
