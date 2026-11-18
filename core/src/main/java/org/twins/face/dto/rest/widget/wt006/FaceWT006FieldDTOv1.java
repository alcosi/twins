package org.twins.face.dto.rest.widget.wt006;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceWT006FieldV1", description = "WT006 field config")
public class FaceWT006FieldDTOv1 {
    @Schema(description = "uniq key")
    public String key;

    @Schema(description = "some label for field")
    public String label;

    @Schema(description = "twin field (also basic field constants supported)")
    public UUID twinClassFieldId;

    @Schema(description = "is editable")
    public Boolean editable;

    @Schema(description = "integer order")
    public int order;
}
