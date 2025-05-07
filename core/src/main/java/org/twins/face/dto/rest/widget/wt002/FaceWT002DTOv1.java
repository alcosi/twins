package org.twins.face.dto.rest.widget.wt002;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.face.FaceDTOv1;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@Schema(name = "FaceWT002v1")
public class FaceWT002DTOv1 extends FaceDTOv1 {
    @Schema(description = "uniq key")
    public String key;

    @Schema(description = "button group extra style attributes")
    public Map<String, String> styleAttributes;

    @Schema(description = "show given columns from table and filter")
    public List<FaceWT002ButtonDTOv1> buttons;
}
