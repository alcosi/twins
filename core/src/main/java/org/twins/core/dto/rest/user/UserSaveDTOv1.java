package org.twins.core.dto.rest.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name =  "UserSaveV1")
public class UserSaveDTOv1 {
    @Schema(description = "fullName", example = "John Doe")
    public String fullName;

    @Schema(description = "email", example = "some@email.com")
    public String email;

    @Schema(description = "avatar url", example = "http://twins.org/a/avatar/carkikrefmkawfwfwg.png")
    public String avatar;
}
