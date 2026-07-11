package org.twins.core.featurer.usergroup.manager;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupRepository;
import org.twins.core.dao.user.UserGroupTypeEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.usergroup.slugger.Slugger;
import org.twins.core.service.usergroup.UserGroupService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class UserGroupManagerImplTest extends BaseUnitTest {

    @Mock
    private UserGroupRepository userGroupRepository;

    @Mock
    private FeaturerService featurerService;

    @Mock
    private Slugger slugger;

    @Mock
    private UserGroupService userGroupService;

    private UserGroupManagerImpl userGroupManager;

    @BeforeEach
    void setUp() {
        userGroupManager = new UserGroupManagerImpl(userGroupRepository, featurerService);
        userGroupManager.userGroupService = userGroupService;
    }

    private UserEntity buildUser() {
        var user = new UserEntity();
        user.setId(UUID.randomUUID());
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
            var user = buildUser();

            userGroupManager.manageForUser(
                    new Properties(),
                    user,
                    Collections.emptySet(),
                    Collections.emptySet(),
                    mock(ApiUser.class)
            );

            verifyNoInteractions(userGroupRepository, featurerService);
        }

        @Test
        void manageForUser_nullEnterAndExitLists_doesNothing() throws ServiceException {
            var user = buildUser();

            userGroupManager.manageForUser(
                    new Properties(),
                    user,
                    null,
                    null,
                    mock(ApiUser.class)
            );

            verifyNoInteractions(userGroupRepository, featurerService);
        }

        @Test
        void manageForUser_enterGroup_callsSluggerEnterGroup() throws ServiceException {
            var groupId = UUID.randomUUID();
            var user = buildUser();
            var userGroup = buildUserGroup(groupId);

            when(userGroupService.findEntitiesSafe(Set.of(groupId)))
                    .thenReturn(kitOf(userGroup));
            when(featurerService.getFeaturer(2001, Slugger.class))
                    .thenReturn(slugger);

            userGroupManager.manageForUser(
                    new Properties(),
                    user,
                    Set.of(groupId),
                    Collections.emptySet(),
                    mock(ApiUser.class)
            );

            verify(slugger).enterGroup(userGroup, user);
            verify(slugger, never()).exitGroup(any(), any());
        }

        @Test
        void manageForUser_exitGroup_callsSluggerExitGroup() throws ServiceException {
            var groupId = UUID.randomUUID();
            var user = buildUser();
            var userGroup = buildUserGroup(groupId);

            when(userGroupService.findEntitiesSafe(Set.of(groupId)))
                    .thenReturn(kitOf(userGroup));
            when(featurerService.getFeaturer(2001, Slugger.class))
                    .thenReturn(slugger);

            userGroupManager.manageForUser(
                    new Properties(),
                    user,
                    Collections.emptySet(),
                    Set.of(groupId),
                    mock(ApiUser.class)
            );

            verify(slugger).exitGroup(userGroup, user);
            verify(slugger, never()).enterGroup(any(), any());
        }

        @Test
        void manageForUser_enterAndExitGroups_callsBothSluggers() throws ServiceException {
            var enterGroupId = UUID.randomUUID();
            var exitGroupId = UUID.randomUUID();
            var user = buildUser();
            var enterGroup = buildUserGroup(enterGroupId);
            var exitGroup = buildUserGroup(exitGroupId);

            when(userGroupService.findEntitiesSafe(Set.of(enterGroupId, exitGroupId)))
                    .thenReturn(kitOf(enterGroup, exitGroup));
            when(featurerService.getFeaturer(2001, Slugger.class))
                    .thenReturn(slugger);

            userGroupManager.manageForUser(
                    new Properties(),
                    user,
                    Set.of(enterGroupId),
                    Set.of(exitGroupId),
                    mock(ApiUser.class)
            );

            verify(slugger).enterGroup(enterGroup, user);
            verify(slugger).exitGroup(exitGroup, user);
        }

        @Test
        void manageForUser_unknownEnterGroupId_skipsGroup() throws ServiceException {
            var unknownGroupId = UUID.randomUUID();
            var user = buildUser();

            when(userGroupService.findEntitiesSafe(Set.of(unknownGroupId)))
                    .thenReturn(kitOf());

            userGroupManager.manageForUser(
                    new Properties(),
                    user,
                    Set.of(unknownGroupId),
                    Collections.emptySet(),
                    mock(ApiUser.class)
            );

            verifyNoInteractions(featurerService);
        }

        @Test
        void manageForUser_unknownExitGroupId_skipsGroup() throws ServiceException {
            var unknownGroupId = UUID.randomUUID();
            var user = buildUser();

            when(userGroupService.findEntitiesSafe(Set.of(unknownGroupId)))
                    .thenReturn(kitOf());

            userGroupManager.manageForUser(
                    new Properties(),
                    user,
                    Collections.emptySet(),
                    Set.of(unknownGroupId),
                    mock(ApiUser.class)
            );

            verifyNoInteractions(featurerService);
        }
    }
}
