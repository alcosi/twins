package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.TwinFactoryTriggerSortField;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TwinFactoryTriggerSearchRqV1")
public class FactoryTriggerSearchRqDTOv1 extends Request {
    @Schema(description = "search")
    public FactoryTriggerSearchDTOv1 search;

    @Schema(description = "Sort field. Default: active")
    public TwinFactoryTriggerSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
