package org.twins.core.dto.rest.tier;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TierRsV1")
public class TierRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - tier")
    public TierDTOv2 tier;
}
