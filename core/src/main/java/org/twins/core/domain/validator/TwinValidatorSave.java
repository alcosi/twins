package org.twins.core.domain.validator;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinValidatorSave {
    private UUID twinValidatorSetId;
    private Integer validatorFeaturerId;
    private Map<String, String> validatorParams;
    private Boolean invert;
    private Boolean active;
    private String description;
    private Integer order;
}
