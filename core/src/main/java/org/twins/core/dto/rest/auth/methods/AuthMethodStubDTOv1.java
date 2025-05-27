package org.twins.core.dto.rest.auth.methods;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import static org.twins.core.dto.rest.auth.methods.AuthMethodStubDTOv1.KEY;

@Data
@Accessors(fluent = true) //todo are you sure?
@Schema(name = KEY, description = "Stub auth token = user_id + business_account_id")
public class AuthMethodStubDTOv1 implements AuthMethodDTOv1 {
    public static final String KEY = "AuthMethodStubV1";
    public String type = KEY;

    @Schema(description = "icon")
    public String icon;

    @Schema(description = "label")
    public String label;
}
