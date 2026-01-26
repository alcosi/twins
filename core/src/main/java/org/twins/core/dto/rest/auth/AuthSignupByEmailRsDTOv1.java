package org.twins.core.dto.rest.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.enums.auth.EmailVerificationType;
import org.twins.core.dto.rest.Response;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AuthSignupByEmailRsV1")
public class AuthSignupByEmailRsDTOv1 extends Response {
    @Schema(description = "Email verification type (code or link)")
    public EmailVerificationType verificationType;
}
