package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "DataListCountV1")
public class DataListCountDTOv1 extends CountDTOv1 {
    // defaultOptionId is returned as a plain scalar UUID — DataListEntity has no @ManyToOne to the
    // default DataListOption, so no related object is loaded (client resolves by id if needed).
    @Schema(description = "default data list option id")
    public UUID defaultOptionId;
}
