package org.twins.core.featurer.usergroup.slugger;

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

@Component
@Featurer(id = FeaturerTwins.ID_2001,
        name = "Domain scope / domain manage",
        description = "")
@Slf4j
public class SluggerDomainScopeDomainManage extends Slugger {
    @Autowired
    UserGroupMapRepository userGroupMapRepository;

    @Override
    protected UserGroupMapEntity enterGroup(Properties properties, UserEntity user, UserGroupEntity userGroup) throws ServiceException {
        if (!checkDomainCompatability(userGroup.getDomainId())) {
            log.warn("{} can not be entered by {}", userGroup.logNormal(), user.logNormal());
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
        UserGroupMapEntity entityToDelete = userGroupMapRepository.findByUserIdAndUserGroupIdAndUserGroup_DomainId(user.getId(), userGroup.getId(), authService.getApiUser().getDomainId());
        if (entityToDelete == null)
            return false;
        entitySmartService.deleteAndLog(entityToDelete.getId(), userGroupMapRepository);
        return true;
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
