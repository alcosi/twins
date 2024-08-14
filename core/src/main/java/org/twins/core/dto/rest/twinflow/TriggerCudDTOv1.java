package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TriggerCudV1")
public class TriggerCudDTOv1 {

    @Schema(description = "triggers create list")
    public List<TriggerCreateDTOv1> create;

    @Schema(description = "triggers update list")
    public List<TriggerUpdateDTOv1> update;

    @Schema(description = "triggers ids list to deletes")
    public List<UUID> delete;
}
