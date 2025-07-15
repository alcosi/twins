package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

// On adding the new implementation, remember about TwinTransitionPerformResultDTOMixIn.class & ApplicationConfig.class
@Schema(
        additionalProperties = Schema.AdditionalPropertiesValue.FALSE,
        description = "Polymorphic twin transition result",
        discriminatorProperty = "resultType",
        discriminatorMapping = {
                    @DiscriminatorMapping(value = TwinTransitionPerformResultMinorDTOv1.KEY, schema = TwinTransitionPerformResultMinorDTOv1.class),
                    @DiscriminatorMapping(value = TwinTransitionPerformResultMajorDTOv1.KEY, schema = TwinTransitionPerformResultMajorDTOv1.class)
        },
        oneOf = {
                TwinTransitionPerformResultMinorDTOv1.class,
                TwinTransitionPerformResultMajorDTOv1.class
        }
)
public interface TwinTransitionPerformResultDTO {
    @Schema(hidden = true)
    default String resultType() { return null; }
}
