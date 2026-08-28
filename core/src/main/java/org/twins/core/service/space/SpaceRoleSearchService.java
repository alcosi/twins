package org.twins.core.service.space;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.space.SpaceRoleEntity;
import org.twins.core.dao.space.SpaceRoleRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.SpaceRoleSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.SpaceRoleGroupField;
import org.twins.core.enums.sort.SpaceRoleSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NFieldDirect;
import static org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecificationDirect;
import static org.twins.core.dao.specifications.CommonSpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class SpaceRoleSearchService extends EntitySearchService
        <SpaceRoleSearch, SpaceRoleEntity, SpaceRoleSortField, SpaceRoleGroupField> {
    private final SpaceRoleRepository spaceRoleRepository;

    @Override
    public JpaSpecificationExecutor<SpaceRoleEntity> jpaSpecificationExecutor() {
        return spaceRoleRepository;
    }

    @Override
    public SpaceRoleSearch emptySearch() {
        return new SpaceRoleSearch();
    }

    @Override
    protected SpaceRoleEntity newEntity() {
        return new SpaceRoleEntity();
    }

    @Override
    protected Class<SpaceRoleEntity> entityClass() {
        return SpaceRoleEntity.class;
    }

    @Override
    public Specification<SpaceRoleEntity> createFilterSpecification(SpaceRoleSearch search, UUID domainId, Locale locale) throws ServiceException {
        return Specification.allOf(
                checkUuidIn(Set.of(domainId), false, false, SpaceRoleEntity.Fields.twinClass, TwinClassEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, true, SpaceRoleEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, SpaceRoleEntity.Fields.id),
                checkUuidIn(search.getTwinClassIdList(), false, true, SpaceRoleEntity.Fields.twinClassId),
                checkUuidIn(search.getTwinClassIdExcludeList(), true, false, SpaceRoleEntity.Fields.twinClassId),
                checkUuidIn(search.getBusinessAccountIdList(), false, true, SpaceRoleEntity.Fields.businessAccountId),
                checkUuidIn(search.getBusinessAccountIdExcludeList(), true, false, SpaceRoleEntity.Fields.businessAccountId),
                checkFieldLikeIn(search.getKeyLikeList(), false, true, SpaceRoleEntity.Fields.key),
                checkFieldLikeIn(search.getKeyNotLikeList(), true, true, SpaceRoleEntity.Fields.key),
                joinAndSearchByI18NFieldDirect(SpaceRoleEntity.Fields.nameI18nTranslationsSpecOnly, search.getNameI18nLikeList(), locale, false, false),
                joinAndSearchByI18NFieldDirect(SpaceRoleEntity.Fields.nameI18nTranslationsSpecOnly, search.getNameI18nNotLikeList(), locale, true, true),
                joinAndSearchByI18NFieldDirect(SpaceRoleEntity.Fields.descriptionI18nTranslationsSpecOnly, search.getDescriptionI18nLikeList(), locale, false, false),
                joinAndSearchByI18NFieldDirect(SpaceRoleEntity.Fields.descriptionI18nTranslationsSpecOnly, search.getDescriptionI18nNotLikeList(), locale, true, true)
        );
    }

    @Override
    public Specification<SpaceRoleEntity> createSortSpecification(SpaceRoleSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        if (sortField == null)
            sortField = SpaceRoleSortField.key;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case key -> toSortSpecification(ascending, SpaceRoleEntity.Fields.key);
            case name -> toSortSpecificationDirect(ascending, locale, SpaceRoleEntity.Fields.nameI18nTranslationsSpecOnly);
            case description -> toSortSpecificationDirect(ascending, locale, SpaceRoleEntity.Fields.descriptionI18nTranslationsSpecOnly);
            case twinClassName -> toSortSpecificationDirect(ascending, locale, SpaceRoleEntity.Fields.twinClass, TwinClassEntity.Fields.nameI18nTranslationsSpecOnly);
            case businessAccountName -> toSortSpecification(ascending, SpaceRoleEntity.Fields.businessAccount, BusinessAccountEntity.Fields.name);
        };
    }

    @Override
    public String convertToEntityField(SpaceRoleGroupField groupField) throws ServiceException {
        return switch (groupField) {
            case twinClassId -> SpaceRoleEntity.Fields.twinClassId;
            case businessAccountId -> SpaceRoleEntity.Fields.businessAccountId;
        };
    }

    @Override
    public void mapGroupedField(SpaceRoleEntity entity, SpaceRoleGroupField field, Object o) {
        switch (field) {
            case twinClassId -> entity.setTwinClassId((UUID) o);
            case businessAccountId -> entity.setBusinessAccountId((UUID) o);
        }
    }
}
