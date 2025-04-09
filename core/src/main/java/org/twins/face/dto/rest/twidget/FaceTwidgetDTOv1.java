package org.twins.face.dto.rest.twidget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.face.FaceDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceTwidgetDTOv1", description = "Twidget basic dto")
public abstract class FaceTwidgetDTOv1 extends FaceDTOv1 {
    @Schema(description = "an id of twin, for which current config is pointed. " +
            "It can bu useful when we want to display widget witch some head twin data on current twin page. ")
    public UUID pointedTwinId;

}
