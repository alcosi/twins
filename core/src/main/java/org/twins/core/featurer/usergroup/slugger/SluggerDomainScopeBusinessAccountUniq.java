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
@Featurer(id = 2004,
        name = "SluggerDomainScopeBusinessAccountUniq",
        description = "")
@RequiredArgsConstructor
public class SluggerDomainScopeBusinessAccountUniq extends Slugger {
    @Override
    protected UserGroupEntity checkConfigAndGetGroup(Properties properties, UserGroupMapEntity userGroupMapEntity) throws ServiceException {
        checkUserGroupBusinessAccountEmpty(userGroupMapEntity);
        if (userGroupMapEntity.getBusinessAccountId() == null) {
            log.warn(userGroupMapEntity.logShort() + " incorrect config. Group is " + userGroupMapEntity.getUserGroup().getUserGroupTypeId() + ". Missing business_account in user_group_map");
            return null;
        } else
            return userGroupMapEntity.getUserGroup();
    }
}
