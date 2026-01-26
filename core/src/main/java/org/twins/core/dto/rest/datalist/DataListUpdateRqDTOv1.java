package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DataListUpdateRqV1")
public class DataListUpdateRqDTOv1 extends DataListSaveRqDTOv1 {
    @Schema(description = "default option id")
    public UUID defaultOptionId;
}
