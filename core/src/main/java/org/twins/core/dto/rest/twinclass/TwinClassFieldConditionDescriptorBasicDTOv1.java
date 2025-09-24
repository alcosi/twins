package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldConditionElementType;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldConditionDescriptorBasicV1")
public class TwinClassFieldConditionDescriptorBasicDTOv1 implements TwinClassFieldConditionDescriptorDTO{
    public static final String KEY = "basic";

    @Override
    public String conditionType() {
        return KEY;
    }

    @Schema(description = "Condition element type", example = "value")
    private TwinClassFieldConditionElementType conditionElement;
    @Schema(description = "Value to compare with", example = "size")
    private String valueToCompareWith;
    @Schema(description = "Evaluated param key", example = "required")
    private String evaluatedParamKey;

}
