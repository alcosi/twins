package org.twins.core.dto.rest.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Map;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AuthLogoutRqV1")
public class AuthLogoutRqDTOv1 extends Request {
    @Schema(description = "logout data. depends upon IDP")
    public Map<String, String> authData;
}
