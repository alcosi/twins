package org.twins.core.featurer.businessaccount.initiator;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.featurer.businessaccount.initiator.BusinessAccountInitiatorImpl;

import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;


class BusinessAccountInitiatorImplTest extends BaseUnitTest {

    private final BusinessAccountInitiatorImpl initiator = new BusinessAccountInitiatorImpl();

    @Nested
    class Init {

        @Test
        void init_doesNotModifyEntity() throws ServiceException {
            var entity = new DomainBusinessAccountEntity();

            initiator.init(new Properties(), entity);

            assertNotNull(entity);
        }
    }
}
