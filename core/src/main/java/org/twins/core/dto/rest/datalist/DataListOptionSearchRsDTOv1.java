package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DataListOptionSearchRsV1")
public class DataListOptionSearchRsDTOv1 extends DataListOptionListRsDTOv1 {
    @Schema(description = "pagination data")
    public PaginationDTOv1 pagination;
}
