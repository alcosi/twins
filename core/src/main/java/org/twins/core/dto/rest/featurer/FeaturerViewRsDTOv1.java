package org.twins.core.dto.rest.featurer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FeaturerViewRsV1")
public class FeaturerViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - featurer")
    public FeaturerDTOv1 featurer;
}
