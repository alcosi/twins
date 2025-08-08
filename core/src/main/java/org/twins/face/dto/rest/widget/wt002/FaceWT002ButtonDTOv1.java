package org.twins.face.dto.rest.widget.wt002;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

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

    @Schema(description = "face modal id")
    public UUID modalFaceId;
}
