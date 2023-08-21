package org.twins.core.dto.rest.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@ApiModel(value = "UserDTOv1")
public class UserDTOv1 {
    @ApiModelProperty(notes = "id", example = "c2a7f81f-d7da-43e8-a1d3-18d6f632878b")
    public UUID id;

    @ApiModelProperty(notes = "name", example = "John Doe")
    public String name;

    @ApiModelProperty(notes = "email", example = "some@email.com")
    public String email;

    @ApiModelProperty(notes = "avatar url", example = "http://twins.org/a/avatar/carkikrefmkawfwfwg.png")
    public String avatar;
}
