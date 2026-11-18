package org.twins.face.dto.rest.widget.wt009;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.face.dto.rest.widget.FaceWidgetDTOv1;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "FaceWT009v1")
public class FaceWT009DTOv1 extends FaceWidgetDTOv1 {

    @Schema(description = "twin action")
    private List<FaceWT009ActionDTOv1> actions;
}
