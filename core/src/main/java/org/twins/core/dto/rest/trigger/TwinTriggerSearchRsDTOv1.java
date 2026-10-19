package org.twins.core.dto.rest.trigger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TwinTriggerSearchRsV1")
public class TwinTriggerSearchRsDTOv1 extends TwinTriggerListRsDTOv1 {
    @Schema(description = "pagination data")
    public PaginationDTOv1 pagination;
}
