package org.twins.face.dto.rest.widget.wt011;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceWT011RowV1")
public class FaceWT011RowDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "order among sibling rows, smaller values first")
    public Integer order;

    @Schema(description = "fields may wrap to a new visual line on narrow screens; false = the row always stays on one line")
    public Boolean allowWrap;

    @Schema(description = "styles, converted to css classes")
    public Set<String> styleClasses;

    @Schema(description = "row fields, sorted by order")
    public List<FaceWT011FieldDTOv1> fields;
}
