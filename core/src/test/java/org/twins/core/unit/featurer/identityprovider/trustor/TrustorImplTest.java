package org.twins.core.featurer.identityprovider.trustor;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.apiuser.ActAsUser;
import org.twins.core.domain.auth.CryptKey;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class TrustorImplTest extends BaseUnitTest {

    private final TrustorImpl trustorImpl = new TrustorImpl();

    @Nested
    class GetActAsUserPublicKey {

        @Test
        void getActAsUserPublicKey_returnsNull() throws ServiceException {
            var result = trustorImpl.getActAsUserPublicKey(new Properties());

            assertNull(result);
        }
    }

    @Nested
    class ResolveActAsUser {

        @Test
        void resolveActAsUser_withUserIdOnly_returnsActAsUserWithUserId() throws ServiceException {
            var userId = UUID.randomUUID();

            var result = trustorImpl.resolveActAsUser(new Properties(), userId.toString());

            assertEquals(userId, result.getUserId());
            assertNull(result.getBusinessAccountId());
        }

        @Test
        void resolveActAsUser_withUserIdAndBusinessAccountId_returnsBoth() throws ServiceException {
            var userId = UUID.randomUUID();
            var businessAccountId = UUID.randomUUID();
            var header = userId + "," + businessAccountId;

            var result = trustorImpl.resolveActAsUser(new Properties(), header);

            assertEquals(userId, result.getUserId());
            assertEquals(businessAccountId, result.getBusinessAccountId());
        }

        @Test
        void resolveActAsUser_withWhitespaceInHeader_trimsValues() throws ServiceException {
            var userId = UUID.randomUUID();
            var businessAccountId = UUID.randomUUID();
            var header = userId + " , " + businessAccountId + " ";

            var result = trustorImpl.resolveActAsUser(new Properties(), header);

            assertEquals(userId, result.getUserId());
            assertEquals(businessAccountId, result.getBusinessAccountId());
        }

        @Test
        void resolveActAsUser_withInvalidUserId_throwsException() {
            var header = "not-a-uuid";

            assertThrows(IllegalArgumentException.class, () ->
                    trustorImpl.resolveActAsUser(new Properties(), header));
        }

        @Test
        void resolveActAsUser_withInvalidBusinessAccountId_throwsException() {
            var userId = UUID.randomUUID();
            var header = userId + ",not-a-uuid";

            assertThrows(IllegalArgumentException.class, () ->
                    trustorImpl.resolveActAsUser(new Properties(), header));
        }
    }
}
