package org.twins.core.featurer.usergroup.slugger;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupMapEntity;

import java.util.Properties;

@Component
@Featurer(id = 2001,
        name = "SluggerDomainScopeDomainManage",
        description = "")
@RequiredArgsConstructor
public class SluggerDomainScopeDomainManage extends Slugger {
    @Override
    protected UserGroupEntity checkConfigAndGetGroup(Properties properties, UserGroupMapEntity userGroupMapEntity) throws ServiceException {
        checkUserGroupBusinessAccountEmpty(userGroupMapEntity);
        checkUserGroupMapBusinessAccountEmpty(userGroupMapEntity);
        return userGroupMapEntity.getUserGroup();
    }
}
