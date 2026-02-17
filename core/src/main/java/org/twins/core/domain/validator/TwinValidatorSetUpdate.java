package org.twins.core.domain.validator;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class TwinValidatorSetUpdate extends TwinValidatorSetSave {
    private UUID id;
}
