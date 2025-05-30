package org.twins.core.dto.rest.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AuthRefreshRqV1")
public class AuthRefreshRqDTOv1 extends Request {
    @Schema(description = "refreshToken")
    public String refreshToken;
}
