package org.twins.core.dto.rest.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "UserV1")
public class UserDTOv1 {
    @Schema(description = "id", example = DTOExamples.USER_ID)
    public UUID id;

    @Schema(description = "fullName", example = "John Doe")
    public String fullName;

    @Schema(description = "email", example = "some@email.com")
    public String email;

    @Schema(description = "avatar url", example = "http://twins.org/a/avatar/carkikrefmkawfwfwg.png")
    public String avatar;
}
