package org.twins.face.dto.rest.tc.tc001;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.face.FaceDTOv1;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FaceTC001v1")
public class FaceTC001DTOv1 extends FaceDTOv1 {
    @Schema(description = "uniq key")
    public String key;

    @Schema(description = "save button label")
    public String saveButtonLabel;

    @Schema(description = "header")
    public String headerLabel;

    @Schema(description = "optionSelectLabel")
    public String optionSelectLabel;

    @Schema(description = "Header icon url. Might be relative")
    public String icon;

    @Schema(description = "Style classes")
    public Set<String> styleClasses;

    @Schema(description = "Sketch mode")
    public Boolean sketchMode;

    @Schema(description = "Single option silent mode")
    public Boolean singleOptionSilentMode;

    @Schema(description = "options")
    public List<FaceTC001OptionDTOv1> options;
}
