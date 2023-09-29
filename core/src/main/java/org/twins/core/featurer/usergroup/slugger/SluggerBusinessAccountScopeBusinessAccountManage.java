package org.twins.core.featurer.usergroup.slugger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupMapEntity;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = 2003,
        name = "SluggerBusinessAccountScopeBusinessAccountManage",
        description = "")
@RequiredArgsConstructor
public class SluggerBusinessAccountScopeBusinessAccountManage extends Slugger {
    @Override
    protected UserGroupEntity checkConfigAndGetGroup(Properties properties, UserGroupMapEntity userGroupMapEntity) throws ServiceException {
        checkUserGroupMapBusinessAccountEmpty(userGroupMapEntity);
        if (userGroupMapEntity.getUserGroup().getBusinessAccountId() == null) {
            log.warn(userGroupMapEntity.getUserGroup().logShort() + " incorrect config. Group is " + userGroupMapEntity.getUserGroup().getUserGroupTypeId() + ". Missing business_account in user_group");
            return null;
        } else
            return userGroupMapEntity.getUserGroup();
    }
}
