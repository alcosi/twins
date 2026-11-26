package org.twins.face.dto.rest.widget.wt011;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.face.dto.rest.widget.FaceWidgetDTOv1;

import java.util.List;
import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FaceWT011v1")
public class FaceWT011DTOv1 extends FaceWidgetDTOv1 {
    @Schema(description = "widget key")
    public String key;

    @Schema(description = "form title")
    public String label;

    @Schema(description = "submit button label (edit mode only), empty falls back to FE default")
    public String submitLabel;

    @Schema(description = "cancel button label (edit mode only), empty falls back to FE default")
    public String cancelLabel;

    @Schema(description = "styles, converted to css classes")
    public Set<String> styleClasses;

    @Schema(description = "form columns, sorted by order")
    public List<FaceWT011ColumnDTOv1> columns;
}
