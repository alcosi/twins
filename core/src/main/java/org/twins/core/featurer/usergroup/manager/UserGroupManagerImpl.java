package org.twins.core.featurer.usergroup.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupMapEntity;
import org.twins.core.dao.user.UserGroupRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.usergroup.slugger.Slugger;

import java.util.*;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_2101,
        name = "UserGroupManagerImpl",
        description = "")
@RequiredArgsConstructor
public class UserGroupManagerImpl extends UserGroupManager {
    final UserGroupRepository userGroupRepository;
    final FeaturerService featurerService;
    @Override
    public void manageForUser(Properties properties, UUID userId, List<UUID> userGroupEnterList, List<UUID> userGroupExitList, ApiUser apiUser) throws ServiceException {
        List<UserGroupMapEntity> userGroupMapEntityList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userGroupEnterList)) {
            for (UUID enterUserGroupId : userGroupEnterList) {
                Optional<UserGroupEntity> optionalUserGroup = userGroupRepository.findById(enterUserGroupId);
                if (optionalUserGroup.isEmpty()) {
                    log.warn("Incorrect enterUserGroupId[" + enterUserGroupId + "]");
                    continue;
                }
                UserGroupEntity userGroup = optionalUserGroup.get();
                Slugger slugger = featurerService.getFeaturer(userGroup.getUserGroupType().getSluggerFeaturer(), Slugger.class);
                slugger.enterGroup(userGroup, userId);
            }
        }

        if (CollectionUtils.isNotEmpty(userGroupExitList)) {
            for (UUID exitUserGroupId : userGroupExitList) {
                Optional<UserGroupEntity> optionalUserGroup = userGroupRepository.findById(exitUserGroupId);
                if (optionalUserGroup.isEmpty()) {
                    log.warn("Incorrect exitUserGroupId[" + exitUserGroupId + "]");
                    continue;
                }
                UserGroupEntity userGroup = optionalUserGroup.get();
                Slugger slugger = featurerService.getFeaturer(userGroup.getUserGroupType().getSluggerFeaturer(), Slugger.class);
                slugger.exitGroup(userGroup, userId);
            }
        }
    }
}
