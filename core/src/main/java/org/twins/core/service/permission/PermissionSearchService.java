package org.twins.core.service.permission;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dao.permission.PermissionRepository;
import org.twins.core.domain.search.PermissionSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.PermissionGroupField;
import org.twins.core.enums.sort.PermissionSortField;
import org.twins.core.service.EntitySearchService;

import java.util.Locale;
import java.util.UUID;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NFieldDirect;
import static org.twins.core.dao.i18n.specifications.I18nSpecification.toSortSpecificationDirect;
import static org.twins.core.dao.specifications.CommonSpecification.*;
import static org.twins.core.dao.specifications.permission.PermissionSpecification.checkDomainId;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class PermissionSearchService extends EntitySearchService
        <PermissionSearch, PermissionEntity, PermissionSortField, PermissionGroupField> {

    private final PermissionRepository permissionRepository;

    @Override
    public JpaSpecificationExecutor<PermissionEntity> jpaSpecificationExecutor() {
        return permissionRepository;
    }

    @Override
    public PermissionSearch emptySearch() {
        return new PermissionSearch();
    }

    @Override
    protected PermissionEntity newEntity() {
        return new PermissionEntity();
    }

    @Override
    protected Class<PermissionEntity> entityClass() {
        return PermissionEntity.class;
    }

    @Override
    public Specification<PermissionEntity> createFilterSpecification(PermissionSearch search, UUID domainId, Locale locale) {
        return Specification.allOf(
                checkDomainId(domainId),
                checkFieldLikeContainsIn(search.getKeyLikeList(), false, true, PermissionEntity.Fields.key),
                checkFieldLikeContainsIn(search.getKeyNotLikeList(), true, true, PermissionEntity.Fields.key),
                checkUuidIn(search.getIdList(), false, true, PermissionEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, PermissionEntity.Fields.id),
                joinAndSearchByI18NFieldDirect(PermissionEntity.Fields.nameI18nTranslationsSpecOnly, search.getNameI18nLikeList(), locale, false, false),
                joinAndSearchByI18NFieldDirect(PermissionEntity.Fields.nameI18nTranslationsSpecOnly, search.getNameI18nNotLikeList(), locale, true, true),
                joinAndSearchByI18NFieldDirect(PermissionEntity.Fields.descriptionI18nTranslationsSpecOnly, search.getDescriptionI18nLikeList(), locale, false, false),
                joinAndSearchByI18NFieldDirect(PermissionEntity.Fields.descriptionI18nTranslationsSpecOnly, search.getDescriptionI18nNotLikeList(), locale, true, true),
                checkUuidIn(search.getGroupIdList(), false, true, PermissionEntity.Fields.permissionGroupId),
                checkUuidIn(search.getGroupIdExcludeList(), true, true, PermissionEntity.Fields.permissionGroupId)
        );
    }

    @Override
    public Specification<PermissionEntity> createSortSpecification(PermissionSortField sortField, SortDirection sortDirection, Locale locale) throws ServiceException {
        if (sortField == null)
            sortField = PermissionSortField.key;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case key -> toSortSpecification(ascending, PermissionEntity.Fields.key);
            case name -> toSortSpecificationDirect(ascending, locale, PermissionEntity.Fields.nameI18nTranslationsSpecOnly);
            case description -> toSortSpecificationDirect(ascending, locale, PermissionEntity.Fields.descriptionI18nTranslationsSpecOnly);
            case groupName -> toSortSpecification(ascending, PermissionEntity.Fields.permissionGroupSpecOnly, PermissionGroupEntity.Fields.name);
        };
    }

    @Override
    public String convertToEntityField(PermissionGroupField groupField) {
        return switch (groupField) {
            case groupId -> PermissionEntity.Fields.permissionGroupId;
        };
    }

    @Override
    public void mapGroupedField(PermissionEntity entity, PermissionGroupField field, Object o) {
        switch (field) {
            case groupId -> entity.setPermissionGroupId((UUID) o);
        }
    }
}
