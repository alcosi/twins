package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinCountRqV1")
public class TwinCountRqDTOv1 extends Request {
    @Schema(description = "Search params")
    public TwinSearchExtendedDTOv2 search;

    @Size(max = 2)
    @Schema(description = "Group by TwinClassFieldId UUIDs. Supported: Basic fields and dynamic fields")
    public Set<UUID> groupFields;
}
