package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "DataListOptionProjectionV1")
public class DataListOptionProjectionDTOv1 {
    @Schema
    public UUID id;

    @Schema(description = "projection type id")
    public UUID projectionTypeId;

    @Schema(description = "src data list option id")
    public UUID srcDataListOptionId;

    @Schema(description = "dst data list option id")
    public UUID dstDataListOptionId;

    @Schema(description = "saved by user id")
    public UUID savedByUserId;

    @Schema(description = "changed at")
    public LocalDateTime changedAt;
}
