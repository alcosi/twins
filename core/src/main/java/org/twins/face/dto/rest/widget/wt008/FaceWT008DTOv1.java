package org.twins.face.dto.rest.widget.wt008;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.face.FaceDTOv1;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class FaceWT008DTOv1 extends FaceDTOv1 {
    @Schema(description = "widget key")
    public String key;

    @Schema(description = "widget label, empty falls back to FE default \"Comments\"")
    public String label;

    @Schema(description = "show chart groupings")
    public Boolean showChart;

    @Schema(description = "styles, converted to css classes")
    public Set<String> styleClasses;
}
