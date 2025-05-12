package org.twins.face.dto.rest.twidget.tw004;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.face.dto.rest.twidget.FaceTwidgetDTOv1;

import java.util.UUID;

@Deprecated
@Data
@Accessors(chain = true)
@Schema(name = "FaceTW004v1", description = "Twin single field view/edit twidget")
public class FaceTW004DTOv1 extends FaceTwidgetDTOv1 {
    @Schema(description = "uniq key")
    public String key;

    @Schema(description = "some label for twidget")
    public String label;

    @Schema(description = "twin field (also basic field constants supported)")
    public UUID twinClassFieldId;
}
