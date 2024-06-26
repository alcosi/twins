package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.twins.core.dto.rest.Response;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "LocaleRsV1")
public class LocaleRsDTOv1 extends Response {
    @Schema(description = "locale")
    public String locale;
}
