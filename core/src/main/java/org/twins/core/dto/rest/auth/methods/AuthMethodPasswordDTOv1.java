package org.twins.core.dto.rest.auth.methods;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import static org.twins.core.dto.rest.auth.methods.AuthMethodPasswordDTOv1.KEY;

@Data
@Accessors(fluent = true) //todo are you sure?
@Schema(name = KEY)
public class AuthMethodPasswordDTOv1 implements AuthMethodDTOv1 {
    public static final String KEY = "AuthMethodPasswordV1";
    public String type = KEY;

    @Schema(description = "New user register is supported")
    public boolean registerSupported;

    @Schema(description = "Password recover is supported")
    public boolean recoverSupported;
}
