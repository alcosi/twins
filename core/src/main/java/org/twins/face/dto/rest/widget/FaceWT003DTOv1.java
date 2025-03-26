package org.twins.face.dto.rest.widget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.face.FaceDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceWT003v1", description = "Twin images simple gallery widget")
public class FaceWT003DTOv1 extends FaceDTOv1 {
    @Schema(description = "uniq key")
    public String key;

    @Schema(description = "some label for widget")
    public String label;

    @Schema(description = "only images from given field should be taken, if empty - then all twins images")
    public UUID imagesTwinClassFieldId;
}
