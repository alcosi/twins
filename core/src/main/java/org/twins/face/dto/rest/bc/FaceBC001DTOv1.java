package org.twins.face.dto.rest.bc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.face.FaceDTOv1;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "FaceBC001V1", description = "Breadcrumbs widget dto")
public class FaceBC001DTOv1 extends FaceDTOv1 {

    @Schema(description = "items list")
    private List<FaceBC001ItemDTOv1> items;
}
