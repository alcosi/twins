package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.FactoryConditionSetSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryConditionSetSearchRqV1")
public class FactoryConditionSetSearchRqDTOv1 extends Request {
    @Schema(description = "search params")
    public FactoryConditionSetSearchDTOv1 search;

    @Schema(description = "Sort field. Default: createdAt")
    public FactoryConditionSetSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
