package org.twins.core.featurer.usergroup.slugger;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupMapEntity;
import org.twins.core.dao.user.UserGroupRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.EntitySmartService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = 2002,
        name = "SluggerDomainScopeBusinessAccountManage",
        description = "")
public class SluggerDomainScopeBusinessAccountManage extends Slugger {

    private final String type = "";

    @Lazy
    @Autowired
    EntitySmartService entitySmartService;

    @Lazy
    @Autowired
    UserGroupRepository userGroupRepository;

    @Override
    protected UserGroupEntity checkConfigAndGetGroup(Properties properties, UserGroupMapEntity userGroupMapEntity) throws ServiceException {
        checkUserGroupBusinessAccountEmpty(userGroupMapEntity.getUserGroup());
        ApiUser apiUser = authService.getApiUser();
        if (userGroupMapEntity.getBusinessAccountId() == null) {
            log.warn(userGroupMapEntity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect config. Group is " + userGroupMapEntity.getUserGroup().getUserGroupTypeId() + ". Missing business_account in user_group_map");
            return null;
        } else if (apiUser.isBusinessAccountSpecified() && !apiUser.getBusinessAccountId().equals(userGroupMapEntity.getBusinessAccountId())) {
            return null;
        } else
            return userGroupMapEntity.getUserGroup();
    }

    @Override
    protected UserGroupMapEntity enterGroup(Properties properties, UserGroupEntity userGroup, UUID userId, ApiUser apiUser) throws ServiceException {
        if (!apiUser.isBusinessAccountSpecified()) {
            log.warn(userGroup.easyLog(EasyLoggable.Level.NORMAL) + " can not be entered by userId[" + userId + "]. Business account is unknown");
            return null;
        }
        return new UserGroupMapEntity()
                .setUserGroupId(userGroup.getId())
                .setUserGroup(userGroup)
                .setUserId(userId)
                .setBusinessAccountId(apiUser.getBusinessAccountId())
                .setBusinessAccount(apiUser.getBusinessAccount())
                .setAddedByUserId(apiUser.getUser().getId())
                .setAddedByUser(apiUser.getUser())
                .setAddedAt(Timestamp.from(Instant.now()));
    }

    @Override
    protected void deleteDomainBusinessAccount(Properties properties, UserGroupEntity userGroup) throws ServiceException {
        List<UUID> usersToDelete = userGroupMapRepository.findAllByBusinessAccountIdAndDomainIdAndTypes(userGroup.getBusinessAccountId(), userGroup.getDomainId(), List.of(userGroup.getUserGroupTypeId()));
        entitySmartService.deleteAllAndLog(usersToDelete, userGroupMapRepository);
    }

    @Override
    protected void deleteDomain(Properties properties, UserGroupEntity userGroup) throws ServiceException {

    }

    @Override
    protected void deleteBusinessAccount(Properties properties, UserGroupEntity userGroup) throws ServiceException {

    }
}
