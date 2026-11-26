package org.twins.face.dto.rest.widget.wt011;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceWT011ColumnV1")
public class FaceWT011ColumnDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "order among sibling columns, smaller values first")
    public Integer order;

    @Schema(description = "relative width of the column (flex-grow semantics: width = weight / sum of sibling weights)")
    public Integer widthWeight;

    @Schema(description = "stacking order when columns collapse vertically on narrow screens; null = same as order")
    public Integer adaptiveOrder;

    @Schema(description = "styles, converted to css classes")
    public Set<String> styleClasses;

    @Schema(description = "column sections, sorted by order")
    public List<FaceWT011SectionDTOv1> sections;
}
