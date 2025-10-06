package org.twins.core.dto.rest.twinclass;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "conditionType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TwinClassFieldConditionDescriptorBasicDTOv1.class, name = TwinClassFieldConditionDescriptorBasicDTOv1.KEY),
})
@Schema(additionalProperties = Schema.AdditionalPropertiesValue.FALSE,
        description = "One of values",
        discriminatorProperty = "conditionType",
        oneOf = {
                TwinClassFieldConditionDescriptorBasicDTOv1.class,
        }
)
public interface TwinClassFieldConditionDescriptorDTO {
    @Schema(description = "discriminator", requiredMode = Schema.RequiredMode.REQUIRED)
    String conditionType();
}
