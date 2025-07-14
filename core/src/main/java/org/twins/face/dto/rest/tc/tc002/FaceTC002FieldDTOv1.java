package org.twins.face.dto.rest.tc.tc002;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceTC002FieldV1", description = "TC002 field config")
public class FaceTC002FieldDTOv1 {
    @Schema(description = "uniq key")
    public String key;

    @Schema(description = "some label for field")
    public String label;

    @Schema(description = "twin field (also basic field constant supported)")
    public UUID twinClassFieldId;
}
