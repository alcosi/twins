package org.twins.core.dto.rest.featurer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "FeaturerCountV1")
public class FeaturerCountDTOv1 extends CountDTOv1 {
    @Schema(description = "featurer type id")
    public Integer featurerTypeId;

    @Schema(description = "deprecated")
    public Boolean deprecated;
}
