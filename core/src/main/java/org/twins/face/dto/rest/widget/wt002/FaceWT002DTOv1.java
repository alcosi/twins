package org.twins.face.dto.rest.widget.wt002;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.face.FaceDTOv1;

import java.util.List;
import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FaceWT002v1")
public class FaceWT002DTOv1 extends FaceDTOv1 {
    @Schema(description = "uniq key")
    public String key;

    @Schema(description = "widget layout")
    public Set<String> styleClasses;

    @Schema(description = "creat twin buttons array")
    public List<FaceWT002ButtonDTOv1> buttons;
}
