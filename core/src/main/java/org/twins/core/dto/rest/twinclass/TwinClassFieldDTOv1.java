package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldV1")
public class TwinClassFieldDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID id;

    @Schema(description = "key", example = DTOExamples.TWIN_CLASS_FIELD_KEY)
    public String key;

    @Schema(description = "name", example = "Serial number")
    public String name;

    @Schema(description = "required", example = "true")
    public boolean required;

    @Schema(description = "description", example = "")
    public String description;

    @Schema(description = "field descriptor", example = "")
    public TwinClassFieldDescriptorDTO descriptor;
}
