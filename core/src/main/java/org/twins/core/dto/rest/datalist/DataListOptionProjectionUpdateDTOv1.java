package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DataListOptionProjectionUpdateV1")
public class DataListOptionProjectionUpdateDTOv1 {
    @NotNull
    @Schema(description = "data list option projection id")
    public UUID id;

    @Schema(description = "projection type id")
    public UUID projectionTypeId;

    @Schema(description = "src data list option id")
    public UUID srcDataListOptionId;

    @Schema(description = "dst data list option id")
    public UUID dstDataListOptionId;
}
