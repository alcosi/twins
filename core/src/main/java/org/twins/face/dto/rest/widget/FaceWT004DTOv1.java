package org.twins.face.dto.rest.widget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.face.FaceDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceWT004v1", description = "Twin i18n field accordion widget")
public class FaceWT004DTOv1 extends FaceDTOv1 {
    @Schema(description = "uniq key")
    public String key;

    @Schema(description = "label for widget")
    public String label;

    @Schema(description = "widget should display an accordion with translations for given field")
    public UUID i18nTwinClassFieldId;
}
