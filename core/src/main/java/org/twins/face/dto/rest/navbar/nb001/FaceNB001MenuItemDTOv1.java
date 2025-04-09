package org.twins.face.dto.rest.navbar.nb001;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceNB001MenuItemV1")
public class FaceNB001MenuItemDTOv1 {
    @Schema(description = "item id", example = DTOExamples.FACE_ID)
    public UUID id;

    @Schema(description = "uniq menu item key")
    public String key;

    @Schema(description = "UI label for item")
    public String label;

    @Schema(description = "description")
    public String description;

    @Schema(description = "item is not selectable")
    public boolean disabled;

    @Schema(description = "Icon url. Might be relative")
    public String icon;

    @Schema(description = "domain navigation bar pointer")
    public UUID targetPageFaceId;

    @Schema(description = "permission id")
    public UUID permissionId;

    @Schema(description = "parent face menu item id")
    public UUID parentFaceMenuItemId;

    @Schema(description = "childs")
    public List<FaceNB001MenuItemDTOv1> childs;
}
