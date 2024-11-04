package org.twins.core.featurer.usergroup.slugger;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupMapEntity;
import org.twins.core.dao.user.UserGroupTypeEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.FeaturerTwins;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2001,
        name = "SluggerDomainScopeDomainManage",
        description = "")
@Slf4j
public class SluggerDomainScopeDomainManage extends Slugger {

    @Override
    protected UserGroupEntity checkConfigAndGetGroup(Properties properties, UserGroupMapEntity userGroupMapEntity) throws ServiceException {
        checkUserGroupBusinessAccountEmpty(userGroupMapEntity.getUserGroup());
        checkUserGroupMapBusinessAccountEmpty(userGroupMapEntity);
        return userGroupMapEntity.getUserGroup();
    }

    @Override
    protected UserGroupMapEntity enterGroup(Properties properties, UserGroupEntity userGroup, UUID userId, ApiUser apiUser) throws ServiceException {
        if (!checkDomainCompatability(apiUser, userGroup)) {
            log.warn(userGroup.easyLog(EasyLoggable.Level.NORMAL) + " can not be entered by userId[" + userId + "]");
            return null;
        }
        UserGroupMapEntity userGroupMapEntity = userGroupMapRepository.findByUserIdAndUserGroupId(userId, userGroup.getId());
        if (userGroupMapEntity != null) {
            log.warn(userGroupMapEntity.easyLog(EasyLoggable.Level.NORMAL) + " is already exists");
            return null;
        }

        return new UserGroupMapEntity()
                .setUserGroupId(userGroup.getId())
                .setUserGroup(userGroup)
                .setUserId(userId)
                .setAddedByUserId(apiUser.getUser().getId())
                .setAddedByUser(apiUser.getUser())
                .setAddedAt(Timestamp.from(Instant.now()));
    }

    @Override
    protected void processDomainBusinessAccountDeletion(Properties properties, UUID businessAccountId, UserGroupTypeEntity userGroupTypeEntity) throws ServiceException {
        //nothing to do
    }

    @Override
    protected void processDomainDeletion(Properties properties) throws ServiceException {
        //todo implement
    }

    @Override
    protected void processBusinessAccountDeletion(Properties properties) throws ServiceException {
        //nothing to do
    }
}
