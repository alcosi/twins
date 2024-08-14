package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "ValidatorCudV1")
public class ValidatorCudDTOv1 {

    @Schema(description = "validators create list")
    public List<ValidatorCreateDTOv1> create;

    @Schema(description = "validators update list")
    public List<ValidatorUpdateDTOv1> update;

    @Schema(description = "validators ids list to deletes")
    public List<UUID> delete;
}
