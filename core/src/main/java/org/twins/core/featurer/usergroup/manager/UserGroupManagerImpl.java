package org.twins.core.featurer.usergroup.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.usergroup.slugger.Slugger;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_2101,
        name = "Impl",
        description = "")
@RequiredArgsConstructor
public class UserGroupManagerImpl extends UserGroupManager {
    final UserGroupRepository userGroupRepository;
    final FeaturerService featurerService;

    @Override
    public void manageForUser(Properties properties, UserEntity user, Set<UUID> userGroupEnterList, Set<UUID> userGroupExitList, ApiUser apiUser) throws ServiceException {
        Set<UUID> groupsToLoad = new HashSet<>();
        if (CollectionUtils.isNotEmpty(userGroupEnterList))
            groupsToLoad.addAll(userGroupEnterList);
        if (CollectionUtils.isNotEmpty(userGroupExitList))
            groupsToLoad.addAll(userGroupExitList);
        if (CollectionUtils.isEmpty(groupsToLoad))
            return;
        Kit<UserGroupEntity, UUID> userGroupsKit = new Kit<>(userGroupRepository.findByIdIn(groupsToLoad), UserGroupEntity::getId);
        if (CollectionUtils.isNotEmpty(userGroupEnterList)) {
            for (UUID enterUserGroupId : userGroupEnterList) {
                UserGroupEntity userGroup = userGroupsKit.get(enterUserGroupId);
                if (userGroup == null) {
                    log.warn("Incorrect enterUserGroupId[" + enterUserGroupId + "]");
                    continue;
                }
                Slugger slugger = featurerService.getFeaturer(userGroup.getUserGroupType().getSluggerFeaturerId(), Slugger.class);
                slugger.enterGroup(userGroup, user);
            }
        }

        if (CollectionUtils.isNotEmpty(userGroupExitList)) {
            for (UUID exitUserGroupId : userGroupExitList) {
                UserGroupEntity userGroup = userGroupsKit.get(exitUserGroupId);
                if (userGroup == null) {
                    log.warn("Incorrect exitUserGroupId[" + exitUserGroupId + "]");
                    continue;
                }
                Slugger slugger = featurerService.getFeaturer(userGroup.getUserGroupType().getSluggerFeaturerId(), Slugger.class);
                slugger.exitGroup(userGroup, user);
            }
        }
    }
}
