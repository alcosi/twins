package org.twins.core.dto.rest.tier;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TierSearchRsV1")
public class TierSearchRsDTOv1 extends ResponseRelatedObjectsDTOv1 {

    @Schema(description = "pagination")
    public PaginationDTOv1 pagination;

    @Schema(description = "tiers")
    public List<TierDTOv1> tiers;
}
