package org.twins.face.dto.rest.widget.wt002;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceWT002ButtonFieldV1", description = "TW002Button field config")
public class FaceWT002ButtonFieldDTOv1 {
    @Schema(description = "uniq key")
    public String key;

    @Schema(description = "some label for field")
    public String label;

    @Schema(description = "twin field (also basic field constants supported)")
    public UUID twinClassFieldId;
}