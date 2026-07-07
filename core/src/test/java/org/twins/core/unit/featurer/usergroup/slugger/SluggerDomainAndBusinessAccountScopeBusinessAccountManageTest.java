package org.twins.core.featurer.usergroup.slugger;

import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupRepository;
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


class SluggerDomainAndBusinessAccountScopeBusinessAccountManageTest extends BaseUnitTest {

    @Mock
    private UserGroupRepository userGroupRepository;

    @Mock
    private UserGroupMapRepository userGroupMapRepository;

    @Mock
    private EntitySmartService entitySmartService;

    @Mock
    private AuthService authService;

    private SluggerDomainAndBusinessAccountScopeBusinessAccountManage slugger;

    @BeforeEach
    void setUp() {
        slugger = new SluggerDomainAndBusinessAccountScopeBusinessAccountManage();
        slugger.userGroupRepository = userGroupRepository;
        slugger.userGroupMapRepository = userGroupMapRepository;
        slugger.entitySmartService = entitySmartService;
        slugger.authService = authService;
    }

    private UserEntity buildUser(UUID id) {
        var user = new UserEntity();
        user.setId(id);
        return user;
    }

    private UserGroupEntity buildUserGroup(UUID groupId, UUID businessAccountId, UUID domainId) {
        var userGroup = new UserGroupEntity();
        userGroup.setId(groupId);
        userGroup.setBusinessAccountId(businessAccountId);
        userGroup.setDomainId(domainId);
        return userGroup;
    }

    @Nested
    class EnterGroup {

        @Test
        void enterGroup_bothMatch_returnsMapEntity() throws ServiceException {
            var businessAccountId = UUID.randomUUID();
            var domainId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            var groupId = UUID.randomUUID();
            var user = buildUser(userId);
            var userGroup = buildUserGroup(groupId, businessAccountId, domainId);

            var apiUserEntity = new UserEntity();
            apiUserEntity.setId(userId);

            var apiUser = mock(ApiUser.class);
            when(apiUser.isBusinessAccountSpecified()).thenReturn(true);
            when(apiUser.getBusinessAccountId()).thenReturn(businessAccountId);
            when(apiUser.isDomainSpecified()).thenReturn(true);
            when(apiUser.getDomainId()).thenReturn(domainId);
            when(apiUser.getUser()).thenReturn(apiUserEntity);
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = slugger.enterGroup(new Properties(), user, userGroup);

            assertNotNull(result);
            assertEquals(groupId, result.getUserGroupId());
            assertEquals(userId, result.getUserId());
            assertSame(userGroup, result.getUserGroup());
            assertSame(user, result.getUser());
            verify(userGroupMapRepository).save(result);
        }

        @Test
        void enterGroup_businessAccountMismatch_returnsNull() throws ServiceException {
            var userBaId = UUID.randomUUID();
            var groupBaId = UUID.randomUUID();
            var domainId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            var groupId = UUID.randomUUID();
            var user = buildUser(userId);
            var userGroup = buildUserGroup(groupId, groupBaId, domainId);

            var apiUser = mock(ApiUser.class);
            when(apiUser.isBusinessAccountSpecified()).thenReturn(true);
            when(apiUser.getBusinessAccountId()).thenReturn(userBaId);
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = slugger.enterGroup(new Properties(), user, userGroup);

            assertNull(result);
            verify(userGroupMapRepository, never()).save(any());
        }

        @Test
        void enterGroup_domainMismatch_returnsNull() throws ServiceException {
            var businessAccountId = UUID.randomUUID();
            var userDomainId = UUID.randomUUID();
            var groupDomainId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            var groupId = UUID.randomUUID();
            var user = buildUser(userId);
            var userGroup = buildUserGroup(groupId, businessAccountId, groupDomainId);

            var apiUser = mock(ApiUser.class);
            when(apiUser.isBusinessAccountSpecified()).thenReturn(true);
            when(apiUser.getBusinessAccountId()).thenReturn(businessAccountId);
            when(apiUser.isDomainSpecified()).thenReturn(true);
            when(apiUser.getDomainId()).thenReturn(userDomainId);
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = slugger.enterGroup(new Properties(), user, userGroup);

            assertNull(result);
            verify(userGroupMapRepository, never()).save(any());
        }

        @Test
        void enterGroup_businessAccountNotSpecified_returnsNull() throws ServiceException {
            var businessAccountId = UUID.randomUUID();
            var domainId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            var groupId = UUID.randomUUID();
            var user = buildUser(userId);
            var userGroup = buildUserGroup(groupId, businessAccountId, domainId);

            var apiUser = mock(ApiUser.class);
            when(apiUser.isBusinessAccountSpecified()).thenReturn(false);
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = slugger.enterGroup(new Properties(), user, userGroup);

            assertNull(result);
            verify(userGroupMapRepository, never()).save(any());
        }

        @Test
        void enterGroup_domainNotSpecified_returnsNull() throws ServiceException {
            var businessAccountId = UUID.randomUUID();
            var domainId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            var groupId = UUID.randomUUID();
            var user = buildUser(userId);
            var userGroup = buildUserGroup(groupId, businessAccountId, domainId);

            var apiUser = mock(ApiUser.class);
            when(apiUser.isBusinessAccountSpecified()).thenReturn(true);
            when(apiUser.getBusinessAccountId()).thenReturn(businessAccountId);
            when(apiUser.isDomainSpecified()).thenReturn(false);
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = slugger.enterGroup(new Properties(), user, userGroup);

            assertNull(result);
            verify(userGroupMapRepository, never()).save(any());
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
            var userGroup = buildUserGroup(groupId, businessAccountId, domainId);

            var apiUser = mock(ApiUser.class);
            when(apiUser.getBusinessAccountId()).thenReturn(businessAccountId);
            when(apiUser.getDomainId()).thenReturn(domainId);
            when(authService.getApiUser()).thenReturn(apiUser);

            var mapEntity = new UserGroupMapEntity();
            mapEntity.setId(mapEntityId);

            when(userGroupMapRepository.findByUserIdAndUserGroupIdAndUserGroupSpecOnly_BusinessAccountIdAndUserGroupSpecOnly_DomainId(
                    userId, groupId, businessAccountId, domainId))
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
            var userGroup = buildUserGroup(groupId, businessAccountId, domainId);

            var apiUser = mock(ApiUser.class);
            when(apiUser.getBusinessAccountId()).thenReturn(businessAccountId);
            when(apiUser.getDomainId()).thenReturn(domainId);
            when(authService.getApiUser()).thenReturn(apiUser);

            when(userGroupMapRepository.findByUserIdAndUserGroupIdAndUserGroupSpecOnly_BusinessAccountIdAndUserGroupSpecOnly_DomainId(
                    userId, groupId, businessAccountId, domainId))
                    .thenReturn(null);

            var result = slugger.exitGroup(new Properties(), user, userGroup);

            assertFalse(result);
            verifyNoInteractions(entitySmartService);
        }
    }

    @Nested
    class ProcessDomainBusinessAccountDeletion {

        @Test
        void processDomainBusinessAccountDeletion_deletesGroups() throws ServiceException {
            var businessAccountId = UUID.randomUUID();
            var domainId = UUID.randomUUID();
            var userGroupTypeEntity = new UserGroupTypeEntity();
            userGroupTypeEntity.setId("type1");
            var group1Id = UUID.randomUUID();
            var group2Id = UUID.randomUUID();

            var apiUser = mock(ApiUser.class);
            when(apiUser.getDomainId()).thenReturn(domainId);
            when(authService.getApiUser()).thenReturn(apiUser);
            when(userGroupRepository.findAllByBusinessAccountIdAndDomainIdAndType(
                    businessAccountId, domainId, "type1"))
                    .thenReturn(List.of(group1Id, group2Id));

            slugger.processDomainBusinessAccountDeletion(new Properties(), businessAccountId, userGroupTypeEntity);

            verify(entitySmartService).deleteAllAndLog(List.of(group1Id, group2Id), userGroupRepository);
        }
    }

    @Nested
    class ProcessDomainDeletion {

        @Test
        void processDomainDeletion_doesNothing() throws ServiceException {
            assertDoesNotThrow(() ->
                    slugger.processDomainDeletion(new Properties())
            );

            verifyNoInteractions(userGroupRepository, entitySmartService);
        }
    }

    @Nested
    class ProcessBusinessAccountDeletion {

        @Test
        void processBusinessAccountDeletion_doesNothing() throws ServiceException {
            assertDoesNotThrow(() ->
                    slugger.processBusinessAccountDeletion(new Properties())
            );

            verifyNoInteractions(userGroupRepository, entitySmartService);
        }
    }
}
