package org.twins.core.service.usergroup;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.usergroup.UserGroupInvolveActAsUserEntity;
import org.twins.core.dao.usergroup.UserGroupInvolveActAsUserRepository;
import org.twins.core.dto.rest.usergroup.UserGroupInvolveActAsUserSearchDTOv1;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.usergroup.UserGroupInvolveActAsUserSpecification.checkUuid;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@AllArgsConstructor
public class UserGroupInvolveActAsUserSearchService {
    private final UserGroupInvolveActAsUserRepository userGroupInvolveActAsUserRepository;
    private final AuthService authService;

    public Specification<UserGroupInvolveActAsUserEntity> createUserGroupInvolveActAsUserEntitySearchSpecification(UserGroupInvolveActAsUserSearchDTOv1 userGroupInvolveActAsUserSearch) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        return checkUuid(UserGroupInvolveActAsUserEntity.Fields.domainId, domainId)
                .and(checkUuidIn(userGroupInvolveActAsUserSearch.getIdList(), false, false, UserGroupInvolveActAsUserEntity.Fields.id))
                .and(checkUuidIn(userGroupInvolveActAsUserSearch.getIdExcludeList(), true, true, UserGroupInvolveActAsUserEntity.Fields.id))
                .and(checkUuidIn(userGroupInvolveActAsUserSearch.getMachineUserIdList(), false, false, UserGroupInvolveActAsUserEntity.Fields.machineUserId))
                .and(checkUuidIn(userGroupInvolveActAsUserSearch.getMachineUserIdExcludeList(), true, true, UserGroupInvolveActAsUserEntity.Fields.machineUserId))
                .and(checkUuidIn(userGroupInvolveActAsUserSearch.getUserGroupIdList(), false, false, UserGroupInvolveActAsUserEntity.Fields.userGroupId))
                .and(checkUuidIn(userGroupInvolveActAsUserSearch.getUserGroupIdExcludeList(), true, true, UserGroupInvolveActAsUserEntity.Fields.userGroupId));
    }

    public PaginationResult<UserGroupInvolveActAsUserEntity> findUserGroupInvolveActAsUsers(UserGroupInvolveActAsUserSearchDTOv1 userGroupInvolveActAsUserSearch, SimplePagination pagination) throws ServiceException {
        if (userGroupInvolveActAsUserSearch == null)
            userGroupInvolveActAsUserSearch = new UserGroupInvolveActAsUserSearchDTOv1(); //no filters
        Page<UserGroupInvolveActAsUserEntity> userGroupInvolveActAsUserList = userGroupInvolveActAsUserRepository
                .findAll(createUserGroupInvolveActAsUserEntitySearchSpecification(userGroupInvolveActAsUserSearch),
                        PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(userGroupInvolveActAsUserList, pagination);
    }
}
