package org.twins.core.dto.rest.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AuthConfigRsV1")
public class AuthConfigRsDTOv1 extends Response {
    @Schema(description = "supported auth methods by linked identity provider")
    public AuthConfigDTOv1 config;
}
