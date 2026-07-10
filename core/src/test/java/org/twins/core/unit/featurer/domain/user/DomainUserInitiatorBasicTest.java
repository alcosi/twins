package org.twins.core.featurer.domain.user;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.featurer.domain.user.DomainUserInitiatorBasic;
import org.twins.core.service.usergroup.UserGroupService;

import java.util.Properties;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class DomainUserInitiatorBasicTest extends BaseUnitTest {

    @Mock
    private UserGroupService userGroupService;

    private DomainUserInitiatorBasic initiator;

    @BeforeEach
    void setUp() {
        initiator = new DomainUserInitiatorBasic(userGroupService);
    }

    @Nested
    class Init {

        @Test
        void init_withGroups_callsEnterGroups() throws ServiceException {
            var groupId = UUID.randomUUID();
            var props = new Properties();
            props.put("enterUserGroups", groupId.toString());

            initiator.init(props, new DomainUserEntity());

            verify(userGroupService).enterGroups(any());
        }

        @Test
        void init_emptyGroups_doesNotCallEnterGroups() throws ServiceException {
            initiator.init(new Properties(), new DomainUserEntity());

            verifyNoInteractions(userGroupService);
        }
    }
}
