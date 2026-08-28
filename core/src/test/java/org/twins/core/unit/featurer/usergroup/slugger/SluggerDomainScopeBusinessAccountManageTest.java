package org.twins.core.featurer.usergroup.slugger;

import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupTypeEntity;
import org.twins.core.dao.usergroup.UserGroupMapEntity;
import org.twins.core.dao.usergroup.UserGroupMapRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class SluggerDomainScopeBusinessAccountManageTest extends BaseUnitTest {

    @Mock
    private UserGroupMapRepository userGroupMapRepository;

    @Mock
    private EntitySmartService entitySmartService;

    @Mock
    private AuthService authService;

    private SluggerDomainScopeBusinessAccountManage slugger;

    @BeforeEach
    void setUp() {
        slugger = new SluggerDomainScopeBusinessAccountManage();
        slugger.userGroupMapRepository = userGroupMapRepository;
        slugger.entitySmartService = entitySmartService;
        slugger.authService = authService;
    }

    private UserEntity buildUser(UUID id) {
        var user = new UserEntity();
        user.setId(id);
        return user;
    }

    private UserGroupEntity buildUserGroup(UUID groupId, UUID domainId) {
        var userGroup = new UserGroupEntity();
        userGroup.setId(groupId);
        userGroup.setDomainId(domainId);
        return userGroup;
    }

    @Nested
    class EnterGroup {

        @Test
        void enterGroup_businessAccountNotSpecified_returnsNull() throws ServiceException {
            var domainId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            var groupId = UUID.randomUUID();
            var user = buildUser(userId);
            var userGroup = buildUserGroup(groupId, domainId);

            var apiUser = mock(ApiUser.class);
            when(apiUser.isBusinessAccountSpecified()).thenReturn(false);
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = slugger.enterGroup(new Properties(), user, userGroup);

            assertNull(result);
            verifyNoInteractions(userGroupMapRepository);
        }

        @Test
        void enterGroup_domainMismatch_returnsNull() throws ServiceException {
            var businessAccountId = UUID.randomUUID();
            var userDomainId = UUID.randomUUID();
            var groupDomainId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            var groupId = UUID.randomUUID();
            var user = buildUser(userId);
            var userGroup = buildUserGroup(groupId, groupDomainId);

            var apiUser = mock(ApiUser.class);
            when(apiUser.isBusinessAccountSpecified()).thenReturn(true);
            when(apiUser.isDomainSpecified()).thenReturn(true);
            when(apiUser.getDomainId()).thenReturn(userDomainId);
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = slugger.enterGroup(new Properties(), user, userGroup);

            assertNull(result);
            verifyNoInteractions(userGroupMapRepository);
        }

        @Test
        void enterGroup_domainMatch_savesAndReturnsMapEntity() throws ServiceException {
            var businessAccountId = UUID.randomUUID();
            var domainId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            var groupId = UUID.randomUUID();
            var user = buildUser(userId);
            var userGroup = buildUserGroup(groupId, domainId);

            var businessAccount = new BusinessAccountEntity();
            businessAccount.setId(businessAccountId);
            var apiUserEntity = new UserEntity();
            apiUserEntity.setId(userId);

            var apiUser = mock(ApiUser.class);
            when(apiUser.isBusinessAccountSpecified()).thenReturn(true);
            when(apiUser.isDomainSpecified()).thenReturn(true);
            when(apiUser.getDomainId()).thenReturn(domainId);
            when(apiUser.getBusinessAccountId()).thenReturn(businessAccountId);
            when(apiUser.getBusinessAccount()).thenReturn(businessAccount);
            when(apiUser.getUser()).thenReturn(apiUserEntity);
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = slugger.enterGroup(new Properties(), user, userGroup);

            assertNotNull(result);
            assertEquals(groupId, result.getUserGroupId());
            assertEquals(userId, result.getUserId());
            assertEquals(businessAccountId, result.getBusinessAccountId());
            assertSame(userGroup, result.getUserGroup());
            assertSame(user, result.getUser());
            verify(userGroupMapRepository).save(result);
        }

        @Test
        void enterGroup_nullDomainIdOnGroup_returnsNull() throws ServiceException {
            var userId = UUID.randomUUID();
            var groupId = UUID.randomUUID();
            var user = buildUser(userId);
            var userGroup = buildUserGroup(groupId, null);

            var apiUser = mock(ApiUser.class);
            when(apiUser.isBusinessAccountSpecified()).thenReturn(true);
            when(apiUser.isDomainSpecified()).thenReturn(true);
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = slugger.enterGroup(new Properties(), user, userGroup);

            assertNull(result);
            verifyNoInteractions(userGroupMapRepository);
        }
    }

    @Nested
    class ExitGroup {

        @Test
        void exitGroup_entityFound_deletesAndReturnsTrue() throws ServiceException {
            var businessAccountId = UUID.randomUUID();
            var domainId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            var groupId = UUID.randomUUID();
            var mapEntityId = UUID.randomUUID();
            var user = buildUser(userId);
            var userGroup = buildUserGroup(groupId, domainId);

            var apiUser = mock(ApiUser.class);
            when(apiUser.getBusinessAccountId()).thenReturn(businessAccountId);
            when(authService.getApiUser()).thenReturn(apiUser);

            var mapEntity = new UserGroupMapEntity();
            mapEntity.setId(mapEntityId);

            when(userGroupMapRepository.findByUserIdAndUserGroupIdAndBusinessAccountId(
                    userId, groupId, businessAccountId))
                    .thenReturn(mapEntity);

            var result = slugger.exitGroup(new Properties(), user, userGroup);

            assertTrue(result);
            verify(entitySmartService).deleteAndLog(mapEntityId, userGroupMapRepository);
        }

        @Test
        void exitGroup_entityNotFound_returnsFalse() throws ServiceException {
            var businessAccountId = UUID.randomUUID();
            var domainId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            var groupId = UUID.randomUUID();
            var user = buildUser(userId);
            var userGroup = buildUserGroup(groupId, domainId);

            var apiUser = mock(ApiUser.class);
            when(apiUser.getBusinessAccountId()).thenReturn(businessAccountId);
            when(authService.getApiUser()).thenReturn(apiUser);

            when(userGroupMapRepository.findByUserIdAndUserGroupIdAndBusinessAccountId(
                    userId, groupId, businessAccountId))
                    .thenReturn(null);

            var result = slugger.exitGroup(new Properties(), user, userGroup);

            assertFalse(result);
            verifyNoInteractions(entitySmartService);
        }
    }

    @Nested
    class ProcessDomainBusinessAccountDeletion {

        @Test
        void processDomainBusinessAccountDeletion_deletesUsersFromGroups() throws ServiceException {
            var businessAccountId = UUID.randomUUID();
            var domainId = UUID.randomUUID();
            var userGroupTypeEntity = new UserGroupTypeEntity();
            userGroupTypeEntity.setId("type1");
            var mapId1 = UUID.randomUUID();
            var mapId2 = UUID.randomUUID();

            var apiUser = mock(ApiUser.class);
            when(apiUser.getDomainId()).thenReturn(domainId);
            when(authService.getApiUser()).thenReturn(apiUser);
            when(userGroupMapRepository.findAllByBusinessAccountIdAndDomainIdAndType(
                    businessAccountId, domainId, "type1"))
                    .thenReturn(List.of(mapId1, mapId2));

            slugger.processDomainBusinessAccountDeletion(new Properties(), businessAccountId, userGroupTypeEntity);

            verify(entitySmartService).deleteAllAndLog(List.of(mapId1, mapId2), userGroupMapRepository);
        }
    }

    @Nested
    class ProcessDomainDeletion {

        @Test
        void processDomainDeletion_doesNothing() throws ServiceException {
            assertDoesNotThrow(() ->
                    slugger.processDomainDeletion(new Properties())
            );

            verifyNoInteractions(userGroupMapRepository, entitySmartService);
        }
    }

    @Nested
    class ProcessBusinessAccountDeletion {

        @Test
        void processBusinessAccountDeletion_doesNothing() throws ServiceException {
            assertDoesNotThrow(() ->
                    slugger.processBusinessAccountDeletion(new Properties())
            );

            verifyNoInteractions(userGroupMapRepository, entitySmartService);
        }
    }
}
