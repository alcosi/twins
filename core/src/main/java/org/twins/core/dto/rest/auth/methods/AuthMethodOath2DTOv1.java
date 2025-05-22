package org.twins.core.dto.rest.auth.methods;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import static org.twins.core.dto.rest.auth.methods.AuthMethodOath2DTOv1.KEY;

@Data
@Accessors(fluent = true) //todo are you sure?
@Schema(name = KEY)
public class AuthMethodOath2DTOv1 implements AuthMethodDTOv1 {
    public static final String KEY = "AuthMethodOath2V1";
    public String type = KEY;

    @Schema(description = "button icon")
    public String iconUrl;

    @Schema(description = "button label")
    public String label;

    @Schema(description = "redirect url")
    public String redirectUrl;
}
