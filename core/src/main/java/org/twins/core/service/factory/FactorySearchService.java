package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryRepository;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.search.FactorySearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.FactoryGroupField;
import org.twins.core.enums.sort.FactorySortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NFieldDirect;
import static org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecificationDirect;
import static org.twins.core.dao.specifications.CommonSpecification.*;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class FactorySearchService extends EntitySearchService
        <FactorySearch, TwinFactoryEntity, FactorySortField, FactoryGroupField> {
    private final TwinFactoryRepository twinFactoryRepository;

    @Override
    public JpaSpecificationExecutor<TwinFactoryEntity> jpaSpecificationExecutor() {
        return twinFactoryRepository;
    }

    @Override
    public FactorySearch emptySearch() {
        return new FactorySearch();
    }

    @Override
    protected TwinFactoryEntity newEntity() {
        return new TwinFactoryEntity();
    }

    @Override
    protected Class<TwinFactoryEntity> entityClass() {
        return TwinFactoryEntity.class;
    }

    @Override
    public Specification<TwinFactoryEntity> createFilterSpecification(FactorySearch search, UUID domainId, Locale locale) {
        return Specification.allOf(
                checkFieldUuid(domainId, TwinFactoryEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, TwinFactoryEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinFactoryEntity.Fields.id),
                checkFieldLikeIn(search.getKeyLikeList(), false, false, TwinFactoryEntity.Fields.key),
                checkFieldLikeIn(search.getKeyNotLikeList(), true, true, TwinFactoryEntity.Fields.key),
                joinAndSearchByI18NFieldDirect(TwinFactoryEntity.Fields.nameI18nTranslationsSpecOnly, search.getNameLikeList(), locale, true, false),
                joinAndSearchByI18NFieldDirect(TwinFactoryEntity.Fields.nameI18nTranslationsSpecOnly, search.getNameNotLikeList(), locale, true, true),
                joinAndSearchByI18NFieldDirect(TwinFactoryEntity.Fields.descriptionI18nTranslationsSpecOnly, search.getDescriptionLikeList(), locale, true, false),
                joinAndSearchByI18NFieldDirect(TwinFactoryEntity.Fields.descriptionI18nTranslationsSpecOnly, search.getDescriptionNotLikeList(), locale, true, true)
        );
    }

    @Override
    public Specification<TwinFactoryEntity> createSortSpecification(FactorySortField sortField, SortDirection sortDirection, Locale locale) {
        if (sortField == null)
            sortField = FactorySortField.createdAt;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case key ->
                    toSortSpecification(ascending, TwinFactoryEntity.Fields.key);
            case name ->
                    toSortSpecificationDirect(ascending, locale, TwinFactoryEntity.Fields.nameI18nTranslationsSpecOnly);
            case description ->
                    toSortSpecificationDirect(ascending, locale, TwinFactoryEntity.Fields.descriptionI18nTranslationsSpecOnly);
            case createdAt ->
                    toSortSpecification(ascending, TwinFactoryEntity.Fields.createdAt);
            case createdByUserName ->
                    toSortSpecification(ascending, TwinFactoryEntity.Fields.createdByUserSpecOnly, UserEntity.Fields.name);
        };
    }

    @Override
    public String convertToEntityField(FactoryGroupField groupField) {
        return switch (groupField) {
            case createdByUserId -> TwinFactoryEntity.Fields.createdByUserId;
            case domainId -> TwinFactoryEntity.Fields.domainId;
        };
    }

    @Override
    public void mapGroupedField(TwinFactoryEntity entity, FactoryGroupField field, Object o) {
        switch (field) {
            case createdByUserId -> entity.setCreatedByUserId((UUID) o);
            case domainId -> entity.setDomainId((UUID) o);
        }
    }
}
