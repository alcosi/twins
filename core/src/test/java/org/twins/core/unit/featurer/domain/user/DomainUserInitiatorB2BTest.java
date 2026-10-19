package org.twins.core.featurer.domain.user;

import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dao.domain.DomainUserRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.domain.user.DomainUserInitiatorB2B;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.businessaccount.BusinessAccountService;
import org.twins.core.service.businessaccount.BusinessAccountUserService;
import org.twins.core.service.domain.DomainBusinessAccountService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.usergroup.UserGroupService;

import java.util.Properties;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class DomainUserInitiatorB2BTest extends BaseUnitTest {

    @Mock
    private UserGroupService userGroupService;

    @Mock
    private DomainBusinessAccountService domainBusinessAccountService;

    @Mock
    private AuthService authService;

    @Mock
    private BusinessAccountUserService businessAccountUserService;

    @Mock
    private BusinessAccountService businessAccountService;

    @Mock
    private EntitySmartService entitySmartService;

    @Mock
    private DomainUserRepository domainUserRepository;

    @Mock
    private TwinService twinService;

    @Mock
    private ApiUser apiUser;

    @InjectMocks
    private DomainUserInitiatorB2B initiator;

    private DomainUserEntity entity;

    @BeforeEach
    void setUp() {
        entity = new DomainUserEntity();
        entity.setDomain(new DomainEntity());
    }

    @Nested
    class Init {

        @Test
        void init_doesNotModifyEntity() throws ServiceException {
            initiator.init(new Properties(), entity);

            assertNotNull(entity);
        }
    }

    @Nested
    class PostInit {

        @Test
        void postInit_autoCreateBusinessAccount_createsAndAssigns() throws ServiceException {
            var userId = UUID.randomUUID();

            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getUserId()).thenReturn(userId);
            when(businessAccountService.addBusinessAccount(
                    any(UUID.class),
                    eq("New company"),
                    eq(EntitySmartService.SaveMode.ifPresentThrowsElseCreate)
            )).thenReturn(new BusinessAccountEntity());

            var props = new Properties();
            props.put("autoCreateBusinessAccount", "true");

            initiator.postInit(props, entity);

            verify(businessAccountService).addBusinessAccount(
                    any(UUID.class),
                    eq("New company"),
                    eq(EntitySmartService.SaveMode.ifPresentThrowsElseCreate)
            );
            verify(domainBusinessAccountService).addBusinessAccount(
                    any(BusinessAccountEntity.class),
                    isNull(),
                    eq(false)
            );
            verify(businessAccountUserService).addUser(
                    any(UUID.class),
                    eq(userId),
                    eq(false)
            );
            assertNotNull(entity.getLastActiveBusinessAccountId());
        }

        @Test
        void postInit_noAutoCreate_doesNotCreateBusinessAccount() throws ServiceException {
            var props = new Properties();
            props.put("autoCreateBusinessAccount", "false");

            initiator.postInit(props, entity);

            verifyNoInteractions(businessAccountService);
            verifyNoInteractions(domainBusinessAccountService);
            verifyNoInteractions(businessAccountUserService);
            assertNull(entity.getLastActiveBusinessAccountId());
        }

        @Test
        void postInit_withGroups_callsManageForUser() throws ServiceException {
            var userId = UUID.randomUUID();
            var groupId = UUID.randomUUID();

            entity.setUserId(userId);

            var props = new Properties();
            props.put("autoCreateBusinessAccount", "false");
            props.put("enterUserGroups", groupId.toString());

            initiator.postInit(props, entity);

            verify(userGroupService).manageForUser(
                    eq(userId),
                    any(),
                    isNull()
            );
        }

        @Test
        void postInit_emptyGroups_doesNotCallManageForUser() throws ServiceException {
            var props = new Properties();
            props.put("autoCreateBusinessAccount", "false");

            initiator.postInit(props, entity);

            verifyNoInteractions(userGroupService);
        }
    }
}
