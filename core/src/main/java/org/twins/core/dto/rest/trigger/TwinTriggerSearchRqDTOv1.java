package org.twins.core.dto.rest.trigger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.TwinTriggerSortField;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TwinTriggerSearchRqV1")
public class TwinTriggerSearchRqDTOv1 extends Request {
    @Schema(description = "search")
    public TwinTriggerSearchDTOv1 search;

    @Schema(description = "Sort field. Default: name")
    public TwinTriggerSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
