package org.twins.face.dto.rest.widget.wt001;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.face.FaceDTOv1;

import java.util.List;
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

    @Schema(description = "show given columns from table and filter")
    public List<FaceWT001ColumnDTOv1> showColumns;
}
