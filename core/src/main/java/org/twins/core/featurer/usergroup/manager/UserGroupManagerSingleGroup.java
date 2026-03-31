package org.twins.core.featurer.usergroup.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.usergroup.slugger.Slugger;

import java.util.*;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_2102,
        name = "Single group",
        description = "")
@RequiredArgsConstructor
public class UserGroupManagerSingleGroup extends UserGroupManager {
    @FeaturerParam(name = "Allow empty", description = "If true, then user can be out of any group", order = 1)
    public static final FeaturerParamBoolean allowEmpty = new FeaturerParamBoolean("allowEmpty");
    final UserGroupRepository userGroupRepository;
    @Lazy
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
        Kit<UserGroupEntity, UUID> loadedUserGroupsKit = new Kit<>(userGroupRepository.findByIdIn(groupsToLoad), UserGroupEntity::getId);
        userGroupService.loadGroups(user);
        if (CollectionUtils.isNotEmpty(userGroupEnterList)) {
            if (CollectionUtils.size(userGroupEnterList) != 1)
                throw new ServiceException(ErrorCodeTwins.USER_GROUP_ENTER_ERROR, "only one group is allowed to be entered");
            UUID enterUserGroupId = userGroupEnterList.stream().findFirst().orElse(null);
            UserGroupEntity userGroup = loadedUserGroupsKit.get(enterUserGroupId);
            if (userGroup == null) {
                throw new ServiceException(ErrorCodeTwins.USER_GROUP_UNKNOWN, "Incorrect enterUserGroupId[" + enterUserGroupId + "]");
            }
            Slugger slugger = featurerService.getFeaturer(userGroup.getUserGroupType().getSluggerFeaturerId(), Slugger.class);
            slugger.enterGroup(userGroup, user);
            userGroupExitList = CollectionUtils.safeAdd(userGroupExitList, user.getUserGroups().getIdSet().stream().filter(m -> !m.equals(enterUserGroupId)).toList());
        }

        if (CollectionUtils.isNotEmpty(userGroupExitList)) {
            List<UserGroupEntity> exitedGroups = new ArrayList<>(), leftGroups = new ArrayList<>();
            for (UserGroupEntity currentlyEnteredGroup : user.getUserGroups().getList()) {
                if (userGroupExitList.stream().anyMatch(id -> id.equals(currentlyEnteredGroup.getId())))
                    exitedGroups.add(currentlyEnteredGroup);
                else
                    leftGroups.add(currentlyEnteredGroup);
            }
            if (leftGroups.isEmpty() && !allowEmpty.extract(properties))
                throw new ServiceException(ErrorCodeTwins.USER_GROUP_IS_MANDATORY, "User can not be deleted from all groups");
            for (UserGroupEntity exitUserGroup : exitedGroups) {
                Slugger slugger = featurerService.getFeaturer(exitUserGroup.getUserGroupType().getSluggerFeaturerId(), Slugger.class);
                slugger.exitGroup(exitUserGroup, user);
            }
        }
    }
}
