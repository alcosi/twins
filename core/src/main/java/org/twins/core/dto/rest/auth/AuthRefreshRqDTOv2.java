package org.twins.core.dto.rest.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AuthRefreshRqV2")
public class AuthRefreshRqDTOv2 extends Request {
    @Schema(description = "refreshToken")
    public String refreshToken;

    @Schema(description = "agent fingerprint (hash)")
    public String fingerprint;
}
