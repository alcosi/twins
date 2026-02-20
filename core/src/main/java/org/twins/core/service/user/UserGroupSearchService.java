package org.twins.core.service.user;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.UserGroupSearch;
import org.twins.core.service.auth.AuthService;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.usergroup.UserGroupSpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class UserGroupSearchService {
    private final AuthService authService;
    private final UserGroupRepository userGroupRepository;

    public PaginationResult<UserGroupEntity> findUserGroupsForDomain(UserGroupSearch search, SimplePagination pagination) throws ServiceException {
        Specification<UserGroupEntity> spec = createUserGroupSearchSpecification(search);
        Page<UserGroupEntity> ret = userGroupRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<UserGroupEntity> createUserGroupSearchSpecification(UserGroupSearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return Specification.allOf(
                checkFieldUuid(apiUser.getDomainId(), UserGroupEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, UserGroupEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, UserGroupEntity.Fields.id),
                joinAndSearchByI18NField(UserGroupEntity.Fields.nameI18N, search.getNameI18NLikeList(), apiUser.getLocale(), true, false),
                joinAndSearchByI18NField(UserGroupEntity.Fields.nameI18N, search.getNameI18nNotLikeList(), apiUser.getLocale(), true, true),
                joinAndSearchByI18NField(UserGroupEntity.Fields.descriptionI18N, search.getDescriptionI18NLikeList(), apiUser.getLocale(), true, false),
                joinAndSearchByI18NField(UserGroupEntity.Fields.descriptionI18N, search.getDescriptionI18NNotLikeList(), apiUser.getLocale(), true, true),
                checkFieldLikeIn(search.getTypeList(), false, true, UserGroupEntity.Fields.userGroupTypeId),
                checkFieldLikeIn(search.getTypeExcludeList(), true, true, UserGroupEntity.Fields.userGroupTypeId));
    }
}
