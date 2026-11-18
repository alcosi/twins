package org.twins.face.dto.rest.widget.wt006;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.face.dto.rest.widget.FaceWidgetDTOv1;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "FaceWT006v2", description = "Twin single field view/edit widget")
public class FaceWT006DTOv2 extends FaceWidgetDTOv1 {

    @Schema(description = "widget label")
    public String label;

    @Schema(description = "style classes")
    public String styleClasses;

    @Schema(description = "twin fields")
    public List<FaceWT006FieldDTOv1> fields;
}
