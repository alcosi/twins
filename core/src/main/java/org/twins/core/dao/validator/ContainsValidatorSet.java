package org.twins.core.dao.validator;

import java.util.UUID;

public interface ContainsValidatorSet {
    UUID getTwinValidatorSetId();
    TwinValidatorSetEntity getTwinValidatorSet();
    void setTwinValidatorSet(TwinValidatorSetEntity twinValidatorSet);

}
