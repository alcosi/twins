package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinFieldValueColorHex")
public class TwinFieldValueColorHex implements TwinFieldValue {
    @Schema(description = "discriminator", requiredMode = Schema.RequiredMode.REQUIRED)
    public String fieldType = TwinFieldValueColorHex.class.getSimpleName();

    @Schema(description = "Color hex code", example = "#575584")
    public String hex;
}
