package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.DataListOptionSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DataListOptionSearchRqV2")
public class DataListOptionSearchRqDTOv2 extends Request {
    @Valid
    @Schema(description = "search params")
    public DataListOptionSearchDTOv1 search;

    @Schema(description = "Sort field. Default: createdAt")
    public DataListOptionSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
