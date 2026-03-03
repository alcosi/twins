package org.twins.core.featurer.usergroup.slugger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupTypeEntity;
import org.twins.core.dao.usergroup.UserGroupMapEntity;
import org.twins.core.dao.usergroup.UserGroupMapRepository;
import org.twins.core.featurer.FeaturerTwins;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_2003,
        name = "Business account scope / business account manage",
        description = "")
@RequiredArgsConstructor
public class SluggerBusinessAccountScopeBusinessAccountManage extends Slugger {
    @Autowired
    UserGroupMapRepository userGroupMapRepository;

    @Override
    protected UserGroupMapEntity enterGroup(Properties properties, UserEntity user, UserGroupEntity userGroup) throws ServiceException {
        if (!checkBusinessAccountCompatability(userGroup.getBusinessAccountId())) {
            return null;
        }
        var apiUser = authService.getApiUser();
        var ret = new UserGroupMapEntity()
                .setUserGroupId(userGroup.getId())
                .setUserGroup(userGroup)
                .setUserId(user.getId())
                .setUser(user)
                .setAddedByUserId(apiUser.getUser().getId())
                .setAddedByUser(apiUser.getUser())
                .setAddedAt(Timestamp.from(Instant.now()));
        userGroupMapRepository.save(ret);
        return ret;
    }

    @Override
    protected boolean exitGroup(Properties properties, UserEntity user, UserGroupEntity userGroup) throws ServiceException {
        //todo no need for select, cause data is already loaded in user
        UserGroupMapEntity entityToDelete = userGroupMapRepository
                .findByUserIdAndUserGroupIdAndUserGroup_BusinessAccountId(user.getId(), userGroup.getId(), authService.getApiUser().getBusinessAccountId());
        if (entityToDelete == null)
            return false;
        entitySmartService.deleteAndLog(entityToDelete.getId(), userGroupMapRepository);
        return true;
    }

    @Override
    protected void processDomainBusinessAccountDeletion(Properties properties, UUID businessAccountId, UserGroupTypeEntity userGroupTypeEntity) throws ServiceException {
        //nothing to do, because slugger should not react on BA from domain deletion
    }

    @Override
    protected void processDomainDeletion(Properties properties) throws ServiceException {
        //nothing to do
    }

    @Override
    protected void processBusinessAccountDeletion(Properties properties) throws ServiceException {
        //todo implement me
    }
}
