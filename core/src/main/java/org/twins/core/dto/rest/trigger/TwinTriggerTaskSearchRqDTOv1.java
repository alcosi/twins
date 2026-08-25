package org.twins.core.dto.rest.trigger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.TwinTriggerTaskSortField;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TwinTriggerTaskSearchRqV1")
public class TwinTriggerTaskSearchRqDTOv1 extends Request {
    @Schema(description = "search")
    public TwinTriggerTaskSearchDTOv1 search;

    @Schema(description = "Sort field. Default: createdAt")
    public TwinTriggerTaskSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
