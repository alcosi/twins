package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "DataListProjectionV1")
public class DataListProjectionDTOv1 {
    @Schema
    public UUID id;

    @Schema(description = "src data list id")
    public UUID srcDataListId;

    @Schema(description = "dst data list id")
    public UUID dstDataListId;

    @Schema
    public String name;

    @Schema(description = "saved by user id")
    public UUID savedByUserId;

    @Schema(description = "changed at")
    public LocalDateTime changedAt;
}
