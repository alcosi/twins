package org.twins.core.featurer.usergroup.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupMapEntity;
import org.twins.core.dao.user.UserGroupMapRepository;
import org.twins.core.dao.user.UserGroupRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.usergroup.slugger.Slugger;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_2102,
        name = "Single group",
        description = "")
@RequiredArgsConstructor
public class UserGroupManagerSingleGroup extends UserGroupManager {
    final UserGroupRepository userGroupRepository;
    final UserGroupMapRepository userGroupMapRepository;
    @Lazy
    final FeaturerService featurerService;

    @FeaturerParam(name = "Allow empty", description = "If true, then user can be out of any group", order = 1)
    public static final FeaturerParamBoolean allowEmpty = new FeaturerParamBoolean("allowEmpty");

    @Override
    public void manageForUser(Properties properties, UUID userId, List<UUID> userGroupEnterList, List<UUID> userGroupExitList, ApiUser apiUser) throws ServiceException {
        if (CollectionUtils.isNotEmpty(userGroupEnterList)) {
            if (CollectionUtils.size(userGroupEnterList) != 1)
                throw new ServiceException(ErrorCodeTwins.USER_GROUP_ENTER_ERROR, "only one group is allowed to be entered");
            UUID enterUserGroupId = userGroupEnterList.get(0);
            Optional<UserGroupEntity> optionalUserGroup = userGroupRepository.findById(enterUserGroupId);
            if (optionalUserGroup.isEmpty()) {
                throw new ServiceException(ErrorCodeTwins.USER_GROUP_UNKNOWN, "Incorrect enterUserGroupId[" + enterUserGroupId + "]");
            }
            UserGroupEntity userGroup = optionalUserGroup.get();
            Slugger slugger = featurerService.getFeaturer(userGroup.getUserGroupType().getSluggerFeaturer(), Slugger.class);
            slugger.enterGroup(userGroup, userId);
            List<UserGroupMapEntity> allEnteredGroups = userGroupService.getUserGroupsMap(userId);
            userGroupMapRepository.deleteAll(allEnteredGroups.stream().filter(m -> !m.getUserGroupId().equals(enterUserGroupId)).collect(Collectors.toList()));
        }

        if (CollectionUtils.isNotEmpty(userGroupExitList)) {
            List<UserGroupMapEntity> exitedGroups = new ArrayList<>(), leftGroups = new ArrayList<>();
            List<UserGroupMapEntity> allEnteredGroups = userGroupService.getUserGroupsMap(userId);
            for (UserGroupMapEntity currentlyEnteredGroup : allEnteredGroups) {
                if (userGroupExitList.stream().anyMatch(id -> id.equals(currentlyEnteredGroup.getUserGroupId())))
                    exitedGroups.add(currentlyEnteredGroup);
                else
                    leftGroups.add(currentlyEnteredGroup);
            }
            if (leftGroups.isEmpty() && !allowEmpty.extract(properties))
                throw new ServiceException(ErrorCodeTwins.USER_GROUP_IS_MANDATORY, "User can not be deleted from all groups");
            for (UserGroupMapEntity exitUserGroup : exitedGroups) {
                Slugger slugger = featurerService.getFeaturer(exitUserGroup.getUserGroup().getUserGroupType().getSluggerFeaturer(), Slugger.class);
                slugger.exitGroup(exitUserGroup.getUserGroup(), userId);
            }
        }
    }
}
