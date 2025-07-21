package org.twins.face.dto.rest.tc.tc001;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceTC001OptionV1")
public class FaceTC001OptionDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "option label")
    public String label;

    @Schema(description = "twin class search id")
    public UUID twinClassSearchId;

    @Schema(description = "class selector label")
    public String classSelectorLabel;

    @Schema(description = "option name label")
    public String optionLabel;

    @Schema(description = "head twin id")
    public UUID pointedHeadTwinId;

    @Schema(description = "twin class field search id")
    public UUID twinClassFieldSearchId;
}
