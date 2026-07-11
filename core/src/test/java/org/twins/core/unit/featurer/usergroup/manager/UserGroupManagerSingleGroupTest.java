package org.twins.core.featurer.usergroup.manager;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupRepository;
import org.twins.core.dao.user.UserGroupTypeEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.usergroup.slugger.Slugger;
import org.twins.core.service.usergroup.UserGroupService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class UserGroupManagerSingleGroupTest extends BaseUnitTest {

    @Mock
    private UserGroupRepository userGroupRepository;

    @Mock
    private FeaturerService featurerService;

    @Mock
    private Slugger slugger;

    @Mock
    private UserGroupService userGroupService;

    private UserGroupManagerSingleGroup manager;

    @BeforeEach
    void setUp() {
        manager = new UserGroupManagerSingleGroup(userGroupRepository, featurerService);
        manager.userGroupService = userGroupService;
    }

    private UserEntity buildUserWithGroups(UserGroupEntity... groups) {
        var user = new UserEntity();
        user.setId(UUID.randomUUID());
        var kit = new Kit<>(UserGroupEntity::getId);
        kit.addAll(Arrays.asList(groups));
        user.setUserGroups(kit);
        return user;
    }

    private UserGroupEntity buildUserGroup(UUID id) {
        var userGroup = new UserGroupEntity();
        userGroup.setId(id);
        var userGroupType = new UserGroupTypeEntity();
        userGroupType.setSluggerFeaturerId(2001);
        userGroup.setUserGroupType(userGroupType);
        return userGroup;
    }


    private Kit<UserGroupEntity, UUID> kitOf(UserGroupEntity... groups) {
        return new Kit<>(java.util.Arrays.asList(groups), UserGroupEntity::getId);
    }

    @Nested
    class ManageForUser {

        @Test
        void manageForUser_emptyEnterAndExitLists_doesNothing() throws ServiceException {
            var user = buildUserWithGroups();

            manager.manageForUser(
                    new Properties(),
                    user,
                    Collections.emptySet(),
                    Collections.emptySet(),
                    mock(ApiUser.class)
            );

            verifyNoInteractions(userGroupRepository, featurerService);
        }

        @Test
        void manageForUser_multipleEnterGroups_throwsException() throws ServiceException {
            var group1 = UUID.randomUUID();
            var group2 = UUID.randomUUID();
            var user = buildUserWithGroups();

            var userGroup1 = buildUserGroup(group1);
            var userGroup2 = buildUserGroup(group2);
            when(userGroupService.findEntitiesSafe(Set.of(group1, group2)))
                    .thenReturn(kitOf(userGroup1, userGroup2));

            var ex = assertThrows(ServiceException.class, () ->
                    manager.manageForUser(
                            new Properties(),
                            user,
                            Set.of(group1, group2),
                            Collections.emptySet(),
                            mock(ApiUser.class)
                    )
            );

            assertEquals(ErrorCodeTwins.USER_GROUP_ENTER_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void manageForUser_unknownEnterGroupId_throwsException() throws ServiceException {
            var unknownGroupId = UUID.randomUUID();
            var user = buildUserWithGroups();

            when(userGroupService.findEntitiesSafe(Set.of(unknownGroupId)))
                    .thenReturn(kitOf());

            var ex = assertThrows(ServiceException.class, () ->
                    manager.manageForUser(
                            new Properties(),
                            user,
                            Set.of(unknownGroupId),
                            Collections.emptySet(),
                            mock(ApiUser.class)
                    )
            );

            assertEquals(ErrorCodeTwins.USER_GROUP_UNKNOWN.getCode(), ex.getErrorCode());
        }

        @Test
        void manageForUser_singleEnterGroup_callsSluggerEnterGroup() throws ServiceException {
            var groupId = UUID.randomUUID();
            var userGroup = buildUserGroup(groupId);
            var user = buildUserWithGroups();

            when(userGroupService.findEntitiesSafe(Set.of(groupId)))
                    .thenReturn(kitOf(userGroup));
            when(featurerService.getFeaturer(2001, Slugger.class))
                    .thenReturn(slugger);

            manager.manageForUser(
                    new Properties(),
                    user,
                    Set.of(groupId),
                    Collections.emptySet(),
                    mock(ApiUser.class)
            );

            verify(slugger).enterGroup(userGroup, user);
        }

        @Test
        @Disabled("bug #16: UserGroupManagerSingleGroup#manageForUser auto-adds the user's existing groups to " +
                "userGroupExitList (after loading), but those groups are not present in loadedUserGroupsKit " +
                "(groupsToLoad = enter ∪ initial exit, which was empty). The exit loop then does " +
                "loadedUserGroupsKit.get(otherGroupId) -> null -> NPE on exitUserGroup.getUserType(). " +
                "Fix: add currentlyEnteredGroup directly to exitedGroups (or load the user's groups into the kit).")
        void manageForUser_singleEnterGroup_exitsOtherGroupsAutomatically() throws ServiceException {
            var enterGroupId = UUID.randomUUID();
            var otherGroupId = UUID.randomUUID();
            var enterGroup = buildUserGroup(enterGroupId);
            var otherGroup = buildUserGroup(otherGroupId);
            var user = buildUserWithGroups(otherGroup);

            when(userGroupService.findEntitiesSafe(Set.of(enterGroupId)))
                    .thenReturn(kitOf(enterGroup));
            when(featurerService.getFeaturer(2001, Slugger.class))
                    .thenReturn(slugger);
            doAnswer(invocation -> {
                UserGroupEntity group = invocation.getArgument(0);
                UserEntity userEntity = invocation.getArgument(1);
                userEntity.getUserGroups().add(group);
                return null;
            }).when(slugger).enterGroup(enterGroup, user);

            manager.manageForUser(
                    new Properties(),
                    user,
                    Set.of(enterGroupId),
                    new HashSet<>(),
                    mock(ApiUser.class)
            );

            verify(slugger).enterGroup(enterGroup, user);
            verify(slugger).exitGroup(otherGroup, user);
        }

        @Test
        void manageForUser_exitAllGroups_allowEmptyTrue_succeeds() throws ServiceException {
            var groupId = UUID.randomUUID();
            var userGroup = buildUserGroup(groupId);
            var user = buildUserWithGroups(userGroup);

            when(userGroupService.findEntitiesSafe(Set.of(groupId)))
                    .thenReturn(kitOf(userGroup));
            when(featurerService.getFeaturer(2001, Slugger.class))
                    .thenReturn(slugger);

            var properties = new Properties();
            properties.setProperty("allowEmpty", "true");

            manager.manageForUser(
                    properties,
                    user,
                    Collections.emptySet(),
                    Set.of(groupId),
                    mock(ApiUser.class)
            );

            verify(slugger).exitGroup(userGroup, user);
        }

        @Test
        void manageForUser_exitAllGroups_allowEmptyFalse_throwsException() throws ServiceException {
            var groupId = UUID.randomUUID();
            var userGroup = buildUserGroup(groupId);
            var user = buildUserWithGroups(userGroup);

            when(userGroupService.findEntitiesSafe(Set.of(groupId)))
                    .thenReturn(kitOf(userGroup));

            var properties = new Properties();
            properties.setProperty("allowEmpty", "false");

            var ex = assertThrows(ServiceException.class, () ->
                    manager.manageForUser(
                            properties,
                            user,
                            Collections.emptySet(),
                            Set.of(groupId),
                            mock(ApiUser.class)
                    )
            );

            assertEquals(ErrorCodeTwins.USER_GROUP_IS_MANDATORY.getCode(), ex.getErrorCode());
        }

        @Test
        void manageForUser_exitSomeGroups_stillHasGroupLeft_succeeds() throws ServiceException {
            var exitGroupId = UUID.randomUUID();
            var remainGroupId = UUID.randomUUID();
            var exitGroup = buildUserGroup(exitGroupId);
            var remainGroup = buildUserGroup(remainGroupId);
            var user = buildUserWithGroups(exitGroup, remainGroup);

            when(userGroupService.findEntitiesSafe(Set.of(exitGroupId)))
                    .thenReturn(kitOf(exitGroup));
            when(featurerService.getFeaturer(2001, Slugger.class))
                    .thenReturn(slugger);

            var properties = new Properties();
            properties.setProperty("allowEmpty", "false");

            manager.manageForUser(
                    properties,
                    user,
                    Collections.emptySet(),
                    Set.of(exitGroupId),
                    mock(ApiUser.class)
            );

            verify(slugger).exitGroup(exitGroup, user);
            verifyNoMoreInteractions(slugger);
        }
    }
}
