package org.twins.face.dto.rest.twidget.tw004;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.face.dto.rest.twidget.FaceTwidgetDTOv1;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "FaceTW004v2", description = "Twin single field view/edit twidget")
public class FaceTW004DTOv2 extends FaceTwidgetDTOv1 {

    @Schema(description = "widget label")
    public String label;

    @Schema(description = "style classes")
    public String styleClasses;

    @Schema(description = "twin fields")
    public List<FaceTW004FieldDTOv1> fields;
}
