package org.twins.core.domain.validator;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class TwinValidatorSetCreate extends TwinValidatorSetSave {
}
