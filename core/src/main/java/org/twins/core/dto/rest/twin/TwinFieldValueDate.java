package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinFieldValueDate")
public class TwinFieldValueDate implements TwinFieldValue {
    @Schema(description = "discriminator", requiredMode = Schema.RequiredMode.REQUIRED)
    public String fieldType = TwinFieldValueDate.class.getSimpleName();

    @Schema(description = "Date")
    public String date;
}
