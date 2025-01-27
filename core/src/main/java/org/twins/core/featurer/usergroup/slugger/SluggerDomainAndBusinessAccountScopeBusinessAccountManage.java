package org.twins.core.featurer.usergroup.slugger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupMapEntity;
import org.twins.core.dao.user.UserGroupRepository;
import org.twins.core.dao.user.UserGroupTypeEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.FeaturerTwins;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_2004,
        name = "DomainAndBusinessAccountScopeBusinessAccountManage",
        description = "")
@RequiredArgsConstructor
public class SluggerDomainAndBusinessAccountScopeBusinessAccountManage extends Slugger {
    @Lazy
    @Autowired
    EntitySmartService entitySmartService;

    @Autowired
    UserGroupRepository userGroupRepository;

    @Override
    protected UserGroupEntity checkConfigAndGetGroup(Properties properties, UserGroupMapEntity userGroupMapEntity) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (userGroupMapEntity.getUserGroup().getBusinessAccountId() == null) {
            log.warn(userGroupMapEntity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect config. Group is " + userGroupMapEntity.getUserGroup().getUserGroupTypeId() + ". Missing business_account in user_group");
            return null;
        } else if (apiUser.isBusinessAccountSpecified() && !apiUser.getBusinessAccountId().equals(userGroupMapEntity.getBusinessAccountId())) {
            return null;
        }
        checkUserGroupMapBusinessAccountEmpty(userGroupMapEntity);

        return userGroupMapEntity.getUserGroup();
    }

    @Override
    protected UserGroupMapEntity enterGroup(Properties properties, UserGroupEntity userGroup, UUID userId, ApiUser apiUser) throws ServiceException {
        if (!checkBusinessAccountCompatability(apiUser, userGroup)) {
            log.warn(userGroup.easyLog(EasyLoggable.Level.NORMAL) + " can not be entered by userId[" + userId + "]");
            return null;
        }
        if (!checkDomainCompatability(apiUser, userGroup)) {
            log.warn(userGroup.easyLog(EasyLoggable.Level.NORMAL) + " can not be entered by userId[" + userId + "]");
            return null;
        }
        if (userGroupMapRepository.existsByUserIdAndUserGroupId(userId, userGroup.getId())) {
            log.warn("userGroupMapEntity for user[" +userId + "] and group[" + userGroup.getId() + "] is already exists");
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
        //we should delete groups of given type, because they were created only for given BA in given domain
        //no need to delete data from user_group_map table, because it has cascade delete FK on user_group table
        List<UUID> groupsToDelete = userGroupRepository.findAllByBusinessAccountIdAndDomainIdAndType(businessAccountId, authService.getApiUser().getDomainId(), userGroupTypeEntity.getId());
        entitySmartService.deleteAllAndLog(groupsToDelete, userGroupRepository);
    }

    @Override
    protected void processDomainDeletion(Properties properties) throws ServiceException {

    }

    @Override
    protected void processBusinessAccountDeletion(Properties properties) throws ServiceException {

    }
}
