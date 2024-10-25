package org.twins.core.dao.validator;

import java.util.List;
import java.util.UUID;

public interface ValidatorRule {
    UUID getId();
    Integer getOrder();
    boolean isActive();
    UUID getTwinValidatorSetId();
    List<TwinValidatorEntity> getTwinValidators();
    TwinValidatorSetEntity getTwinValidatorSet();
    void setTwinValidatorSet(TwinValidatorSetEntity twinValidatorSet);

}
