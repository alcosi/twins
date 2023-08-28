package org.twins.core.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;
import org.twins.core.dao.businessaccount.BusinessAccountUserRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TQL;

import java.util.List;
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
