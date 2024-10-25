package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.dao.validator.TwinValidatorSetRepository;
import org.twins.core.dao.validator.Validator;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinValidatorSetService {

    private final TwinValidatorSetRepository twinValidatorSetRepository;
    @Lazy
    private final AuthService authService;

    public TwinValidatorSetEntity loadTwinValidatorSet(TwinValidatorEntity twinValidatorEntity) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (twinValidatorEntity.getTwinValidatorSet() != null)
            return twinValidatorEntity.getTwinValidatorSet();
        twinValidatorEntity.setTwinValidatorSet(twinValidatorSetRepository.findAllByIdAndDomainId(twinValidatorEntity.getTwinValidatorSetId(), apiUser.getDomainId()));
        return twinValidatorEntity.getTwinValidatorSet();
    }

    public TwinValidatorSetEntity loadTwinValidatorSet(Validator implementedValidatorRule) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (implementedValidatorRule.getTwinValidatorSet() != null)
            return implementedValidatorRule.getTwinValidatorSet();
        implementedValidatorRule.setTwinValidatorSet(twinValidatorSetRepository.findAllByIdAndDomainId(implementedValidatorRule.getTwinValidatorSetId(), apiUser.getDomainId()));
        return implementedValidatorRule.getTwinValidatorSet();
    }
}
