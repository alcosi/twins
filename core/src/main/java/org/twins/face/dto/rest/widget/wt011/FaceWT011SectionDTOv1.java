package org.twins.face.dto.rest.widget.wt011;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceWT011SectionV1")
public class FaceWT011SectionDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "optional section header")
    public String label;

    @Schema(description = "order among sibling sections, smaller values first")
    public Integer order;

    @Schema(description = "styles, converted to css classes")
    public Set<String> styleClasses;

    @Schema(description = "section rows, sorted by order")
    public List<FaceWT011RowDTOv1> rows;
}
