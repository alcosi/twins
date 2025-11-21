package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DataListProjectionSaveV1")
public class DataListProjectionSaveDTOv1 {
    @Schema(description = "src data list id")
    public UUID srcDataListId;

    @Schema(description = "src data list id")
    public UUID dstDataListId;

    @Schema
    public String name;
}
