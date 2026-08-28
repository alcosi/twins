package org.twins.core.service.action;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.action.ActionRestrictionReasonEntity;
import org.twins.core.dao.action.ActionRestrictionReasonRepository;
import org.twins.core.domain.search.ActionRestrictionReasonSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.ActionRestrictionReasonGroupField;
import org.twins.core.enums.sort.ActionRestrictionReasonSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NFieldDirect;
import static org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecificationDirect;
import static org.twins.core.dao.specifications.CommonSpecification.*;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class ActionRestrictionReasonSearchService extends EntitySearchService
        <ActionRestrictionReasonSearch, ActionRestrictionReasonEntity, ActionRestrictionReasonSortField, ActionRestrictionReasonGroupField> {
    private final ActionRestrictionReasonRepository actionRestrictionReasonRepository;

    @Override
    public JpaSpecificationExecutor<ActionRestrictionReasonEntity> jpaSpecificationExecutor() {
        return actionRestrictionReasonRepository;
    }

    @Override
    public ActionRestrictionReasonSearch emptySearch() {
        return new ActionRestrictionReasonSearch();
    }

    @Override
    protected ActionRestrictionReasonEntity newEntity() {
        return new ActionRestrictionReasonEntity();
    }

    @Override
    protected Class<ActionRestrictionReasonEntity> entityClass() {
        return ActionRestrictionReasonEntity.class;
    }

    @Override
    public Specification<ActionRestrictionReasonEntity> createFilterSpecification(ActionRestrictionReasonSearch search, UUID domainId, Locale locale) throws ServiceException {
        return Specification.allOf(
                checkFieldUuid(domainId, ActionRestrictionReasonEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, ActionRestrictionReasonEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, ActionRestrictionReasonEntity.Fields.id),
                checkFieldLikeIn(search.getTypeLikeList(), false, true, ActionRestrictionReasonEntity.Fields.type),
                checkFieldLikeIn(search.getTypeNotLikeList(), true, true, ActionRestrictionReasonEntity.Fields.type),
                joinAndSearchByI18NFieldDirect(ActionRestrictionReasonEntity.Fields.descriptionI18nTranslationsSpecOnly, search.getDescriptionLikeList(), locale, false, false),
                joinAndSearchByI18NFieldDirect(ActionRestrictionReasonEntity.Fields.descriptionI18nTranslationsSpecOnly, search.getDescriptionNotLikeList(), locale, true, true)
        );
    }

    @Override
    public Specification<ActionRestrictionReasonEntity> createSortSpecification(ActionRestrictionReasonSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        if (sortField == null)
            sortField = ActionRestrictionReasonSortField.type;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case type ->
                    toSortSpecification(ascending, ActionRestrictionReasonEntity.Fields.type);
            case description ->
                    toSortSpecificationDirect(ascending, locale, ActionRestrictionReasonEntity.Fields.descriptionI18nTranslationsSpecOnly);
        };
    }

    @Override
    public String convertToEntityField(ActionRestrictionReasonGroupField groupField) throws ServiceException {
        return switch (groupField) {
            case type -> ActionRestrictionReasonEntity.Fields.type;
        };
    }

    @Override
    public void mapGroupedField(ActionRestrictionReasonEntity entity, ActionRestrictionReasonGroupField field, Object o) {
        switch (field) {
            case type -> entity.setType((String) o);
        }
    }
}
