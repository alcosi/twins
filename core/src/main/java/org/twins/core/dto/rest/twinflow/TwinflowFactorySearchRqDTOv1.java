package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.TwinflowFactorySortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinflowFactorySearchRqV1")
public class TwinflowFactorySearchRqDTOv1 extends Request {

    @Schema(description = "search DTO")
    public TwinflowFactorySearchDTOv1 search;

    @Schema(description = "Sort field. Default: twinFactoryLauncherId")
    public TwinflowFactorySortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
