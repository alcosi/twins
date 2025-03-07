package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv3;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TagSearchRsV1")
public class TagSearchRsDTOv1 extends ResponseRelatedObjectsDTOv1 {

    @Schema(description = "pagination data")
    public PaginationDTOv1 pagination;

    @Schema(description = "results - tag option list")
    public List<DataListOptionDTOv3> options;
}