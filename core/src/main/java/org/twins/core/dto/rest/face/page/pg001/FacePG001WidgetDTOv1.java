package org.twins.core.dto.rest.face.page.pg001;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FacePG001WidgetV1")
public class FacePG001WidgetDTOv1 {
    @Schema(description = "uniq id", example = DTOExamples.FACE_ID)
    public UUID id;

    @Schema(description = "uniq menu item key")
    public Integer order;

    @Schema(description = "widget face pointer")
    public UUID widgetFaceId;
}
