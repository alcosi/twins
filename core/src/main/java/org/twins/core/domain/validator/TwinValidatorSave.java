package org.twins.core.domain.validator;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.validator.TwinValidatorEntity;

@Data
@Accessors(chain = true)
public class TwinValidatorSave {
    public TwinValidatorEntity twinValidator;
}
