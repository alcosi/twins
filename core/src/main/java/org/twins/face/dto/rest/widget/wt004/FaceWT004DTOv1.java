package org.twins.face.dto.rest.widget.wt004;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.face.dto.rest.widget.FaceWidgetDTOv1;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "FaceWT004v1", description = "Twin images simple gallery widget")
public class FaceWT004DTOv1 extends FaceWidgetDTOv1 {
    @Schema(description = "uniq key")
    public String key;

    @Schema(description = "some label for widget")
    public String label;

    @Schema(description = "only images from given field [by id] should be taken, if empty - then all twins images")
    public UUID imagesTwinClassFieldId;

    @Schema(description = "boolean upload flag")
    public boolean uploadable;

    @Schema(description = "restriction id")
    public UUID restrictionId;
}
