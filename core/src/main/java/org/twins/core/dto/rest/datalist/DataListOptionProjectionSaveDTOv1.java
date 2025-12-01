package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DataListOptionProjectionSaveV1")
public class DataListOptionProjectionSaveDTOv1 {
    @Schema(description = "projection type id")
    public UUID projectionTypeId;

    @Schema(description = "src data list option id")
    public UUID srcDataListOptionId;

    @Schema(description = "dst data list option id")
    public UUID dstDataListOptionId;
}
