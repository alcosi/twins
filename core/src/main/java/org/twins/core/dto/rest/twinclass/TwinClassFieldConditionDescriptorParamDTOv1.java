package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldConditionDescriptorParamV1")
public class TwinClassFieldConditionDescriptorParamDTOv1 extends TwinClassFieldConditionDescriptorBasicDTOv1{
    public static final String KEY = "param";

    @Override
    public String conditionType() {
        return KEY;
    }

    @Schema(description = "Evaluated param key", example = "required")
    public String evaluatedParamKey;
}
