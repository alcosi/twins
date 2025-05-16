package org.twins.core.dto.rest.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.auth.methods.AuthMethodDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name = "AuthConfigV1")
public class AuthConfigDTOv1 {
    @Schema(description = "some name")
    public String name;

    @Schema(description = "supported auth methods by linked identity provider")
    public List<AuthMethodDTOv1> authMethods;
}
