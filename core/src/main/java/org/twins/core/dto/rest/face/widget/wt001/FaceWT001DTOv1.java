package org.twins.core.dto.rest.face.widget.wt001;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.face.FaceDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceWT001v1")
public class FaceWT001DTOv1 extends FaceDTOv1 {
    @Schema(description = "uniq key")
    public String key;

    @Schema(description = "UI label for item")
    public String label;

    @Schema(description = "twins of given twin class id")
    public UUID twinClassId;

    @Schema(description = "searchId")
    public UUID searchId;

    @Schema(description = "hide given basic columns from table and filter")
    public Set<String> hideColumns;
}
