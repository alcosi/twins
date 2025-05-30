package org.twins.core.dto.rest.auth.methods;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import static org.twins.core.dto.rest.auth.methods.AuthMethodPasswordDTOv1.KEY;

@Data
@Accessors(fluent = true) //todo are you sure?
@Schema(name = KEY, description = "Login by username/password")
public class AuthMethodPasswordDTOv1 implements AuthMethodDTOv1 {
    public static final String KEY = "AuthMethodPasswordV1";
    public String type = KEY;

    @Schema(description = "icon")
    public String icon;

    @Schema(description = "label")
    public String label;

    @Schema(description = "New user register is supported")
    public boolean registerSupported;

    @Schema(description = "Password recover is supported")
    public boolean recoverSupported;

    @Schema(description = "Agent finger print check is supported")
    public boolean fingerPrintRequired;

    @Schema(description = "Public key to crypt password (if supported)")
    public String publicKey;
}
