package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinCreateRsV1")
public class TwinCreateRsDTOv1 extends TwinSaveRsV1 {
    @Schema(description = "new twin id")
    public UUID twinId;

    @Schema(description = "twin alias list")
    public List<String> twinAliasList;
}


