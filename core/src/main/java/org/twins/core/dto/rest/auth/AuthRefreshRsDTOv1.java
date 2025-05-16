package org.twins.core.dto.rest.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AuthRefreshRsV1")
public class AuthRefreshRsDTOv1 extends Response {
    @Schema(description = "tokens data")
    public TokensDTOv1 tokens;
}
