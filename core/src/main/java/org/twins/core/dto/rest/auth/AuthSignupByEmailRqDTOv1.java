package org.twins.core.dto.rest.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AuthSignupByEmailRqV1")
public class AuthSignupByEmailRqDTOv1 extends Request {
    @Schema(description = "first name", example = DTOExamples.NAME)
    public String firstName;

    @Schema(description = "last name", example = DTOExamples.NAME)
    public String lastName;

    @Schema(description = "password", example = DTOExamples.EMAIL)
    public String email;

    @Schema(description = "password", example = DTOExamples.PASSWORD)
    public String password;

    @Schema(description = "public key id")
    public UUID publicKeyId;
}
