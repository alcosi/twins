package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.i18n.specifications.I18nSpecification;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twin.TwinStatusRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.TwinStatusSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.TwinStatusGroupField;
import org.twins.core.enums.sort.TwinStatusSortField;
import org.twins.core.enums.status.StatusType;
import org.twins.core.service.EntitySearchService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NFieldDirect;
import static org.twins.core.dao.specifications.CommonSpecification.*;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinStatusSearchService extends EntitySearchService
        <TwinStatusSearch, TwinStatusEntity, TwinStatusSortField, TwinStatusGroupField> {
    private final TwinStatusRepository twinStatusRepository;
    private final TwinClassService twinClassService;

    @Override
    public JpaSpecificationExecutor<TwinStatusEntity> jpaSpecificationExecutor() {
        return twinStatusRepository;
    }

    @Override
    public TwinStatusSearch emptySearch() {
        return new TwinStatusSearch();
    }

    @Override
    protected TwinStatusEntity newEntity() {
        return new TwinStatusEntity();
    }

    @Override
    protected Class<TwinStatusEntity> entityClass() {
        return TwinStatusEntity.class;
    }

    @Override
    public Specification<TwinStatusEntity> createFilterSpecification(TwinStatusSearch search, UUID domainId, Locale locale) throws ServiceException {
        return Specification.allOf(
                checkFieldUuid(domainId, TwinStatusEntity.Fields.twinClass, TwinClassEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, TwinStatusEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinStatusEntity.Fields.id),
                checkTwinClassAndInheritable(twinClassService.loadExtends(search.getTwinClassIdMap()), false, TwinStatusEntity.Fields.twinClassId, TwinStatusEntity.Fields.inheritable),
                checkTwinClassAndInheritable(twinClassService.loadExtends(search.getTwinClassIdExcludeMap()), true, TwinStatusEntity.Fields.twinClassId, TwinStatusEntity.Fields.inheritable),
                checkTernary(search.getInheritable(), TwinStatusEntity.Fields.inheritable),
                checkFieldLikeIn(search.getKeyLikeList(), false, true, TwinStatusEntity.Fields.key),
                checkFieldLikeIn(search.getKeyNotLikeList(), true, true, TwinStatusEntity.Fields.key),
                joinAndSearchByI18NFieldDirect(TwinStatusEntity.Fields.nameI18nTranslationsSpecOnly, search.getNameI18nLikeList(), locale, true, false),
                joinAndSearchByI18NFieldDirect(TwinStatusEntity.Fields.nameI18nTranslationsSpecOnly, search.getNameI18nNotLikeList(), locale, true, true),
                joinAndSearchByI18NFieldDirect(TwinStatusEntity.Fields.descriptionI18nTranslationsSpecOnly, search.getDescriptionI18nLikeList(), locale, true, false),
                joinAndSearchByI18NFieldDirect(TwinStatusEntity.Fields.descriptionI18nTranslationsSpecOnly, search.getDescriptionI18nNotLikeList(), locale, true, true)
        );
    }

    @Override
    public Specification<TwinStatusEntity> createSortSpecification(TwinStatusSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        if (sortField == null)
            sortField = TwinStatusSortField.key;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case key -> toSortSpecification(ascending, TwinStatusEntity.Fields.key);
            case name -> I18nSpecification.toSortSpecificationDirect(ascending, locale, TwinStatusEntity.Fields.nameI18nTranslationsSpecOnly);
            case description -> I18nSpecification.toSortSpecificationDirect(ascending, locale, TwinStatusEntity.Fields.descriptionI18nTranslationsSpecOnly);
            case inheritable -> toSortSpecification(ascending, TwinStatusEntity.Fields.inheritable);
            case backgroundColor -> toSortSpecification(ascending, TwinStatusEntity.Fields.backgroundColor);
            case fontColor -> toSortSpecification(ascending, TwinStatusEntity.Fields.fontColor);
            case type -> toSortSpecification(ascending, TwinStatusEntity.Fields.type);
            case twinClassName -> I18nSpecification.toSortSpecificationDirect(ascending, locale, TwinStatusEntity.Fields.twinClass, TwinClassEntity.Fields.nameI18nTranslationsSpecOnly);
        };
    }

    @Override
    public String convertToEntityField(TwinStatusGroupField groupField) {
        return switch (groupField) {
            case twinClassId -> TwinStatusEntity.Fields.twinClassId;
            case inheritable -> TwinStatusEntity.Fields.inheritable;
            case type -> TwinStatusEntity.Fields.type;
        };
    }

    @Override
    public void mapGroupedField(TwinStatusEntity entity, TwinStatusGroupField field, Object o) {
        switch (field) {
            case twinClassId -> entity.setTwinClassId((UUID) o);
            case inheritable -> entity.setInheritable((Boolean) o);
            case type -> entity.setType(o instanceof StatusType ? (StatusType) o : StatusType.valueOf((String) o));
        }
    }
}
