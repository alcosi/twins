package org.twins.face.dto.rest.page.pg002;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FacePG002WidgetV1")
public class FacePG002WidgetDTOv1 {
    @Schema(description = "uniq id", example = DTOExamples.FACE_ID)
    public UUID id;

    @Schema(description = "page layout")
    public Set<String> styleClasses;

    @Schema(description = "is widget active")
    public Boolean active;

    @Schema(description = "widget face pointer")
    public UUID widgetFaceId;
}
