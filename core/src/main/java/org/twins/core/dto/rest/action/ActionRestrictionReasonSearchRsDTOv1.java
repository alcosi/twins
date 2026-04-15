package org.twins.core.dto.rest.action;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;

@EqualsAndHashCode(callSuper = false)
@Data
@Accessors(chain = true)
@Schema(name = "ActionRestrictionReasonSearchRsV1")
public class ActionRestrictionReasonSearchRsDTOv1 extends ActionRestrictionReasonListRsDTOv1 {
    @Schema(description = "pagination")
    private PaginationDTOv1 pagination;
}
