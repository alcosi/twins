package org.twins.core.dto.rest.tql;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@ApiModel(value = "TqlDTOv1")
public class TqlDTOv1 {
    @ApiModelProperty(notes = "id", example = "c2a7f81f-d7da-43e8-a1d3-18d6f632878b")
    public UUID id;

    @ApiModelProperty(notes = "created at", example = "1549632759")
    public Instant createdAt;


}
