package org.twins.core.dto.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.SortDirection;

@Data
@Accessors(chain = true)
@Schema(name = "SortV1")
public class SortDTOv1 {
    @Schema(description = "sort field name")
    public String field;

    @Schema(description = "sort direction: ASC or DESC. Default: ASC")
    public SortDirection direction;
}
