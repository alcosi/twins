package org.twins.face.dto.rest.twidget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceTW001v1", description = "Twin images simple gallery widget")
public class FaceTW001DTOv1 extends FaceTwidgetDTOv1 {
    @Schema(description = "uniq key")
    public String key;

    @Schema(description = "some label for widget")
    public String label;

    @Schema(description = "only images from given field [by id] should be taken, if empty - then all twins images")
    public UUID imagesTwinClassFieldId;
}
