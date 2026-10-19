package org.twins.core.domain.twinclass;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class TwinClassFieldValidatorCreate extends TwinClassFieldValidatorSave {
}
