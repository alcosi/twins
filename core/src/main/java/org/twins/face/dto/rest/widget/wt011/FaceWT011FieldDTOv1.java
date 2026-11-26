package org.twins.face.dto.rest.widget.wt011;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceWT011FieldV1")
public class FaceWT011FieldDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "twin class field id; the field descriptor (including editability) ships in relatedObjects")
    public UUID twinClassFieldId;

    @Schema(description = "label override; falls back to the twin class field's own label")
    public String label;

    @Schema(description = "order among sibling fields, smaller values first")
    public Integer order;

    @Schema(description = "relative width inside the row (flex-grow semantics: width = weight / sum of sibling weights)")
    public Integer widthWeight;

    @Schema(description = "styles, converted to css classes")
    public Set<String> styleClasses;
}
