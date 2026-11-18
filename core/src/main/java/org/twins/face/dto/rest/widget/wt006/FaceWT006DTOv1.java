package org.twins.face.dto.rest.widget.wt006;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.face.dto.rest.widget.FaceWidgetDTOv1;

import java.util.UUID;

@Deprecated
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "FaceWT006v1", description = "Twin single field view/edit widget")
public class FaceWT006DTOv1 extends FaceWidgetDTOv1 {
    @Schema(description = "uniq key")
    public String key;

    @Schema(description = "some label for widget")
    public String label;

    @Schema(description = "twin field (also basic field constants supported)")
    public UUID twinClassFieldId;

    @Schema(description = "is editable")
    public Boolean editable;
}
