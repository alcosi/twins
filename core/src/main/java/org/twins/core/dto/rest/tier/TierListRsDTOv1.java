package org.twins.core.dto.rest.tier;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TierListRsV1")
public class TierListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - tier list")
    public List<TierDTOv1> tiers;
}
