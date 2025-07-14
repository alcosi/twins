package org.twins.face.dto.rest.twidget.tw006;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.face.dto.rest.twidget.FaceTwidgetDTOv1;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "FaceWT006v1")
public class FaceTW006DTOv1 extends FaceTwidgetDTOv1 {

    @Schema(description = "twin action")
    private List<FaceTW006ActionDTOv1> actions;
}
