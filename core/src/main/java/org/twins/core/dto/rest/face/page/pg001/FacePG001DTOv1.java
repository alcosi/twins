package org.twins.core.dto.rest.face.page.pg001;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.face.FaceDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name = "FacePG001v1")
public class FacePG001DTOv1 extends FaceDTOv1 {
    @Schema(description = "page widgets list")
    public List<FacePG001WidgetDTOv1> widgets;
}
