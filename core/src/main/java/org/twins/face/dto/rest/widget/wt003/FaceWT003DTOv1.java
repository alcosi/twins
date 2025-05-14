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

    @Schema(description = "message i18n id")
    public UUID messageI18nId;

    @Schema(description = "icon resource id")
    public UUID iconResourceId;

    @Schema(description = "styles, converted to css classes")
    public Set<String> styleClasses;
}
