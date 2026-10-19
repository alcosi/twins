package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.FactoryMultiplierFilterSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryMultiplierFilterSearchRqV1")
public class FactoryMultiplierFilterSearchRqDTOv1 extends Request {
    @Schema(description = "search params")
    public FactoryMultiplierFilterSearchDTOv1 search;

    @Schema(description = "Sort field. Default: active")
    public FactoryMultiplierFilterSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
