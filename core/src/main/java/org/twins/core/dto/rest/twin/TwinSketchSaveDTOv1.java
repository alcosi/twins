package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinSketchSaveV1")
public class TwinSketchSaveDTOv1 {

    @Schema(description = "Class Id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID classId;

    @Schema(description = "fields")
    public Map<String, String> fields;
}
