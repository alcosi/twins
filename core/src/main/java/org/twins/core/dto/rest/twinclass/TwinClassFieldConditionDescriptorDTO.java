package org.twins.core.dto.rest.twinclass;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "conditionType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TwinClassFieldConditonDescriptorValueDTOv1.class, name = TwinClassFieldConditonDescriptorValueDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinClassFieldConditionDescriptorParamDTOv1.class, name = TwinClassFieldConditionDescriptorParamDTOv1.KEY)
})
@Schema(additionalProperties = Schema.AdditionalPropertiesValue.FALSE,
        description = "One of values",
        discriminatorProperty = "conditionType",
        oneOf = {
                TwinClassFieldConditonDescriptorValueDTOv1.class,
                TwinClassFieldConditionDescriptorParamDTOv1.class
        }
)
public interface TwinClassFieldConditionDescriptorDTO {
    @Schema(description = "discriminator", requiredMode = Schema.RequiredMode.REQUIRED)
    String conditionType();
}
