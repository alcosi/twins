package org.twins.face.dto.rest.page.pg002;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.face.FaceDTOv1;
import org.twins.face.dao.page.pg002.FacePG002TabEntity;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name = "FacePG002TabV1")
public class FacePG002TabDTOv1 extends FaceDTOv1 {
    @Schema(description = "page title")
    public String title;

    @Schema(description = "Icon url. Might be relative")
    public String icon;

    @Schema(description = "tab layout")
    public FacePG002TabEntity.Layout layout;

    @Schema(description = "tab widgets list")
    public List<FacePG002WidgetDTOv1> widgets;
}
