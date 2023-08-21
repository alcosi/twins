package org.twins.core.dto.rest.twin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.Instant;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@ApiModel(value = "TwinV1")
public class TwinDTOv1 {
    @ApiModelProperty(notes = "id", example = "c2a7f81f-d7da-43e8-a1d3-18d6f632878b")
    public UUID id;

    @ApiModelProperty(notes = "externalId", example = "934599502DFFAE")
    public String externalId;

    @ApiModelProperty(notes = "created at", example = "1549632759")
    public Instant createdAt;

    @ApiModelProperty(notes = "name", example = "Oak")
    public String name;

    @ApiModelProperty(notes = "description", example = "The biggest tree")
    public String description;

    @ApiModelProperty(notes = "status")
    public TwinStatusDTOv1 status;

    @ApiModelProperty(notes = "current assigner")
    public UserDTOv1 assignerUser;

    @ApiModelProperty(notes = "author")
    public UserDTOv1 authorUser;


}
