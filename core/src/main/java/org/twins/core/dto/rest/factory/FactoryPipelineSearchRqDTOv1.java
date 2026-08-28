package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.FactoryPipelineSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryPipelineSearchRqV1")
public class FactoryPipelineSearchRqDTOv1 extends Request {
    @Schema(description = "search params")
    public FactoryPipelineSearchDTOv1 search;

    @Schema(description = "Sort field. Default: active")
    public FactoryPipelineSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
