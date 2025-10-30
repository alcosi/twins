package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinClassFieldDeleteRqV1")
public class TwinClassFieldsDeleteRqDTOv1 {

    @Schema(description = "List of twin class field ids")
    private List<UUID> fieldIds;
}
