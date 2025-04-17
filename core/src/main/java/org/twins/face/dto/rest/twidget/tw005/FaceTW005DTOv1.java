package org.twins.face.dto.rest.twidget.tw005;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.face.dto.rest.twidget.FaceTwidgetDTOv1;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceWT005v1")
public class FaceTW005DTOv1 extends FaceTwidgetDTOv1 {
    @Schema(description = "twins of given twin class id")
    public boolean glue;

    @Schema(description = "twins of given twin class id")
    public boolean alignVertical;

    @Schema(description = "searchId")
    public UUID searchId;

    @Schema(description = "button group extra style attributes")
    public Map<String, String> styleAttributes;

    @Schema(description = "show given columns from table and filter")
    public List<FaceTW005ButtonDTOv1> buttons;
}
