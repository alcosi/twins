package org.twins.core.dao.validator;

import java.util.UUID;

public interface ContainsTwinValidatorSet {
    UUID getTwinValidatorSetId();
    TwinValidatorSetEntity getTwinValidatorSet();
    ContainsTwinValidatorSet setTwinValidatorSet(TwinValidatorSetEntity twinValidatorSet);

}
