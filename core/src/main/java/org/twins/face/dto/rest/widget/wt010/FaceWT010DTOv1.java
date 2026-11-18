package org.twins.face.dto.rest.widget.wt010;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.face.dto.rest.widget.FaceWidgetDTOv1;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceWT010v1")
public class FaceWT010DTOv1 extends FaceWidgetDTOv1 {

    @Schema(description = "twin class search id")
    public UUID twinClassSearchId;

    @Schema(description = "Search named params values")
    public Map<String, String> twinClassSearchParams;

    @Schema(description = "class selector label")
    public String classSelectorLabel;

    @Schema(description = "save button label")
    public String saveChangesLabel;

    @Schema(description = "label")
    public String label;

    @Schema(description = "icon url")
    public String iconUrl;
}
