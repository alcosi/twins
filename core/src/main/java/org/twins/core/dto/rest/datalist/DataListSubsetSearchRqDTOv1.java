package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.DataListSubsetSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DataListSubsetSearchRqV1")
public class DataListSubsetSearchRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public DataListSubsetSearchDTOv1 search;

    @Schema(description = "Sort field. Default: key")
    public DataListSubsetSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
