package org.twins.core.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;
import org.twins.core.dao.businessaccount.BusinessAccountUserRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessAccountService {
    final BusinessAccountUserRepository businessAccountUserRepository;

    public void addUser(UUID businessAccountId, UUID userId) throws ServiceException {
        BusinessAccountUserEntity businessAccountEntity = new BusinessAccountUserEntity()
                .businessAccountId(businessAccountId)
                .userId(userId);
        businessAccountUserRepository.save(businessAccountEntity);
    }

}
