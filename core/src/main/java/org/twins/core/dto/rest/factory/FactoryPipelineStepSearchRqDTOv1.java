package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.FactoryPipelineStepSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryPipelineStepSearchRqV1")
public class FactoryPipelineStepSearchRqDTOv1 extends Request {
    @Schema(description = "search params")
    public FactoryPipelineStepSearchDTOv1 search;

    @Schema(description = "Sort field. Default: order")
    public FactoryPipelineStepSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
