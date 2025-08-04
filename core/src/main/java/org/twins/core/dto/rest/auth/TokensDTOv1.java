package org.twins.core.dto.rest.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "AuthTokensV1")
public class TokensDTOv1 {
    @Schema(description = "authToken")
    public String authToken;

    @Schema(description = "refreshToken")
    public String refreshToken;
}
