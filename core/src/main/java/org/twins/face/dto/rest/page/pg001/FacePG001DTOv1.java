package org.twins.face.dto.rest.page.pg001;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.face.FaceDTOv1;

import java.util.List;
import java.util.Set;

@Data
@Accessors(chain = true)
@Schema(name = "FacePG001v1")
public class FacePG001DTOv1 extends FaceDTOv1 {
    @Schema(description = "page title")
    public String title;

    @Schema(description = "page layout")
    public Set<String> styleClasses;

    @Deprecated
    @Schema(description = "page layout")
    public String layout = "TWO_COLUMNS"; //todo delete me after UI update

    @Schema(description = "page widgets list")
    public List<FacePG001WidgetDTOv1> widgets;
}
