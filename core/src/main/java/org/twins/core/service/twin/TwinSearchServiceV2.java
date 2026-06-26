package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.twins.core.dao.i18n.specifications.I18nSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.apiuser.DBUMembershipCheck;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.twin.sorter.TwinSorter;
import org.twins.core.service.EntitySearchService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DBUService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.usergroup.UserGroupService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.toSortSpecification;
import static org.twins.core.dao.specifications.twin.TwinSpecification.*;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
public class TwinSearchServiceV2 extends EntitySearchService<BasicSearch, TwinEntity, UUID, UUID> {
    private final TwinRepository twinRepository;
    private final UserGroupService userGroupService;
    private final TwinClassFieldService twinClassFieldService;
    private final PermissionService permissionService;
    @Lazy
    private final FeaturerService featurerService;
    @Lazy
    private final AuthService authService;
    private final DBUService dbuService;

    @Override
    public JpaSpecificationExecutor<TwinEntity> jpaSpecificationExecutor() {
        return twinRepository;
    }

    @Override
    public BasicSearch emptySearch() {
        return new BasicSearch();
    }

    @Override
    protected Class<TwinEntity> entityClass() {
        return TwinEntity.class;
    }

    @Override
    protected TwinEntity newEntity() {
        return new TwinEntity();
    }

    @Override
    public Specification<TwinEntity> createFilterSpecification(BasicSearch search, UUID domainId, Locale locale) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        userGroupService.loadGroupsForCurrentUser();
        UUID businessAccountId = apiUser.getBusinessAccountId();
        UUID userId = apiUser.getUser().getId();
        //todo create filter by basicSearch.getExtendsTwinClassIdList()
        Specification<TwinEntity> specification = createTwinEntityBasicSearchSpecification(search, userId);

        if (permissionService.currentUserHasPermission(Permissions.DOMAIN_TWINS_VIEW_ALL) || !search.isCheckViewPermission()) {
            specification = specification
                    .and(checkFieldUuid(apiUser.getDomainId(), TwinEntity.Fields.twinClass, TwinClassEntity.Fields.domainId))
                    .and(checkClassId(search.getTwinClassIdList()));
        } else {
            detectSystemClassSearchCheck(search);
            specification = specification
                    .and(checkPermissions(domainId, businessAccountId, userId, apiUser.getUser().getUserGroupsFootprint()))
                    .and(checkClass(search.getTwinClassIdList(), apiUser, search.getDbuMembershipCheck()));
        }

        //HEAD TWIN CHECK
        if (null != search.getHeadSearch() && !search.getHeadSearch().isEmpty())
            specification = specification.and(
                    checkHeadTwin(
                            createTwinEntityBasicSearchSpecification(search.getHeadSearch(), userId),
                            search.getHeadSearch()
                    ));

        //CHILDREN TWINS CHECK
        if (null != search.getChildrenSearch() && !search.getChildrenSearch().isEmpty())
            specification = specification.and(
                    checkChildrenTwins(
                            createTwinEntityBasicSearchSpecification(search.getChildrenSearch(), userId),
                            search.getChildrenSearch()
                    ));

        return specification;
    }

    @Override
    public Specification<TwinEntity> createSortSpecification(UUID twinClassFieldId, SortDirection sortDirection, Locale locale) throws ServiceException {
        if (twinClassFieldId == null)
            return (root, query, cb) -> cb.conjunction();
        boolean ascending = sortDirection != SortDirection.DESC;

        var basicField = TwinEntity.BasicField.convertOrNull(twinClassFieldId);
        if (basicField != null) {
            switch (basicField) {
                case NAME:
                    return toSortSpecification(ascending, TwinEntity.Fields.name);
                case DESCRIPTION:
                    return toSortSpecification(ascending, TwinEntity.Fields.description);
                case CREATED_AT:
                    return toSortSpecification(ascending, TwinEntity.Fields.createdAt);
                case EXTERNAL_ID:
                    return toSortSpecification(ascending, TwinEntity.Fields.externalId);
                case TWIN_CLASS_ID:
                    return I18nSpecification.toSortSpecification(ascending, locale, TwinEntity.Fields.twinClass, TwinClassEntity.Fields.nameI18nSpecOnly);
                case TWIN_STATUS_ID:
                    return I18nSpecification.toSortSpecification(ascending, locale, TwinEntity.Fields.twinStatus, TwinStatusEntity.Fields.nameI18nSpecOnly);
                case OWNER_USER_ID:
                    return toSortSpecification(ascending, TwinEntity.Fields.ownerUser, UserEntity.Fields.name);
                case ASSIGNEE_USER_ID:
                    return toSortSpecification(ascending, TwinEntity.Fields.assignerUser, UserEntity.Fields.name);
                case CREATOR_USER_ID:
                    return toSortSpecification(ascending, TwinEntity.Fields.createdByUser, UserEntity.Fields.name);
                default:
                    throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "Field " + twinClassFieldId + " is not groupable");
            }
        }

        // Default: dynamic field via TwinSorter featurer
        TwinClassFieldEntity twinClassField = twinClassFieldService.findEntitySafe(twinClassFieldId);
        TwinSorter fieldSorter = featurerService.getFeaturer(twinClassField.getTwinSorterFeaturerId(), TwinSorter.class);
        var sortFn = fieldSorter.createSort(twinClassField.getTwinSorterParams(), twinClassField, toHibernateDirection(sortDirection));
        return sortFn.apply((root, query, cb) -> cb.conjunction());
    }

    @Override
    public String convertToEntityField(UUID twinClassFieldId) throws ServiceException {
        var basicField = TwinEntity.BasicField.convertOrNull(twinClassFieldId);
        if (basicField == TwinEntity.BasicField.HEAD_TWIN_ID
                || basicField == TwinEntity.BasicField.TWIN_CLASS_ID
                || basicField == TwinEntity.BasicField.TWIN_STATUS_ID
                || basicField == TwinEntity.BasicField.OWNER_USER_ID
                || basicField == TwinEntity.BasicField.ASSIGNEE_USER_ID
                || basicField == TwinEntity.BasicField.CREATOR_USER_ID)
            return basicField.getName();
        throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "Field " + twinClassFieldId + " is not groupable");
    }

    @Override
    public void mapGroupedField(TwinEntity entity, UUID field, Object value) {
        if (field.equals(SystemIds.TwinClassField.Base.TWIN_CLASS_ID))
            entity.setTwinClassId((UUID) value);
        else if (field.equals(SystemIds.TwinClassField.Base.STATUS_ID))
            entity.setTwinStatusId((UUID) value);
        else if (field.equals(SystemIds.TwinClassField.Base.OWNER_USER_ID))
            entity.setOwnerUserId((UUID) value);
        else if (field.equals(SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID))
            entity.setAssignerUserId((UUID) value);
        else if (field.equals(SystemIds.TwinClassField.Base.CREATOR_USER_ID))
            entity.setCreatedByUserId((UUID) value);
        else if (field.equals(SystemIds.TwinClassField.Base.HEAD_ID))
            entity.setHeadTwinId((UUID) value);
    }

    private static org.hibernate.query.SortDirection toHibernateDirection(SortDirection direction) {
        return direction == SortDirection.DESC
                ? org.hibernate.query.SortDirection.DESCENDING
                : org.hibernate.query.SortDirection.ASCENDING;
    }

    protected void detectSystemClassSearchCheck(BasicSearch basicSearch) throws ServiceException {
        if (basicSearch.getDbuMembershipCheck() != null) {
            return;
        }
        if (CollectionUtils.isEmpty(basicSearch.getTwinClassIdList())) {
            basicSearch
                    .addTwinClassId(List.of(SystemIds.TwinClass.BUSINESS_ACCOUNT, SystemIds.TwinClass.USER), true)
                    .setDbuMembershipCheck(DBUMembershipCheck.BLOCKED);
            return;
        }
        DBUMembershipCheck detectedCheck = DBUMembershipCheck.BLOCKED;
        for (UUID twinClassId : basicSearch.getTwinClassIdList()) {
            detectedCheck = dbuService.detectSystemTwinsDBUMembershipCheck(twinClassId);
            if (detectedCheck != DBUMembershipCheck.BLOCKED && basicSearch.getTwinClassIdList().size() > 1) {
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_INCORRECT, "mixed search is not allowed");
            }
        }
        basicSearch.setDbuMembershipCheck(detectedCheck);
    }

    public Map<UUID, Long> countByGroupFields(BasicSearch basicSearch, TwinEntity.BasicField basicField) throws ServiceException {
        return countByGroupFields(basicSearch, basicField.getId());
    }


}
