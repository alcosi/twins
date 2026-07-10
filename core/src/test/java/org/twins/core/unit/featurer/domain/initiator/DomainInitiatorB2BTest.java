package org.twins.core.featurer.domain.initiator;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.enums.twinclass.OwnerType;
import org.twins.core.featurer.domain.initiator.DomainInitiatorB2B;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class DomainInitiatorB2BTest extends BaseUnitTest {

    private final DomainInitiatorB2B initiator = new DomainInitiatorB2B();

    @Nested
    class GetDefaultTwinClassOwnerType {

        @Test
        void returnsDomainBusinessAccount() {
            assertEquals(OwnerType.DOMAIN_BUSINESS_ACCOUNT, initiator.getDefaultTwinClassOwnerType());
        }
    }

    @Nested
    class IsSupportedTwinClassOwnerType {

        @Test
        void domain_returnsTrue() {
            assertTrue(initiator.isSupportedTwinClassOwnerType(OwnerType.DOMAIN));
        }

        @Test
        void domainUser_returnsTrue() {
            assertTrue(initiator.isSupportedTwinClassOwnerType(OwnerType.DOMAIN_USER));
        }

        @Test
        void domainBusinessAccount_returnsTrue() {
            assertTrue(initiator.isSupportedTwinClassOwnerType(OwnerType.DOMAIN_BUSINESS_ACCOUNT));
        }

        @Test
        void domainBusinessAccountUser_returnsTrue() {
            assertTrue(initiator.isSupportedTwinClassOwnerType(OwnerType.DOMAIN_BUSINESS_ACCOUNT_USER));
        }
    }

    @Nested
    class Init {

        @Test
        void init_doesNotModifyEntity() throws ServiceException {
            var entity = new DomainEntity();

            initiator.init(new Properties(), entity);

            assertNotNull(entity);
        }
    }
}
