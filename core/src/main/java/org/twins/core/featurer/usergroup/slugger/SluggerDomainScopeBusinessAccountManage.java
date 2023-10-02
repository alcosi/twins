package org.twins.core.featurer.usergroup.slugger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupMapEntity;
import org.twins.core.domain.ApiUser;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = 2002,
        name = "SluggerDomainScopeBusinessAccountManage",
        description = "")
public class SluggerDomainScopeBusinessAccountManage extends Slugger {
    @Override
    protected UserGroupEntity checkConfigAndGetGroup(Properties properties, UserGroupMapEntity userGroupMapEntity) throws ServiceException {
        checkUserGroupBusinessAccountEmpty(userGroupMapEntity.getUserGroup());
        if (userGroupMapEntity.getBusinessAccountId() == null) {
            log.warn(userGroupMapEntity.logShort() + " incorrect config. Group is " + userGroupMapEntity.getUserGroup().getUserGroupTypeId() + ". Missing business_account in user_group_map");
            return null;
        } else
            return  userGroupMapEntity.getUserGroup();
    }

    @Override
    protected UserGroupMapEntity enterGroup(Properties properties, UserGroupEntity userGroup, UUID userId, ApiUser apiUser) {
        return new UserGroupMapEntity()
                .setUserGroupId(userGroup.getId())
                .setUserGroup(userGroup)
                .setUserId(userId)
                .setBusinessAccountId(apiUser.getBusinessAccount().getId())
                .setBusinessAccount(apiUser.getBusinessAccount())
                .setAddedByUserId(apiUser.getUser().getId())
                .setAddedByUser(apiUser.getUser())
                .setAddedAt(Timestamp.from(Instant.now()));
    }
}