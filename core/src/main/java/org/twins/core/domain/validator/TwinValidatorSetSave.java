package org.twins.core.domain.validator;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.validator.TwinValidatorSetEntity;

@Data
@Accessors(chain = true)
public class TwinValidatorSetSave {
    public TwinValidatorSetEntity twinValidatorSet;
}
