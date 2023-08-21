package org.twins.core.dto.rest.twin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@ApiModel(value = "TwinStatusDTOv1")
public class TwinStatusDTOv1 {
    @ApiModelProperty(notes = "id", example = "c2a7f81f-d7da-43e8-a1d3-18d6f632878b")
    public UUID id;

    @ApiModelProperty(notes = "name", example = "PLN")
    public String name;

    @ApiModelProperty(notes = "description", example = "PLN")
    public String description;

    @ApiModelProperty(notes = "logo", example = "PLN")
    public String logo;
}
