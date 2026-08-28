package org.twins.core.featurer.domain.initiator;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.enums.twinclass.OwnerType;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.domain.initiator.DomainInitiatorBasic;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class DomainInitiatorBasicTest extends BaseUnitTest {

    private final DomainInitiatorBasic initiator = new DomainInitiatorBasic();

    @Nested
    class GetDefaultTwinClassOwnerType {

        @Test
        void returnsDomain() {
            assertEquals(OwnerType.DOMAIN, initiator.getDefaultTwinClassOwnerType());
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
        void domainBusinessAccount_returnsFalse() {
            assertFalse(initiator.isSupportedTwinClassOwnerType(OwnerType.DOMAIN_BUSINESS_ACCOUNT));
        }

        @Test
        void domainBusinessAccountUser_returnsFalse() {
            assertFalse(initiator.isSupportedTwinClassOwnerType(OwnerType.DOMAIN_BUSINESS_ACCOUNT_USER));
        }
    }

    @Nested
    class Init {

        @Test
        void init_nullifiesBusinessAccountFields() throws ServiceException {
            var entity = new DomainEntity();

            initiator.init(new Properties(), entity);

            assertNull(entity.getBusinessAccountInitiatorFeaturerId());
            assertNull(entity.getBusinessAccountInitiatorParams());
            assertNull(entity.getBusinessAccountTemplateTwinId());
        }

        @Test
        void init_setsDomainUserInitiator() throws ServiceException {
            var entity = new DomainEntity();

            initiator.init(new Properties(), entity);

            assertEquals(FeaturerTwins.ID_3401, entity.getDomainUserInitiatorFeaturerId());
        }

        @Test
        void init_nullifiesDomainUserTemplateTwinId() throws ServiceException {
            var entity = new DomainEntity();

            initiator.init(new Properties(), entity);

            assertNull(entity.getDomainUserTemplateTwinId());
        }
    }
}
