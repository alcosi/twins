package org.twins.face.dto.rest.navbar;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.face.FaceDTOv1;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "FaceNB001v1")
public class FaceNB001DTOv1 extends FaceDTOv1 {
    @Schema(description = "label for admin area menu")
    public String adminAreaLabel;

    @Schema(description = "Icon for admin area menu")
    public String adminAreaIcon;

    @Schema(description = "label for user area menu")
    public String userAreaLabel;

    @Schema(description = "Icon for user area menu")
    public String userAreaIcon;

    @Schema(description = "menu items list")
    public List<FaceNB001MenuItemDTOv1> userAreaMenuItems;
}
