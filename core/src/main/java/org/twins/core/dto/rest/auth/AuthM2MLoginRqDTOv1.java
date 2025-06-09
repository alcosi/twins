package org.twins.core.dto.rest.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AuthM2MLoginRqV1")
public class AuthM2MLoginRqDTOv1 extends Request {
    @Schema(description = "client id")
    public String clientId;

    @Schema(description = "client secret")
    public String clientSecret;

    @Schema(description = "public key id")
    public UUID publicKeyId;
}
