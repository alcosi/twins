package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DataListProjectionCreateV1")
public class DataListProjectionUpdateDTOv1 extends DataListProjectionSaveDTOv1 {
    @Schema
    public UUID id;
}
