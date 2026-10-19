package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DataListOptionProjectionCreateV1")
public class DataListOptionProjectionCreateDTOv1 {
    @NotNull
    @Schema(description = "projection type id")
    public UUID projectionTypeId;

    @NotNull
    @Schema(description = "src data list option id")
    public UUID srcDataListOptionId;

    @NotNull
    @Schema(description = "dst data list option id")
    public UUID dstDataListOptionId;
}
