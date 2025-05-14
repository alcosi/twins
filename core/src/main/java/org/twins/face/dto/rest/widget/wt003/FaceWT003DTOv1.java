package org.twins.face.dto.rest.widget.wt003;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.face.FaceDTOv1;
import org.twins.face.dao.widget.wt003.FaceWT003Level;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class FaceWT003DTOv1 extends FaceDTOv1 {
    @Schema(description = "level (info, warn, etc.)")
    public FaceWT003Level level;

    @Schema(description = "title")
    public String titleI18n;

    @Schema(description = "message")
    public String messageI18n;

    @Schema(description = "icon resource")
    public String iconResource;

    @Schema(description = "styles, converted to css classes")
    public Set<String> styleClasses;
}
