package org.twins.face.dto.rest.tc.tc002;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceTC002OptionV1")
public class FaceTC002OptionDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "class selector label")
    public String classSelectorLabel;

    @Schema(description = "extends hierarchy twin class id")
    public UUID twinClassId;

    @Schema(description = "hierarchy depth")
    public Integer extendsDepth;

    @Schema(description = "head twin id")
    public UUID pointedHeadTwinId;

    @Schema(description = "twin fields")
    public List<FaceTC002FieldDTOv1> fields;
}
