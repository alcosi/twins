package org.twins.core.dao.validator;

import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;

import java.util.UUID;

public interface ContainsTwinValidatorSet extends EasyLoggable {
    UUID getId();
    UUID getTwinValidatorSetId();
    TwinValidatorSetEntity getTwinValidatorSet();
    ContainsTwinValidatorSet setTwinValidatorSet(TwinValidatorSetEntity twinValidatorSet);
    Kit<TwinValidatorEntity, UUID> getTwinValidatorKit();
    ContainsTwinValidatorSet setTwinValidatorKit(Kit<TwinValidatorEntity, UUID> twinValidators);
}
