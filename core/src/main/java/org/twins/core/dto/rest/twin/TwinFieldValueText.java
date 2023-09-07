package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv1;

import java.util.SortedMap;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinFieldValueText")
public class TwinFieldValueText implements TwinFieldValue {
    @Schema(description = "discriminator", requiredMode = Schema.RequiredMode.REQUIRED)
    public String fieldType = TwinFieldValueText.class.getSimpleName();

    @Schema(description = "Some simple text", example = "Hello world")
    public String text;
}
