package org.twins.core.dto.rest.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AuthLoginRqV1")
public class AuthLoginRqDTOv2 extends Request {
    @Schema(description = "username", example = DTOExamples.EMAIL)
    public String username;

    @Schema(description = "password", example = DTOExamples.PASSWORD)
    public String password;

    @Schema(description = "agent fingerprint (hash)")
    public String fingerprint;
}
