package org.twins.face.dto.rest.widget.wt007;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.face.dto.rest.widget.FaceWidgetDTOv1;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceWT007v1")
public class FaceWT007DTOv1 extends FaceWidgetDTOv1 {
    @Schema(description = "twins of given twin class id")
    public boolean glue;

    @Schema(description = "twins of given twin class id")
    public boolean alignVertical;

    @Schema(description = "searchId")
    public UUID searchId;

    @Schema(description = "styles, converted to css classes")
    public Set<String> styleClasses;

    @Schema(description = "show given columns from table and filter")
    public List<FaceWT007ButtonDTOv1> buttons;
}
