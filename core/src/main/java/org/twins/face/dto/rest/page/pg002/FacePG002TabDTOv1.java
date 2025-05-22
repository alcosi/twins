package org.twins.face.dto.rest.page.pg002;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FacePG002TabV1")
public class FacePG002TabDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "page title")
    public String title;

    @Schema(description = "Icon url. Might be relative")
    public String icon;

    @Schema(description = "page layout")
    public Set<String> styleClasses;

    @Schema(description = "tab widgets list")
    public List<FacePG002WidgetDTOv1> widgets;

    @Schema(name = "order")
    public Integer order;
}
