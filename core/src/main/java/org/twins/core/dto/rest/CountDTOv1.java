package org.twins.core.dto.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "CountDTOv1")
public class CountDTOv1 {
    @Schema(description = "count of records in this group")
    public Long count;
}
