package org.twins.face.dto.rest.widget.wt003;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.face.dao.widget.wt003.FaceWT003Level;
import org.twins.face.dto.rest.widget.FaceWidgetDTOv1;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class FaceWT003DTOv1 extends FaceWidgetDTOv1 {
    @Schema(description = "level (info, warn, etc.)")
    public FaceWT003Level level;

    @Schema(description = "title")
    public String title;

    @Schema(description = "message")
    public String message;

    @Schema(description = "icon")
    public String icon;

    @Schema(description = "styles, converted to css classes")
    public Set<String> styleClasses;
}
