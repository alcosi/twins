package org.twins.face.dto.rest.twidget.tw004;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceTW004FieldV1", description = "TW004 field config")
public class FaceTW004FieldDTOv1 {
    @Schema(description = "uniq key")
    public String key;

    @Schema(description = "some label for field")
    public String label;

    @Schema(description = "twin field (also basic field constants supported)")
    public UUID twinClassFieldId;
}
