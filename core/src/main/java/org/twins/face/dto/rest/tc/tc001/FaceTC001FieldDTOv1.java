package org.twins.face.dto.rest.tc.tc001;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceTC001FieldV1", description = "TC001 field config")
public class FaceTC001FieldDTOv1 {
    @Schema(description = "uniq key")
    public String key;

    @Schema(description = "some label for field")
    public String label;

    @Schema(description = "twin field (also basic field constant supported)")
    public UUID twinClassFieldId;
}
