package org.twins.face.dto.rest.tc.tc002;

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
@Schema(name = "FaceTC002v1")
public class FaceTC002DTOv1 extends FaceDTOv1 {
    @Schema(description = "uniq key")
    public String key;

    @Schema(description = "save button label")
    public String saveButtonLabel;

    @Schema(description = "header")
    public String header;

    @Schema(description = "Header icon url. Might be relative")
    public String icon;

    @Schema(description = "Style classes")
    public Set<String> styleClasses;

    @Schema(description = "options")
    public List<FaceTC002OptionDTOv1> options;
}
