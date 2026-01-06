package org.twins.core.service.user;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class UserFilterService {
    final UserRepository userRepository;
    final AuthService authService;
    public int countFilterResult(UUID userFilterId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        //todo filter by filter id
        int ret = 0;
        if (apiUser.isBusinessAccountSpecified())
            ret = (int) userRepository.countByBusinessAccountIdAndDomainId(apiUser.getBusinessAccount().getId(), apiUser.getDomain().getId());
        else
            ret = (int) userRepository.countByDomainId(apiUser.getDomain().getId());
        return ret;
    }

    public List<UserEntity> findUsers(UUID userFilterId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        //todo filter by filter id
        List<UserEntity> ret;
        if (apiUser.isBusinessAccountSpecified())
            ret = userRepository.findByBusinessAccountIdAndDomainId(apiUser.getBusinessAccount().getId(), apiUser.getDomain().getId());
        else
            ret = userRepository.findByDomainId(apiUser.getDomain().getId());
        return ret;
    }
}
