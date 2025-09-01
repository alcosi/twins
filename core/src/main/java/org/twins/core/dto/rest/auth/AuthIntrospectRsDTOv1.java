package org.twins.core.dto.rest.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.twins.core.dto.rest.Response;

@Data
@EqualsAndHashCode(callSuper = true)
public class AuthIntrospectRsDTOv1 extends Response {
    @Schema(description = "client id")
    public String clientId;
    @Schema(description = "token expiry date (epoch millis)")
    public Long tokenExpiryDate;

}
