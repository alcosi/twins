package org.twins.core.dao.validator;

import java.util.List;
import java.util.UUID;

public interface Validator {
    UUID getId();
    Integer getOrder();
    UUID getTwinValidatorSetId();
    List<TwinValidatorEntity> getTwinValidators();
    TwinValidatorSetEntity getTwinValidatorSet();
    void setTwinValidatorSet(TwinValidatorSetEntity twinValidatorSet);

}
