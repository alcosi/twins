package org.twins.face.dto.rest.widget.wt002;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.face.dto.rest.twidget.tw004.FaceTW004FieldDTOv1;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceWT002ButtonV1")
public class FaceWT002ButtonDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "uniq key")
    public String key;

    @Schema(description = "label")
    public String label;

    @Schema(description = "Icon url. Might be relative")
    public String icon;

    @Schema(description = "widget layout")
    public Set<String> styleClasses;

    @Schema(description = "extends hierarchy twin class id")
    public UUID twinClassId;

    @Schema(description = "head twin id")
    public UUID pointedHeadTwinId;

    @Schema(description = "hierarchy depth")
    public Integer extendsDepth;

    @Schema(description = "twin fields")
    public List<FaceWT002ButtonFieldDTOv1> fields;
}
