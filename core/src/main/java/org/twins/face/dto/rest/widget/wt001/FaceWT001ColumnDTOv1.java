package org.twins.face.dto.rest.widget.wt001;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceWT001ColumnV1")
public class FaceWT001ColumnDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "label")
    public String label;

    @Schema(description = "twin class field id")
    public UUID twinClassFieldId;

    @Schema(description = "order")
    public Integer order;

    @Schema(description = "show by default")
    public Boolean showByDefault;
}
